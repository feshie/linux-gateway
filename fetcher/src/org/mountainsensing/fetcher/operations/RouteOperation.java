/**
 * get routing info from nodes using CoAP
 * Arthur Fabre, University of Southampton, 2015
 * mountainsensing.org
 */
package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.mountainsensing.fetcher.CoapException;
import org.mountainsensing.fetcher.CoapException.Method;
import org.mountainsensing.fetcher.net.NodeAddress;
import org.mountainsensing.fetcher.utils.RouteGraph;

/**
 * Operation to get routing info from a node.
 */
@Parameters(commandDescription = "Get the parent node, neighbours and routing table from the node(s)")
public class RouteOperation extends NodeOperation {

    private static final Logger log = Logger.getLogger(SampleOperation.class.getName());

    private static final String RESSOURCE = "routes";

    /**
     * Size (in chars) of the IDs returned by the routes ressource.
     */
    private static final int NODE_ID_SIZE = 4;

    /**
     * All of the route information we've gathered.
     */
    private final Map<NodeAddress, RouteInfo> routes = new LinkedHashMap<>();

    /**
     * The routes page on the nodes only returns the last 2 bytes of the IPv6 address.
     * We can recover the full ip addresse's from keeping track of what nodes we've talked to.
     */
    private final Map<String, NodeAddress> realAddresses = new HashMap<>();

    @Parameter(names = {"-g", "--graph"}, description = "Write a DOT graph of routes / neighbors to this file")
    private String graphPath;

    /**
     * POD class to group routing info obtained from a node together.
     */
    private static class RouteInfo {
        /**
         * Neighbours of a node.
         */
        public List<String> neighbours = new ArrayList<>();

        /**
         * Routes a node has to a node, through another.
         * Use a linked hash map to keep the ordering given by the node
         */
        public Map<String, String> routes = new LinkedHashMap<>();

        /**
         * Parent of a node.
         */
        public String parent;
    }

    /**
     * Register a known node address.
     * This is used to keep track of what "ids" (2 bytes returned by the ressource) map to what real addresses.
     * @param addr The address processed.
     */
    private void registerNodeAddress(NodeAddress addr) {
        // ID of the node as used in the routes ressource
        String nodeId = addr.getAddress().getHostAddress().substring(addr.getAddress().getHostAddress().length() - NODE_ID_SIZE);

        // If the id is already known to us, but with a different address the graph is going to be messed up
        if (realAddresses.containsKey(nodeId) && !realAddresses.get(nodeId).equals(addr)) {
            log.log(Level.WARNING, "Nodes {0} and {1} have the same last {2} bytes. They will be treated as the same node in the graph.", new Object[] {addr, realAddresses.get(nodeId), NODE_ID_SIZE / 2});
            return;
        }

        realAddresses.put(nodeId, addr);
    }

    /**
     * Get the best String representation of a node we have.
     * Our order of preference is: hostname > full ipv6 > last bytes of ipv6
     * @param node The node to search for.
     * @return The best String representation of node available.
     */
    private String getRealAddressString(String node) {
        // If this node's address hasn't been seen before, it's short ID is the best we can do
        if (!realAddresses.containsKey(node)) {
            return node;
        }

        // Otherwise get the best String representation of it's full IPv6 address
        return nodeAddressToString(realAddresses.get(node));
    }

    /**
     * Get the best String representation of an IP address.
     * Our order of preference is hostname > full IP
     * @param addr The Address to use
     * @return The best String representation of the addr available.
     * 
     * @note This will only use cached hostname information, it will not make any reverse DNS queries.
     */
    private String nodeAddressToString(NodeAddress addr) {
        // We don't want to preform any DNS lookups if we were given IPs
        return addr.hasHostName() ? addr.getHostName() : addr.getAddress().getHostAddress();
    }

    @Override
    public void perform(int timeout, int retries) {
        // Get NodeOperation.perform() to talk to all the nodes
        super.perform(timeout, retries);

        // If we aren't writing a graph, we're done
        if (graphPath == null) {
            return;
        }

        try (OutputStream graphOut = new FileOutputStream(new File(graphPath))) {
            makeGraph().write(graphOut);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to write graph file: " + e.getMessage(), e);
        }
    }

    /**
     * Biuld a graph representing the routing information.
     * @return A graph
     */
    private RouteGraph makeGraph() {
        RouteGraph graph = new RouteGraph(RESSOURCE);

        // Add all the edges in, looking up "real" ip addresses as we find them
        for (NodeAddress node : routes.keySet()) {
            RouteInfo info = routes.get(node);
            String nodeId = nodeAddressToString(node);

            // Add the parent as a route
            graph.addRoute(nodeId, getRealAddressString(info.parent));

            for (String neighbour : info.neighbours) {
                graph.addNeighbour(nodeId, getRealAddressString(neighbour));
            }

            // Use the keys to add routes to the "via" nodes
            for (String route : info.routes.values()) {
                graph.addRoute(nodeId, getRealAddressString(route));
            }
        }

        return graph;
    }

    @Override
    protected void processNode(URI uri, NodeAddress nodeAddr) throws IOException {
        registerNodeAddress(nodeAddr);

        CoapClient client = new CoapClient(uri);
        CoapResponse response = client.get();

        if (response != null && response.isSuccess()) {
            String[] entries = response.getResponseText().split("\n");

            RouteInfo info = new RouteInfo();

            info.parent = entries[0];

            for (String entry : Arrays.copyOfRange(entries, 1, entries.length)) {
                if (entry.isEmpty()) {
                    continue;
                }

                if (entry.contains("@")) {
                    info.routes.put(entry.split("@")[0], entry.split("@")[1]);
                } else {
                    info.neighbours.add(entry);
                }
            }

            log.log(Level.INFO, "Got route info: \nParent: {0}\nNeighbours: {1}\nRoutes: {2}", new Object[] {info.parent, info.neighbours, info.routes});
            routes.put(nodeAddr, info);
            return;
        }

        throw new CoapException(uri, Method.GET, response, "Failed to get routes");
    }

    @Override
    public String getRessource() {
        return RESSOURCE;
    }
}
