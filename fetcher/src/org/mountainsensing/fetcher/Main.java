package org.mountainsensing.fetcher;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;

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
			
			// input URI from command line arguments
			try {
				uri = new URI(args[0]);
			} catch (URISyntaxException e) {
				System.err.println("Invalid URI: " + e.getMessage());
				System.exit(-1);
			}
			
			CoapClient client = new CoapClient(uri);

			CoapResponse response = client.get();
			
			if (response!=null) {
				
				System.out.println(response.getResponseText());
				
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