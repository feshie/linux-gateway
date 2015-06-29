package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.ByteArrayInputStream;
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

        @Override
        public void processNode(URI uri, int timeout) throws IOException {
            getSample(uri);
            deleteSample(uri);
        }
    }
    
    public Sample getSample(URI uri) throws IOException {
        CoapClient client = new CoapClient(uri);
        //client.setTimeout(timeout);
        System.out.println("Attempting to get sample from: " + uri.toString());
        
        CoapResponse response = client.get();
        if (response != null && response.isSuccess()) {
            return Sample.parseDelimitedFrom(new ByteArrayInputStream(response.getPayload()));
        }

        throw new IOException();
    }

    public void deleteSample(URI uri) throws IOException {
       /*
             System.out.println("Attempting to delete samples " + args[0]);

             CoapClient client = new CoapClient();
             //ByteArrayOutputStream out = new ByteArrayOutputStream();
             //config.writeDelimitedTo(out);
            
             //CoapResponse response = client.post(out.toByteArray(), MediaTypeRegistry.APPLICATION_OCTET_STREAM);

             //if (response != null && response.isSuccess()) {
             //    System.out.println("Posted config!");
             //}
            
             CoapResponse response;
            
             while (true) {
             client.setURI(PREFIX + args[0] + SUFFIX);
                
             response = client.get();
                
             if (response != null && response.isSuccess()) {
             Sample sample = Sample.parseDelimitedFrom(new ByteArrayInputStream(response.getPayload()));
             //config = SensorConfig.parseDelimitedFrom(new ByteArrayInputStream(response.getPayload()));
                    
             System.out.println();

             //System.out.println(config);
                
             System.out.println(sample);

             client.setURI(PREFIX + args[0] + SUFFIX + "/" + Integer.toString(sample.getId()));
             response = client.delete();

             if (response.isSuccess()) {
             System.out.println("Succesfully deleted Sample " + Integer.toString(sample.getId()));
             }
             }
             }*/ 
    }

    @Override
    public String getRessource() {
        return RESSOURCE;
    }
}
