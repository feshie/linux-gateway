/**
 * ping a node
 * Arthur Fabre, University of Southampton, 2015
 * mountainsensing.org
 */
package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.Parameters;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.californium.core.CoapClient;
import org.mountainsensing.fetcher.net.NodeAddress;

/**
 * Operation for pinging nodes (using CoAP).
 */
@Parameters(commandDescription = "Send a CoAP ping packet to the node(s)")
public class PingOperation extends NodeOperation {

    private static final Logger log = Logger.getLogger(SampleOperation.class.getName());

    @Override
    public String getRessource() {
        // We don't need a ressource for ping
        return "";
    }

    @Override
    protected void processNode(URI uri, NodeAddress nodeAddr) throws IOException {
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
