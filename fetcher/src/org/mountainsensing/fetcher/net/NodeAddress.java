package org.mountainsensing.fetcher.net;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.commons.validator.routines.DomainValidator;
import org.apache.commons.validator.routines.InetAddressValidator;

/**
 * Representation of the address of a node.
 * Contains the underlying InetAddress of the node,
 * and optionally it's hostname.
 * 
 * An instance of this class will only attempt a DNS lookup once,
 * in the constructor - see {@link #NodeAddress(java.lang.String)}.
 */
public class NodeAddress {

    /**
     * The actual address of the node.
     */
    private final InetAddress address;

    /**
     * The hostname of the node.
     * null if unknown.
     */
    private final String hostName;

    /**
     * Get a NodeAddress from either an IP address or a hostname.
     * 
     * If a literal IP address is provided, no DNS queries will be made,
     * and the hostname of this Node will remain unknown.
     * 
     * If a hostname is provided, it will be resolved:
     *  - First using NodeNameService
     *  - Second using the default NameService used by InetAddress
     * This is the only DNS query that will be made by this instance,
     * and the result will be cached for the lifetime of this instance.
     * 
     * @param host The hostname or literal IP address (v4 or v6).
     * @throws UnknownHostException If the hostname could not be resolved
     * @throws MalformedHostException If the host is neither a hostname nor an IP address
     */
    public NodeAddress(String host) throws UnknownHostException, MalformedHostException {
        if (!isValid(host)) {
            throw new MalformedHostException("Invalid IP / hostname", host);
        }

        // If it's an IPv4 or an IPv6 address, store it as is
        if (isIPv4Address(host) || isIPv6Address(host)) {
            address = InetAddress.getByName(host);
            hostName = null;
            return;
        }

        // Otherwise it must be a hostname, save that
        hostName = host;

        // If the NodeNameService can resolve it, use that result
        if (NodeNameService.getInstance().knowsNode(host)) {
            address = NodeNameService.getInstance().lookupNode(host);
            return;
        }

        // Otherwise fallback to DNS
        address = InetAddress.getByName(host);
    }

    /**
     * Get the IPAddress of this node.
     * No DNS requests will be made.
     * @return The IPAddress of this node.
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * Get the host name of this node.
     * No DNS requests will be made.
     * @return The host name of this node if known, null otherwise.
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * Check if the hos tname of this node is known.
     * @return True if it is, false otherwise.
     */
    public boolean hasHostName() {
        return hostName != null;
    }

    @Override
    public String toString() {
        return (hasHostName() ? getHostName() + '/' : "") + getAddress().getHostAddress();
    }

    /**
     * Check if a String represents a valid literal IPv6 address.
     * @param addr The String to test.
     * @return True if it does, false otherwise.
     */
    public static boolean isIPv6Address(String addr) {
        return InetAddressValidator.getInstance().isValidInet6Address(addr);
    }

    /**
     * Check if a String represents a valid literal IPv4 address.
     * @param addr The String to test.
     * @return True if it does, false otherwise.
     */
    public static boolean isIPv4Address(String addr) {
        return InetAddressValidator.getInstance().isValidInet4Address(addr);
    }

    /**
     * Check if a String represents a valid literal IPv4 or IPv6 address.
     * @param addr The String to test.
     * @return True if it does, false otherwise.
     * @see #isIPv4Address(java.lang.String)
     * @see #isIPv6Address(java.lang.String)
     */
    public static boolean isAddress(String addr) {
        return InetAddressValidator.getInstance().isValid(addr);
    }

    /**
     * Check if a String represents a valid host name.
     * This includes FQDNs, and "local" domain names.
     * @param host The String to test.
     * @return True if it does, false otherwise.
     */
    public static boolean isHostName(String host) {
        // Allow local addresses (ie hostnames like 'steve')
        return DomainValidator.getInstance(true).isValid(host);
    }

    /**
     * Check if a String represents a valid host name, or IP address.
     * @param host The String to test.
     * @return True if it does, false otherwise.
     * @see #isAddress(java.lang.String)
     * @see #isHostName(java.lang.String)
     */
    public static boolean isValid(String host) {
        return isAddress(host) || isHostName(host);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof NodeAddress && ((NodeAddress)o).address.equals(address);
    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }
}
