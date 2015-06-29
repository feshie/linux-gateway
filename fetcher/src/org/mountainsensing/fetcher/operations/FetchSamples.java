package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.Parameters;
import java.io.IOException;
import java.net.URI;

/**
 *
 */
@Parameters(commandDescription = "Get sample(s) from the node(s), decode them, delete them from the node(s), and output them in a directory")
public class FetchSamples extends SampleOperation {

    @Override
    public boolean processNode(URI uri, int timeout) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
