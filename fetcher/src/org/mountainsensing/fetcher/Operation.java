package org.mountainsensing.fetcher;

import com.beust.jcommander.Parameter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Common class / interface for all operations.
 * An operation is something that can be done to a node, such as getting all it's samples or setting it's config.
 * Every operation becomes a separate command line command, and can declare it's own arguments.
 */
public abstract class Operation {

    /**
     * The list of nodes to process.
     * These should be part of the Main options, but due to a bug in JCommander have to be decalared as arguments per Operation.
     * Hidden to make the help output clearer.
     */
    @Parameter(description = "node(s)", required = true, hidden = true)
    private List<String> nodes = new ArrayList<String>(); 

    /**
     * Get the relative CoAP resource used by this Operation.
     * @return A string representing the resource, without leading or trailing slashes.
     */
    public abstract String getRessource();

    /**
     * Process a node represented by a URI.
     * @param uri
     * @throws org.mountainsensing.fetcher.CoapException If a CoAP I/O error occurs.
     * @throws java.io.IOException If an I/O error occurs other than CoAP.
     */
    public abstract void processNode(URI uri) throws CoapException, IOException;

    /**
     * Test if the node in the last call to processNode requires further processing.
     * @return 
     */
    public boolean shouldKeepProcessingNode() {
        return false;
    }

    /**
     * 
     * @return 
     */
    protected List<String> getNodes() {
        return nodes;   
    }
}
