package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.google.protobuf.Descriptors.FieldDescriptor;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.mountainsensing.fetcher.CoapException;
import org.mountainsensing.fetcher.CoapException.Method;
import org.mountainsensing.fetcher.utils.EpochDate;
import org.mountainsensing.fetcher.Main;
import org.mountainsensing.fetcher.Operation;
import org.mountainsensing.fetcher.utils.Pair;
import org.mountainsensing.fetcher.utils.UTCDateFormat;
import org.mountainsensing.pb.Readings.Sample;
import org.mountainsensing.pb.Rs485Message.Rs485;

/**
 *
 */
public abstract class SampleOperation extends Operation {
    
    private static final Logger log = Logger.getLogger(SampleOperation.class.getName());
    
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

    @Parameters(commandDescription = "Get samples from the node(s)")
    public static class Get extends SampleOperation {
        
        @Override
        public void processSample(URI uri) throws IOException {
            Sample sample = getSample(uri);
            log.log(Level.INFO, "Got sample: \n{0}", sampleToString(sample));

            if (sample.hasAVR()) {
                try (InputStream rsin = new ByteArrayInputStream(sample.getAVR().toByteArray())) {
                    Rs485 rs485 = Rs485.parseFrom(rsin);
                    log.log(Level.INFO, "RS485: \n{0}", rs485);
                }
            }
        }
    }

    @Parameters(commandDescription = "Delete sample(s) from the node(s)")
    public static class Delete extends SampleOperation {

        @Override
        public void processSample(URI uri) throws IOException {
            deleteSample(uri);
            log.log(Level.INFO, "Deleted sample: {0}", uri);
        }
    }

    @Parameters(commandDescription = "Get sample(s) from the node(s), decode them, delete them from the node(s), and output them in a directory")
    public static class Grab extends SampleOperation {

        @Parameter(names = {"-d", "--destination"}, description = "Directory in which to output protocol buffer encoded samples")
        private String dir = "/ms/queue/";

        @Parameter(names = {"-a", "--all"}, validateWith = SampleExclusionValidator.class, description = "Grab all samples from the node(s). This cannot be used in conjunction with --sample")
        private boolean shouldProcessAll = false;

        private void grabSample(URI uri) throws IOException, CoapException {
            Sample sample = getSample(uri);

            log.log(Level.INFO, "Got sample with id {0} from node {1}", new Object[] {sample.getId(), uri.getHost()});
            
            // Substring strips the aquare backets from around the IPv6 address
            File file = new File(dir + System.currentTimeMillis() + "_" + uri.getHost().substring(1, uri.getHost().length() - 1));
            try (FileOutputStream fileStream = new FileOutputStream(file)) {
                sample.writeDelimitedTo(fileStream);
                fileStream.flush();
                log.log(Level.INFO, "Saved sample to file {0}", file);
            }
            
            deleteSample(getURI(uri, sample.getId()));
            log.log(Level.INFO, "Sample {0} deleted from node", sample.getId());
            // Always just get the latest sample, seeing as we've deleted the previous one.
        }

        @Override
        public void processSample(URI uri) throws IOException {
            if (shouldProcessAll) {
                int retryAttempt = 0;

                // Do the right amount of retries. Retries will be reset every time we succesfully get something.
                while (retryAttempt < Main.getOptions().getRetries()) {
                    try {
                        grabSample(uri);
                        // Reset the retry attempt on success
                        retryAttempt = 0;
                        break;
                    } catch (CoapException e) {
                        // If not found, we've reached the last sample
                        if (e.getCode() == ResponseCode.NOT_FOUND) {
                            log.log(Level.INFO, "No more samples available");
                            break;
                        }
                        log.log(Level.WARNING, e.getMessage() + ". Got CoAP response " + e.getCode() + " using " + e.getMethod() + " on " + e.getURI(), e);
                    } catch (IOException e) {
                        log.log(Level.WARNING, e.getMessage(), e);
                    }
                    retryAttempt++;
                }

            } else {
                grabSample(uri);
            }
        }
    }

    /**
     * 
     * @param uri
     * @return
     * @throws IOException 
     */
    public Sample getSample(URI uri) throws IOException {
        CoapClient client = new CoapClient(uri);
        log.log(Level.FINE, "Attempting to get sample from: {0}", client.getURI());

        CoapResponse response = client.get();
        if (response != null) {
            return Sample.parseDelimitedFrom(new ByteArrayInputStream(response.getPayload()));
        }

        throw new CoapException(uri, Method.GET, response, "Unable to get sample");
    }
    
    public void deleteSample(URI uri) throws IOException {
        CoapClient client = new CoapClient(uri);
        
        log.log(Level.FINE, "Attempting to delete sample from: {0}", client.getURI());

        CoapResponse response = client.delete();
        if (response != null && response.isSuccess()) {
            return; 
        }

        throw new CoapException(uri, Method.DELETE, response, "Failed to delete Sample");
    }

    @Override
    public void processNode(URI uri) throws IOException {
        processSample(getURI(uri, sampleId));
    }
    
    /**
     * Process a given sample. Returns true if in the case of multiple samples the ID should be decremented.
     * @param uri
     * @throws IOException 
     */
    public abstract void processSample(URI uri) throws IOException;
    
    protected int getNextSample(int currentSample) {
        return currentSample - 1;
    }
    
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
    private static String sampleToString(Sample sample) {
        String result = new String();
        for (FieldDescriptor descriptor : sample.getAllFields().keySet()) {

            result += descriptor.getName() + ": ";

            // Field 1 is the sampling time, we need to handle it sepcially to print epoch + human readable time.
            // Index here is 0 indexed, so field 0.
            if (descriptor.getIndex() == 0) {
                result += Long.toString(sample.getTime());
                result += " aka ";
                // Scale date from s to ms
                result += new UTCDateFormat().format(new EpochDate(sample.getTime()));

            // Print all the rest normally
            } else {
                result += sample.getAllFields().get(descriptor).toString();
            }

            result += System.lineSeparator();
        }
        return result;
    }
}