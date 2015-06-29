package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.mountainsensing.fetcher.Operation;
import org.mountainsensing.pb.Readings.Sample;

/**
 *
 */
public abstract class SampleOperation extends Operation {

    private static final int LATEST_SAMPLE = 0;

    private static final int NO_MORE_SAMPLES = -1;

    private static final String RESSOURCE = "sample";
    
    @Parameter(names = {"-s", "--sample-id"}, description = "Sample id. 0 for latest sample.")
    private int sampleId = LATEST_SAMPLE;

    @Parameter(names = {"-a", "--all"}, description = "Process all samples from the node(s). This overrides any sample-id set.")
    private boolean shouldProcessAll = false;
        
    @Parameters(commandDescription = "Get samples from the node(s)")
    public static class Get extends SampleOperation {
        
        @Override
        public int processSample(URI uri, int timeout) throws IOException {
            Sample sample = getSample(uri);
            System.out.println(sample);

            // Subtract 1 from the id to try and find a previous sample
            return sample.getId() - 1;
        }
    }

    @Parameters(commandDescription = "Delete sample(s) from the node(s)")
    public static class Delete extends SampleOperation {

        @Override
        public int processSample(URI uri, int timeout) throws IOException {
            deleteSample(uri);
            // We don't support deleting ranges / all samples
            return NO_MORE_SAMPLES;
        }
    }

    @Parameters(commandDescription = "Get sample(s) from the node(s), decode them, delete them from the node(s), and output them in a directory")
    public static class Fetch extends SampleOperation {

        @Parameter(names = {"-d", "--destination"}, description = "Directory in which to output protocol buffer encoded samples")
        private String dir = "/ms/queue/";

        @Override
        public int processSample(URI uri, int timeout) throws IOException {
            Sample sample = getSample(uri);
            
            // Substring strips the aquare backets from around the IPv6 address
            File file = new File(dir + System.currentTimeMillis() + "_" + uri.getHost().substring(1, uri.getHost().length() - 1));
            try (FileOutputStream fileStream = new FileOutputStream(file)) {
                sample.writeDelimitedTo(fileStream);
                fileStream.flush();
                System.out.println("Sample saved to file: " + file.toString());
            }
            
            deleteSample(getURI(uri, sample.getId()));
            // Always just get the latest sample, seeing as we've deleted the previous one.
            return LATEST_SAMPLE;
        }
    }
      
    public Sample getSample(URI uri) throws IOException {
        CoapClient client = new CoapClient(uri);
        //client.setTimeout(timeout);
        System.out.println("Attempting to get sample from: " + client.getURI());
        
        CoapResponse response = client.get();
        if (response != null && response.isSuccess()) {
            return Sample.parseDelimitedFrom(new ByteArrayInputStream(response.getPayload()));
        }

        throw new IOException(); 
    }
    
    public void deleteSample(URI uri) throws IOException {
        CoapClient client = new CoapClient(uri);
        
        System.out.println("Attempting to delete sample from: " + client.getURI());

        CoapResponse response = client.delete();
        if (response != null && response.isSuccess()) {
            return; 
        }

        throw new IOException();
    }

    @Override
    public void processNode(URI uri, int timeout) throws IOException {
        // If request, process all samples
        if (shouldProcessAll) {
            int nextSample = LATEST_SAMPLE;

            while (nextSample != NO_MORE_SAMPLES) {
                nextSample = processSample(getURI(uri, nextSample), timeout);
            }
        
        // Otherwise just process the requested sample.
        } else {
            processSample(getURI(uri, sampleId), timeout);
        }
    }
    
    /**
     * Process a given sample. Returns true if in the case of multiple samples the ID should be decremented.
     * @param uri
     * @param timeout
     * @return
     * @throws IOException 
     */
    public abstract int processSample(URI uri, int timeout) throws IOException;
    
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
}