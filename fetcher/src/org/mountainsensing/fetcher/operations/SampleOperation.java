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

    private static final String RESSOURCE = "sample";
    
    @Parameter(names = {"-s", "--sample-id"}, description = "Sample id. 0 for latest.")
    private int id = LATEST_SAMPLE;
        
    @Parameters(commandDescription = "Get samples from the node(s)")
    public static class Get extends SampleOperation {
        
        @Override
        public void processNode(URI uri, int timeout) throws IOException {
            Sample sample = getSample(uri);
            System.out.println(sample);
        }
    }

    @Parameters(commandDescription = "Delete sample(s) from the node(s)")
    public static class Delete extends SampleOperation {

        @Override
        public void processNode(URI uri, int timeout) throws IOException {
            deleteSample(uri); 
        }
    }

    @Parameters(commandDescription = "Get sample(s) from the node(s), decode them, delete them from the node(s), and output them in a directory")
    public static class Fetch extends SampleOperation {

        @Parameter(names = {"-d", "--destination"}, description = "Directory in which to output protocol buffer encoded samples")
        private String dir = "/ms/queue/";

        @Override
        public void processNode(URI uri, int timeout) throws IOException {
            Sample sample = getSample(uri);
            
            // Substring strips the aquare backets from around the IPv6 address
            File file = new File(dir + System.currentTimeMillis() + "_" + uri.getHost().substring(1, uri.getHost().length() - 1));
            try (FileOutputStream fileStream = new FileOutputStream(file)) {
                sample.writeDelimitedTo(fileStream);
                fileStream.flush();
                System.out.println("Sample saved to file: " + file.toString());
            }
            
            deleteSample(uri, sample.getId());
        }
    }
      
    public Sample getSample(URI uri) throws IOException {
        return getSample(uri, id); 
    }
    
    public Sample getSample(URI uri, int sampleId) throws IOException {
        CoapClient client = new CoapClient(getURI(uri, sampleId));
        //client.setTimeout(timeout);
        System.out.println("Attempting to get sample from: " + client.getURI());
        
        CoapResponse response = client.get();
        if (response != null && response.isSuccess()) {
            return Sample.parseDelimitedFrom(new ByteArrayInputStream(response.getPayload()));
        }

        throw new IOException(); 
    }
    
    public void deleteSample(URI uri) throws IOException {
        deleteSample(uri, id);
    }
    
    public void deleteSample(URI uri, int sampleId) throws IOException {
        CoapClient client = new CoapClient(getURI(uri, sampleId));
        
        System.out.println("Attempting to delete sample from: " + client.getURI());

        CoapResponse response = client.delete();
        if (response != null && response.isSuccess()) {
            return; 
        }

        throw new IOException();
    }
    
    private String getURI(URI base, int sampleId) {
        return base.toString() + (sampleId == LATEST_SAMPLE ? "" : Integer.toString(sampleId));
    }
    
    @Override
    public String getRessource() {
        return RESSOURCE;
    }
}
