package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.Parameters;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;
import org.mountainsensing.fetcher.Operation;

/**
 *
 */
public abstract class ConfigOperation extends Operation {

    private static final Logger log = Logger.getLogger(SampleOperation.class.getName());
    
    public static final String RESSOURCE = "config";
    
    /**
     *
     */
    @Parameters(commandDescription = "Get the configuration from the node(s)")
    public static class Get extends ConfigOperation {

        @Override
        public void processNode(URI uri, int timeout) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    /**
     *
     */
    @Parameters(commandDescription = "Set the configuration of the node(s)")
    public static class Set extends ConfigOperation {

        @Override
        public void processNode(URI uri, int timeout) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    @Override
    public String getRessource() {
        return RESSOURCE;
    }
}
