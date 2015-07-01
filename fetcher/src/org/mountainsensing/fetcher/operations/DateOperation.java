package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.Parameters;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;
import org.mountainsensing.fetcher.Operation;

/**
 *
 */
public abstract class DateOperation extends Operation {
    
    private static final Logger log = Logger.getLogger(SampleOperation.class.getName());
    
    static final String RESSOURCE = "date";
    
    /**
     *
     */
    @Parameters(commandDescription = "Get the date from the node(s)")
    public static class Get extends DateOperation {

        @Override
        public void processNode(URI uri) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
    
    /**
     *
     */
    @Parameters(commandDescription = "Set the date of the node(s)")
    public static class Set extends DateOperation {

        @Override
        public void processNode(URI uri) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    @Override
    public String getRessource() {
        return RESSOURCE;
    }
}
