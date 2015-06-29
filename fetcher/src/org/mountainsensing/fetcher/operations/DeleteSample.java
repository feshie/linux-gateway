package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.Parameters;
import java.io.IOException;
import java.net.URI;

/**
 *
 */
@Parameters(commandDescription = "Delete sample(s) from the node(s)")
public class DeleteSample extends SampleOperation {

    @Override
    public boolean processNode(URI uri, int timeout) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
