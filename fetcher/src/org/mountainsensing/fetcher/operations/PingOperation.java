package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.Parameters;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.californium.core.CoapClient;

/**
 * Operation for pinging nodes (using CoAP).
 */
public abstract class PingOperation extends NodeOperation {

    private static final Logger log = Logger.getLogger(SampleOperation.class.getName());

    @Override
    public String getRessource() {
        return "";
    }
    /**
     * 
     */
    @Parameters(commandDescription = "Send a CoAP ping packet to the node(s)")
    public static class Ping extends PingOperation {

        @Override
        public void processNode(URI uri) throws IOException {
            CoapClient client = new CoapClient(uri.getHost());

            long startTime = System.currentTimeMillis();
            
            if (client.ping()) {
                
                long endTime = System.currentTimeMillis();
                
                log.log(Level.INFO, "Node is up, RTT {0}ms", endTime -startTime);
                
            } else {
                log.log(Level.INFO, "No response");
            }
        }
    }
}