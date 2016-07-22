/**
 * node operations including retries
 * Arthur Fabre, University of Southampton, 2015
 * mountainsensing.org
 */
package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import java.io.IOException;
import java.net.InetAddress;
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
     * The amount of retries.
     */
    private static int retries;

    /**
     * The list of nodes to process.
     */
    @Parameter(description = "node(s)", converter=InetAddressConverter.class, required = true)
    private List<InetAddress> nodes = new ArrayList<>();

    /**
     * Convert a String to an InetAddress.
     * This preforms a DNS lookup if required.
     */
    public static class InetAddressConverter implements IStringConverter<InetAddress> {
        @Override
        public InetAddress convert(String value) {
            try {
                return InetAddress.getByName(value);
            } catch (UnknownHostException e) {
                // This should never happen, as JCommander calls the validator first
                throw new ParameterException("Error resolving IP of node " + value, e);
            }
        }
    }

    /**
     * Initialise the NodeOperations.
     * @param retries The amount of times to try before giving up.
     * @param timeout The timeout in seconds.
     */
    public static void init(int retries, int timeout) {
        NodeOperation.retries = retries;

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
     * @throws org.mountainsensing.fetcher.CoapException If a CoAP I/O error occurs.
     * @throws java.io.IOException If an I/O error occurs other than CoAP.
     */
    protected abstract void processNode(URI uri) throws CoapException, IOException;

    /**
     * Test if the node in the last call to processNode requires further processing.
     * @return
     */
    protected boolean shouldKeepProcessingNode() {
        return false;
    }

    @Override
    public void perform() {
        for (InetAddress node : nodes) {
            setContext(node.toString());

            URI uri;
            try {
                // This will add ://, and insert square brackets around IPv6 addresses. Trailing slash to make it easy to append to.
                uri = new URI(PROTOCOL, node.getHostAddress(), "/" + getRessource() + "/", null);
            } catch (URISyntaxException e) {
                log.log(Level.WARNING, e.getMessage(), e);
                continue;
            }

            int retryAttempt = 0;

            do {
                try {
                    processNode(uri);
                    // Reset the retry attempt on success
                    retryAttempt = 0;
                    continue;
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
