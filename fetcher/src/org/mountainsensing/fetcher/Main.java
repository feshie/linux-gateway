package org.mountainsensing.fetcher;

import com.google.protobuf.InvalidProtocolBufferException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.mountainsensing.pb.Readings.Sample;

/**
 *
 * @author af1g12
 */
public class Main {
    
    public static void main(String[] args) {
        Logger californiumLogger = Logger.getLogger("org.eclipse.californium");
        californiumLogger.setLevel(Level.WARNING);
        URI uri = null; // URI parameter of the request
		
		if (args.length > 0) {
		    System.out.println("Attempting to get sample from node " + args[0]);
            
			// input URI from command line arguments
			try {
				uri = new URI("coap://[aaaa::c30c:0:0:" + args[0] + "]/sample");
			} catch (URISyntaxException e) {
				System.err.println("Invalid URI: " + e.getMessage());
				System.exit(-1);
			}
			
			CoapClient client = new CoapClient(uri);
			CoapResponse response = client.get();
			
			if (response != null) {
                Sample sample = null;
                
                try {
                    sample = Sample.parseFrom(response.getPayload());
                } catch (InvalidProtocolBufferException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
                
				System.out.println();

                System.out.println(sample);
				
			} else {
				System.out.println("No response received.");
			}
			
		} else {
			// display help
			System.out.println("Usage: " + Main.class.getSimpleName() + " URI");
			System.out.println("  URI: The CoAP URI of the remote resource to GET");
		}
	}
}