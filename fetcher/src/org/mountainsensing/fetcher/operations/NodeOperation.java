package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.Parameter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
    private static final String PROTOCOL = "coap://";

    /**
     * The prefix to use for nodes.
     */
    private static String prefix;

    /**
     * The amount of retries.
     */
    private static int retries;

    /**
     * The list of nodes to process.
     * These should be part of the Main options, but due to a bug in JCommander have to be decalared as arguments per Operation.
     * Hidden to make the help output clearer.
     */
    @Parameter(description = "node(s)", required = true)
    private List<String> nodes = new ArrayList<>();
    
    /**
     * Initialise the NodeOperations.
     * @param prefix The IPv6 prefix/
     * @param retries The amount of times to try before giving up.
     * @param timeout The timeout in seconds.
     */
    public static void init(String prefix, int retries, int timeout) {
        NodeOperation.prefix = prefix;
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
        for (String node : nodes) {
            setContext(node);

            URI uri;
            try {
                uri = new URI(PROTOCOL + "[" + prefix + node + "]" + "/" + getRessource() + "/");
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
