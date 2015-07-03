package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.Parameters;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.mountainsensing.fetcher.Operation;

/**
 *
 */
public abstract class RouteOperation extends Operation {
    private static final Logger log = Logger.getLogger(SampleOperation.class.getName());

    private static final String RESSOURCE = "routes";

    /**
     *
     */
    @Parameters(commandDescription = "Get the parent node, neighbours and routing table from the node(s)")
    public static class Get extends RouteOperation {

        @Override
        public void processNode(URI uri) throws IOException {
            CoapClient client = new CoapClient(uri);
            CoapResponse response = client.get();

            if (response != null && response.isSuccess()) {
                String[] entries = response.getResponseText().split("\n");
                
                String parent = entries[0];
                
                List<String> neighbours = new ArrayList<>();
                // Use a linked hash map to keep the ordering given by the node
                Map<String, String> routes = new LinkedHashMap<>();
                
                for (String entry : Arrays.copyOfRange(entries, 1, entries.length)) {
                    if (entry.isEmpty()) {
                        continue;
                    } 
                    
                    if (entry.contains("@")) {
                        routes.put(entry.split("@")[0], entry.split("@")[1]);
                    } else {
                        neighbours.add(entry);
                    }
                }
                
                log.log(Level.INFO, "Got route info: \nParent: {0}\nNeighbours: {1}\nRoutes: {2}", new Object[] {parent, neighbours, routes});
                return;
            }

            log.log(Level.FINER, "Failed to get routes from {0}", client.getURI());
            throw new IOException("Failed to get routes from: " + client.getURI());
        }
    }

    @Override
    public String getRessource() {
        return RESSOURCE;
    }
}
