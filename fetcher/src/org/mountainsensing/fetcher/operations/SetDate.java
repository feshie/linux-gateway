package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.Parameters;
import java.io.IOException;
import java.net.URI;

/**
 *
 */
@Parameters(commandDescription = "Set the date of the node(s)")
public class SetDate extends DateOperation {

    @Override
    public boolean processNode(URI uri, int timeout) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
