/**
 * reboot a node using CoAP request
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
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.mountainsensing.fetcher.CoapException;
import org.mountainsensing.fetcher.CoapException.Method;
import org.mountainsensing.fetcher.net.NodeAddress;

/**
 * Operations for rebooting / getting reboot count from the nodes.
 */
public abstract class RebootOperation extends NodeOperation {

    private static final Logger log = Logger.getLogger(SampleOperation.class.getName());

    static final String RESSOURCE = "reboot";

    /**
     * Operation to reboot a node.
     */
    @Parameters(commandDescription = "Force the node(s) to reboot immediately. This is a blind operation, the node(s) are not able to confirm reception of the command.")
    public static class Force extends RebootOperation {

        @Override
        protected void processNode(URI uri, NodeAddress nodeAddr) throws IOException {
            CoapClient client = new CoapClient(uri);
            // Rediculously short timeout - we don't actually expect a response.
            client.setTimeout(1000);
            client.useNONs();
            client.post(new byte[0], MediaTypeRegistry.UNDEFINED);
        }
    }

    /**
     * Operation to get the reboot counter.
     */
    @Parameters(commandDescription = "Get the reboot count of the node(s)")
    public static class Get extends RebootOperation {

        @Override
        protected void processNode(URI uri, NodeAddress nodeAddr) throws IOException {
            CoapClient client = new CoapClient(uri);
            CoapResponse response = client.get();

            if (response != null && response.isSuccess()) {
                log.log(Level.INFO, "Reboot count is {0}", response.getResponseText());
                return;
            }

            throw new CoapException(uri, Method.GET, response, "Failed to get reboot count");
        }
    }

    @Override
    public String getRessource() {
        return RESSOURCE;
    }
}
