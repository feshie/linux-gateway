package org.mountainsensing.fetcher.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * A NameService that can parse a file in the UNIX hosts file format,
 * and resolve hostnames that are defined in it.
 * 
 * Any number of hosts files can be parsed.
 * Only the last IP found for a given hostname will be used.
 */
public class NodeNameService {

    /**
     * Interface allowing the parser to report what line it's at in the stream.
     * This allows the callee to display more relevant log / error messages.
     */
    public interface LineTracker {

        /**
         * Report the line being parsed.
         * @param line The line number of the line being parsed
         */
        public void setLine(int line);
    }

    private static final Logger log = Logger.getLogger(NodeNameService.class.getName());

    /**
     * The singleton instance.
     */
    private static final NodeNameService INSTANCE = new NodeNameService();

    /**
     * Pattern matching a comment.
     * From man 5 hosts:
     *   Text from a "#" character until the end of the line is a comment, and is ignored.
     */
    private static final Pattern COMMENT = Pattern.compile("#.*\\z");

    /**
     * Pattern matching whitespace.
     * From man 5 hosts:
     *   Fields of the entry are separated by any number of blanks and/or tab characters.
     */
    private static final Pattern WHITESPACE = Pattern.compile("[ \t]+");

    /**
     * Map of HostNames to IPs.
     */
    private final Map<String, InetAddress> hosts = new HashMap<>();

    /**
     * Private constructor as this is a Singelton.
     */
    private NodeNameService() {

    }

    /**
     * Get the Singleton instance of this class.
     * @return The Singelton instance.
     */
    public static NodeNameService getInstance() {
        return INSTANCE;
    }

    /**
     * Parse a file, in the UNIX hosts file format.
     * Entries in the file will override identical entries in any previous files parsed.
     *
     * @param in A stream to the hosts file.
     * @param tracker Tracker to use to report the current line we're parsing. Can be used to set
     * the context for a Logger.
     * @throws IOException If an error occurs reading the file, or if it cannto be parsed.
     * @see #parse(java.io.InputStream)
     */
    public void parse(InputStream in, LineTracker tracker) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

            // The current line number we're parsing
            int lineNum = 1;

            // Parse every line in the file
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                if (tracker != null) {
                    tracker.setLine(lineNum);
                }

                parseLine(line);
                lineNum++;
            }
        }
    }

    /**
     * Parse a file, in the UNIX hosts file format.
     *
     * This behaves the same as {@link #parse(java.io.InputStream, org.mountainsensing.fetcher.net.NodeNameService.LineTracker)},
     * but with no LineTracker.
     *
     * @param in A stream to the hosts file.
     * @throws IOException If an error occurs reading the file, or if it cannto be parsed.
     * @see #parse(java.io.InputStream, org.mountainsensing.fetcher.net.NodeNameService.LineTracker)
     */
    public void parse(InputStream in) throws IOException {
        parse(in, null);
    }

    /**
     * Parse a single line of the hosts file.
     * @param line The line of the hosts file.
     * @throws IOException If an error occurs parsing the line.
     */
    private void parseLine(String line) throws IOException {
        // Strip any comments from the line
        line = COMMENT.matcher(line).replaceFirst("");

        // Remove leading / trailing whitespace
        line = line.trim();

        // Skip any empty lines
        if (line.isEmpty()) {
            return;
        }

        // Split the String on whitespace matches
        String[] tokens = WHITESPACE.split(line);

        // We should always have one token from split()
        if (tokens.length < 1) {
            throw new IOException("Internal parser error: Missing host for line \'" + line + "\'");
        }

        String host = tokens[0];

        if (!NodeAddress.isAddress(host)) {
            throw new IOException("Invalid IP Address \'" + host + "\'");
        }

        // We need at least one hostname (we'll always have at least one token,
        // as line will never be empty by this point).
        if (tokens.length < 2) {
            throw new IOException("Missing hostname(s) for IP \'" + host + "\'");
        }

        // host is a literal IP, so this will not cause any DNS lookups
        InetAddress addr = InetAddress.getByName(host);

        for (int i = 1; i < tokens.length; i++) {
            register(addr, tokens[i]);   
        }
    }

    /**
     * Register a hostname as mapping to an IP.
     * @param addr The IP address.
     * @param host The hostname that maps to it.
     * @throws IOException If the hostname is not valid.
     */
    private void register(InetAddress addr, String host) throws IOException {
        log.log(Level.FINE, "Node {0} has IP {1}", new Object[]{host, addr.getHostAddress()});

        if (!NodeAddress.isHostName(host)) {
            throw new IOException("Invalid host name \'" + host + "\'");
        }

        hosts.put(host, addr);
    }

    /**
     * Check if an address is known for a given hostname.
     * @param host The hostname to check.
     * @return True if an IP address is known for it, false otherwise.
     */
    public boolean knowsNode(String host) {
        return hosts.containsKey(host);
    }

    /**
     * Get the IP address for a hostname.
     * @param host The hostname to lookup.
     * @return An IP address the hostname points to
     * @throws UnknownHostException If the node is not known see {@link #knowsNode(java.lang.String)}
     */
    public InetAddress lookupNode(String host) throws UnknownHostException {
        if (!knowsNode(host)) {
            throw new UnknownHostException(host);
        }

        return hosts.get(host);
    }
}