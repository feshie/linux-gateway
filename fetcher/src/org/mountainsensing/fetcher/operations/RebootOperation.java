package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.Parameters;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.mountainsensing.fetcher.Operation;

/**
 *
 */
public abstract class RebootOperation extends Operation {

    private static final Logger log = Logger.getLogger(SampleOperation.class.getName());

    static final String RESSOURCE = "reboot";

    /**
     *
     */
    @Parameters(commandDescription = "Force the node(s) to reboot immediately. This is a blind operation, the node(s) are not able to confirm reception of the command.")
    public static class Force extends RebootOperation {

        @Override
        public void processNode(URI uri, int timeout) throws IOException {
            CoapClient client = new CoapClient(uri);
            // Rediculously short timeout - we don't actually expect a response.
            client.setTimeout(1000);
            client.useNONs();
            client.post(new byte[0], MediaTypeRegistry.UNDEFINED);
        }
    }

    /**
     *
     */
    @Parameters(commandDescription = "Get the reboot count of the node(s)")
    public static class Get extends RebootOperation {

        @Override
        public void processNode(URI uri, int timeout) throws IOException {
            CoapClient client = new CoapClient(uri);
            CoapResponse response = client.get();

            if (response != null && response.isSuccess()) {
                log.log(Level.INFO, "Reboot count is {0}", response.getResponseText());
                return;
            }

            log.log(Level.FINER, "Failed to get reboot count from {0}", client.getURI());
            throw new IOException("Failed to get reboot count from {0}: " + client.getURI());
        }
    }

    @Override
    public String getRessource() {
        return RESSOURCE;
    }
}