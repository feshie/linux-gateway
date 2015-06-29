package org.mountainsensing.fetcher;

import com.beust.jcommander.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Options {
    /**
     * Dummy parameter to make usage clearer. Real nodes parameter is in Operation.
     */
    @Parameter(descriptionKey = "nodes", description = "node(s)")
    private List<String> nodes = new ArrayList<String>();

    @Parameter(names = {"-p", "--prefix"}, description = "/96 IPv6 prefix for the nodes")
    private String prefix = "aaaa::c30c:0:0:";

    @Parameter(names = {"-t", "--timeout"}, description = "CoAP timeout in seconds")
    private int timeout = 10;

    @Parameter(names = {"-r", "--retries"}, description = "Number of retries before giving up on a node")
    private int retries = 3;

    @Parameter(names = {"-h", "--help"}, description = "Show usage help and exit", help = true)
    private boolean help;

    /**
     * 
     * @return 
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * 
     * @return 
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * 
     * @return 
     */
    public int getRetries() {
        return retries;
    }
    
    /**
     * 
     * @return 
     */
    public boolean shouldShowHelp() {
        return help;
    }
}
