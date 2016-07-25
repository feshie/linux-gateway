package org.mountainsensing.fetcher.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Helper to make a DOT graph from routing info.
 */
public class RouteGraph {

    /**
     * Strict graph means duplicate edges are merged into one (keeping their attributes).
     * This allows us to specify neighbors and routes, and have them show up as red when it's a route.
     */
    private static final String GRAPH = "strict graph";

    /**
     * Options to apply to edges representing neighbours.
     */
    private static final String NEIGHBOUR_OPTIONS = "";

    /**
     * Options to apply to edges representing default routes.
     */
    private static final String ROUTE_OPTIONS = "[color=\"red\"]";

    /**
     * All the edges we're aware of.
     */
    private final Set<Edge> neighbours = new HashSet<>();

    /**
     * All the routes we're aware of.
     */
    private final Set<Edge> routes = new HashSet<>();

    /**
     * The name of this graph.
     */
    private final String name;

    /**
     * Representation of an Edge in the Graph.
     */
    private static class Edge {

        /**
         * The first member of this edge.
         */
        private final String A;

        /**
         * The second member of this edge.
         */
        private final String B;

        /**
         * Create an edge from two nodes.
         * @param A The first member of this edge.
         * @param B The second member of this edge.
         */
        public Edge(String A, String B) {
            this.A = A;
            this.B = B;
        }

        /**
         * Get the first member of this edge.
         * @return The first member of this edge.
         */
        public String getA() {
            return A;
        }

        /**
         * Get the second member of this edge.
         * @return The second member of this edge.
         */
        public String getB() {
            return B;
        }

        /**
         * Check if this Edge equals another.
         * An Edge is equal to another edge if, and only if,
         * the first members and the second members are respectively equal.
         * @param o The edge to chck equality against.
         * @return True if this Edge equals the other, false otherwise.
         */
        @Override
        public boolean equals(Object o) {
            return o instanceof Edge && ((Edge)o).A.equals(A) && ((Edge)o).B.equals(B);
        }

        @Override
        public int hashCode() {
            return A.hashCode() + B.hashCode();
        }
    }

    /**
     * Create a RouteGraph.
     * @param name The name of the graph.
     */
    public RouteGraph(String name) {
        this.name = name;
    }

    /**
     * Add a route between a node and another one.
     * @param node One node.
     * @param dest The other node in the route.
     */
    public void addRoute(String node, String dest) {
        routes.add(new Edge(node, dest));
    }

    /**
     * Add a neighbour edge between two nodes
     * @param node One node.
     * @param neighbour One of it's neeighbouring nodes.
     */
    public void addNeighbour(String node, String neighbour) {
        neighbours.add(new Edge(node, neighbour));
    }

    /**
     * Write the DOT repsentation of the Graph out.
     * @param out The stream to write the Graph out to.
     * @throws IOException If an error is encountered writing the Graph.
     */
    public void write(OutputStream out) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))) {
            writer.append(GRAPH).append(" ").append(name).append(" {").append(System.lineSeparator());

            writeEdges(writer, neighbours, NEIGHBOUR_OPTIONS);

            writeEdges(writer, routes, ROUTE_OPTIONS);

            writer.append("}").append(System.lineSeparator());
        }
    }

    /**
     * Write a collection of edges in DOT format.
     * @param writer The Writer to write to in DOT.
     * @param edges The edges to write.
     * @param options A DOT options String that will be used one every edge.
     * @throws IOException If an error occurs writing to the writer.
     */
    private static void writeEdges(Writer writer, Collection<? extends Edge> edges, String options) throws IOException {
        for (Edge edge : edges) {
            startLine(writer);
            writer.append(quote(edge.getA())).append(" -- ").append(quote(edge.getB())).append(" ").append(options);
            endLine(writer);
        }
    }

    /**
     * Enclose a String in literal quotes.
     * @param str The String to enclose.
     * @return The String enclosed in literal quotes.
     */
    private static String quote(String str) {
        return "\"" + str + "\"";
    }

    /**
     * Write the DOT start of line marker.
     * @param writer The writer to write to in DOT.
     * @throws IOException If an error occurs writing to the writer.
     */
    private static void startLine(Writer writer) throws IOException {
        writer.append("\t");
    }

    /**
     * Write the DOT end of line marker.
     * @param writer The writer to write to in DOT.
     * @throws IOException If an error occurs writing to the writer.
     */
    private static void endLine(Writer writer) throws IOException {
        writer.append(";").append(System.lineSeparator());
    }
}
