package org.mountainsensing.fetcher;

import com.beust.jcommander.Parameter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Common class for all operations.
 */
public abstract class Operation {

    /**
     * 
     */
    @Parameter(description = "node(s)", required = true, hidden = true)
    private List<String> nodes = new ArrayList<String>(); 
    
    /**
     * 
     * @return 
     */
    public abstract String getRessource();
    
    /**
     * 
     * @param uri
     * @param timeout
     * @return 
     * @throws java.io.IOException 
     */
    public abstract void processNode(URI uri) throws IOException;

    protected List<String> getNodes() {
        return nodes;   
    }
}
