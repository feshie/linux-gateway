package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.Parameters;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.mountainsensing.pb.Readings.Sample;

/**
 *
 */
@Parameters(commandDescription = "Get samples from the node(s)")
public class GetSample extends SampleOperation {
    
    @Override
    public boolean processNode(URI uri, int timeout) throws IOException {
        CoapClient client = new CoapClient(uri);
        //client.setTimeout(timeout);
        System.out.println("Attempting to get sample from: " + uri.toString());
        
        CoapResponse response = client.get();
		if (response != null && response.isSuccess()) {
            Sample sample = Sample.parseDelimitedFrom(new ByteArrayInputStream(response.getPayload()));
            System.out.println(sample);
            return true;
        }

        return false;
    }
}
