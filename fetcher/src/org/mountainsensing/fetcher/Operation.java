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
     * @throws org.mountainsensing.fetcher.CoapException If a CoAP I/O error occurs.
     * @throws java.io.IOException If an I/O error occurs other than CoAP.
     */
    public abstract void processNode(URI uri) throws CoapException, IOException;

    protected List<String> getNodes() {
        return nodes;   
    }
}
