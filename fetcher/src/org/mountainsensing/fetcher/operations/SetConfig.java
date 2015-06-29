package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.Parameters;
import java.io.IOException;
import java.net.URI;
import org.mountainsensing.fetcher.Operation;

/**
 *
 */
@Parameters(commandDescription = "Set the configuration of the node(s)")
public class SetConfig extends ConfigOperation {

    @Override
    public boolean processNode(URI uri, int timeout) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
