/**
 * node operations including retries
 * Arthur Fabre, University of Southampton, 2015
 * mountainsensing.org
 */
package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.mountainsensing.fetcher.CoapException;
import org.mountainsensing.fetcher.Operation;
import org.mountainsensing.fetcher.net.MalformedHostException;
import org.mountainsensing.fetcher.net.NodeAddress;

/**
 * Common class / interface for all operations.
 * An operation is something that can be done to a node, such as getting all it's samples or setting it's config.
 * Every operation becomes a separate command line command, and can declare it's own arguments.
 */
public abstract class NodeOperation extends Operation {

    private static final Logger log = Logger.getLogger(NodeOperation.class.getName());

    /**
     * Californium key for the timeout property.
     */
    private static final String COAP_TIMEOUT_KEY = "ACK_TIMEOUT";

    /**
     * Protocol to use for communication with nodes.
     */
    private static final String PROTOCOL = "coap";

    /**
     * The list of nodes to process.
     */
    @Parameter(description = "node(s)", validateWith = NodeValidator.class, required = true)
    private List<String> nodes = new ArrayList<>();

    /**
     * Validator to check a String is a valid representation of a node (IPv4, Ipv6, or hostname).
     */
    public static class NodeValidator implements IParameterValidator {
        @Override
        public void validate(String name, String value) throws ParameterException {
            if (!NodeAddress.isValid(value)) {
                throw new ParameterException("\'" + value + "\' is not a valid IPv{4,6} address, or hostname");
            }
        }
    }

    /**
     * Set the CoAP timeout.
     * @param timeout The timeout in seconds.
     */
    private void setTimeout(int timeout) {
        // Don't save / read from the Californium.properties file
        NetworkConfig config = NetworkConfig.createStandardWithoutFile();
        // Nedd to scale the timeout from seconds to ms
        config.setInt(COAP_TIMEOUT_KEY, timeout * 1000);
        NetworkConfig.setStandard(config);
    }

    /**
     * Get the relative CoAP resource used by this Operation.
     * @return A string representing the resource, without leading or trailing slashes.
     */
    protected abstract String getRessource();

    /**
     * Process a node with this operation.
     * @param uri The URI representing the node / URI.
     * @param nodeAddr The IP address of the node.
     * @throws org.mountainsensing.fetcher.CoapException If a CoAP I/O error occurs.
     * @throws java.io.IOException If an I/O error occurs other than CoAP.
     */
    protected abstract void processNode(URI uri, NodeAddress nodeAddr) throws CoapException, IOException;

    /**
     * Test if the node in the last call to processNode requires further processing.
     * @return
     */
    protected boolean shouldKeepProcessingNode() {
        return false;
    }

    /**
     * Get the IP addresses from a list of nodes.
     * @param nodes A list of nodes, which can be either literal IPv{4,6} addresses, or hostnames.
     * @return A list of IP addresses, with any unresolvable / unparseable nodes discarded.
     */
    private List<NodeAddress> getAddresses(List<String> nodes) {
        List<NodeAddress> addresses = new ArrayList<>();

        for (String node : nodes) {
            setContext(node);
            try {
                addresses.add(new NodeAddress(node));
            } catch (UnknownHostException e) {
                log.log(Level.WARNING, "Unable to resolve address. Discarding node.", e);
            } catch (MalformedHostException e) {
                // Shouldn't happen as they are validated before hand
                log.log(Level.SEVERE, "Unexpected unparseable IP - bug in validation code? " + node, e);
            } finally {
                clearContext();
            }
        }

        return addresses;
    }

    @Override
    public void perform(int timeout, int retries) {
        setTimeout(timeout);

        List<NodeAddress> nodeAddrs = getAddresses(nodes);

        for (NodeAddress node : nodeAddrs) {
            setContext(node.toString());

            URI uri;
            try {
                // This will add ://, and insert square brackets around IPv6 addresses. Trailing slash to make it easy to append to.
                uri = new URI(PROTOCOL, node.getAddress().getHostAddress(), "/" + getRessource() + "/", null);
            } catch (URISyntaxException e) {
                log.log(Level.WARNING, e.getMessage(), e);
                continue;
            }

            int retryAttempt = 0;

            do {
                try {
                    processNode(uri, node);
                    // Reset the retry attempt on success
                    retryAttempt = 0;
                    continue;

                } catch (CoapException e) {
                    log.log(Level.WARNING, e.getMessage(), e);

                    // If the error is our fault (bad request, file not found..), don't retry
                    if (e.isClientError()) {
                        retryAttempt = 0;
                        continue;
                    }

                } catch (IOException e) {
                    log.log(Level.WARNING, e.getMessage(), e);
                }

                retryAttempt++;

            /*
                Keep going as long as either:
                        retryAttempt != 0 -> the most recent operation didn't succed
                    and
                        retryAttempt < RETRIES -> we still have attempts to try again left
                or
                        retryAttempt == 0 -> the most recent operation did succeed
                    and
                        shouldKeepProcessingNode() -> that operation needs to process the node more
            */
            } while ((retryAttempt != 0 && retryAttempt < retries) || (retryAttempt == 0 && shouldKeepProcessingNode()));

            clearContext();
        }
    }
}
