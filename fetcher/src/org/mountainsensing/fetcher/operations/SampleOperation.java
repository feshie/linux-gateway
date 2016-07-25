/**
 * get data samples from nodes using CoAP GET requests
 * Arthur Fabre, University of Southampton, 2015
 * mountainsensing.org
 */
package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.mountainsensing.fetcher.CoapException;
import org.mountainsensing.fetcher.CoapException.Method;
import org.mountainsensing.fetcher.utils.EpochDate;
import org.mountainsensing.fetcher.utils.FormatUtils;
import org.mountainsensing.fetcher.utils.ProtoBufUtils;
import org.mountainsensing.fetcher.utils.UTCEpochDateFormat;
import org.mountainsensing.pb.Readings.Sample;
import org.mountainsensing.pb.Rs485Message.Rs485;

/**
 * Operations for dealing with Samples.
 */
public abstract class SampleOperation extends NodeOperation {

    private static final Logger log = Logger.getLogger(SampleOperation.class.getName());

    /**
     * Map used to override the printing of Samples.
     * The key is the integer id of the field to override the printing for,
     * mapping to a FieldOverride returning the String to use for that field.
     */
    private static final Map<Integer, ProtoBufUtils.FieldOverride<Sample>> sampleOverrideMap;
    static {
        sampleOverrideMap = new HashMap<>();
        // Field 1 is the sampling time, we need to handle it sepcially to print epoch + human readable time.
        sampleOverrideMap.put(Sample.TIME_FIELD_NUMBER, new ProtoBufUtils.FieldOverride<Sample>() {
            @Override
            public String toString(Sample message) {
                return new UTCEpochDateFormat().format(new EpochDate(message.getTime()));
            }
        });

        // Field 10 is the embedded AVR message - handle it specially to actually decode it
        sampleOverrideMap.put(Sample.AVR_FIELD_NUMBER, new ProtoBufUtils.FieldOverride<Sample>() {
            @Override
            public String toString(Sample message) throws IOException {
                StringBuilder avr = new StringBuilder();

                avr.append(System.lineSeparator());

                // Append the logical representation of the message
                avr.append(FormatUtils.indent(ProtoBufUtils.toString(Rs485.parseFrom(message.getAVR()))));
                return avr.toString();
            }
        });
    }

    /**
     * Canary value for the latest sample.
     */
    private static final int LATEST_SAMPLE = 0;

    private static final String RESSOURCE = "sample";

    @Parameter(names = {"-s", "--sample-id"}, validateWith = SampleExclusionValidator.class, description = "Sample id. " + LATEST_SAMPLE + " for latest sample.")
    private int sampleId = LATEST_SAMPLE;

    /**
     * Ensures that any Operations who implement other means of specifying the sample (ie --all) can verify both options aren't supplied.
     */
    public static class SampleExclusionValidator implements IParameterValidator {
        private static String previousName = null;
        @Override
        public void validate(String name, String value) throws ParameterException {
            if (previousName != null) {
                throw new ParameterException("Parameter " + name + " can not be used in conjunction with " + previousName);
            }
            previousName = name;
        }
    }

    /**
     * Get a single sample from the node, without deleting it.
     */
    @Parameters(commandDescription = "Get a sample from the node(s)")
    public static class Get extends SampleOperation {

        @Override
        public void processSample(URI uri) throws IOException {
            Sample sample = getSample(uri);
            log.log(Level.INFO, "Got sample: \n{0}", sampleToString(sample));
        }
    }

    /**
     * Delete samples from the nodes.
     */
    @Parameters(commandDescription = "Delete a sample from the node(s)")
    public static class Delete extends SampleOperation {

        @Override
        public void processSample(URI uri) throws IOException {
            deleteSample(uri);
            log.log(Level.INFO, "Deleted sample: {0}", uri);
        }
    }

    /**
     * Get, decode, and delete samples from a node.
     */
    @Parameters(commandDescription = "Get sample(s) from the node(s), decode them, delete them from the node(s), and output them in a directory")
    public static class Grab extends SampleOperation {

        @Parameter(names = {"-d", "--destination"}, description = "Directory in which to output protocol buffer encoded samples")
        private String dir = "/ms/queue/";

        @Parameter(names = {"-a", "--all"}, validateWith = SampleExclusionValidator.class, description = "Grab all samples from the node(s). This cannot be used in conjunction with --sample")
        private boolean shouldProcessAll = false;

        private boolean hasReachedEnd;

        @Override
        public void processSample(URI uri) throws IOException, CoapException {
            Sample sample;

            // Assume we've reached the last sample until we know otherwise.
            hasReachedEnd = true;

            try {
                sample = getSample(uri);
                // If we have a sample, it means we haven't reached the end.
                hasReachedEnd = false;
            } catch (CoapException e) {
                // If not found, we've reached the last sample
                if (e.getCode() == ResponseCode.NOT_FOUND) {
                    log.log(Level.INFO, "No more samples available");
                    return;
                }
                throw e;
            }

            log.log(Level.INFO, "Got sample with id {0}", sample.getId());

            // Substring strips the aquare backets from around the IPv6 address
            saveSample(dir, uri.getHost().substring(1, uri.getHost().length() - 1), sample);

            deleteSample(getURI(uri, sample.getId()));
            log.log(Level.INFO, "Sample {0} deleted from node", sample.getId());
        }

        @Override
        public boolean shouldKeepProcessingNode() {
            // We have more stuff to do with the node if we need to grab all and we haven't reached the end
            return shouldProcessAll && !hasReachedEnd;
        }
    }

    /**
     * Decode operation. Decodes a sample.
     */
    @Parameters(commandDescription = "Decode a delimited protocol buffer encoded sample")
    public static class Decode extends DecodeOperation {

        private static final String SAMPLE_START = "+++SERIALDUMP+++SAMPLE+++START+++";
        private static final String SAMPLE_END = "+++SERIALDUMP+++SAMPLE+++END+++";

        @Parameter(names = {"-d", "--destination"}, description = "Directory in which to output protocol buffer encoded samples. Only supported with '--is-serial-dump'")
        private String dir;

        @Override
        protected void decode(byte[] data, String nodeId) throws IOException {
            Sample sample = Sample.parseDelimitedFrom(new ByteArrayInputStream(data));

            if (dir == null) {
                log.log(Level.INFO, "Decoded sample to \n{0}", sampleToString(sample));
            } else {
                // Need to make the node id into a dummy ipv6 address
                saveSample(dir, "dead:beef::" + nodeId, sample);
            }
        }

        @Override
        protected String startMarker() {
            return SAMPLE_START;
        }

        @Override
        protected String endMarker() {
            return SAMPLE_END;
        }
    }

    /**
     * Get a sample from a URI.
     * @param uri The URI to get a sample from.
     * @return The Sample decoded at the URI.
     * @throws IOException If we fail to communicate with the node, or we fail to decode the sample we got.
     */
    protected static Sample getSample(URI uri) throws IOException {
        CoapClient client = new CoapClient(uri);
        log.log(Level.FINE, "Attempting to get sample from: {0}", client.getURI());

        CoapResponse response = client.get();
        if (response != null && response.isSuccess()) {
            return Sample.parseDelimitedFrom(new ByteArrayInputStream(response.getPayload()));
        }

        throw new CoapException(uri, Method.GET, response, "Unable to get sample");
    }

    /**
     * Delete a sample from a URI.
     * @param uri The URI of the sample.
     * @throws IOException If we fail to communicate with the node, or we fail to decode the sample we got.
     */
    protected static void deleteSample(URI uri) throws IOException {
        CoapClient client = new CoapClient(uri);

        log.log(Level.FINE, "Attempting to delete sample from: {0}", client.getURI());

        CoapResponse response = client.delete();
        if (response != null && response.isSuccess()) {
            return;
        }

        throw new CoapException(uri, Method.DELETE, response, "Failed to delete Sample");
    }

    /**
     * Save a sample as a protocol buffer
     * @param dir The directory to create the file in.
     * @param suffix A suffix to append to the filename.
     * @param sample The sample to save.
     * @throws IOException If an error occurs encoding the sample, or writing to the file.
     */
    protected static void saveSample(String dir, String suffix, Sample sample) throws IOException {
        File file = new File(dir + System.nanoTime() + "_" + suffix);

        try (FileOutputStream fileStream = new FileOutputStream(file)) {
            sample.writeDelimitedTo(fileStream);
            fileStream.flush();
            log.log(Level.INFO, "Saved sample to file {0}", file);
        }
    }

    @Override
    protected void processNode(URI uri, InetAddress nodeAddr) throws IOException {
        processSample(getURI(uri, sampleId));
    }

    /**
     * Process a given sample. Returns true if in the case of multiple samples the ID should be decremented.
     * @param uri
     * @throws IOException
     */
    public abstract void processSample(URI uri) throws IOException;

    /**
     * Get the URI associated with a given sample ID.
     * @param base The base URI of the node.
     * @param sampleId The sample ID.
     * @return The URI representing the sample with that ID on that node.
     */
    protected URI getURI(URI base, int sampleId) {
        return base.resolve(sampleId == LATEST_SAMPLE ? "" : Integer.toString(sampleId));
    }

    @Override
    public String getRessource() {
        return RESSOURCE;
    }

    /**
     * Get a String representation of a sample.
     * @param config The sample
     * @return A string representing the sample, properly formatted.
     */
    private static String sampleToString(Sample sample) throws IOException {
        return ProtoBufUtils.toString(sample, sampleOverrideMap);
    }
}
