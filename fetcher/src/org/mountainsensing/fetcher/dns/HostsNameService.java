package org.mountainsensing.fetcher.dns;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import sun.net.spi.nameservice.NameService;

/**
 * Implementation of a NameService that parse DNS info from 
 * files in the UNIX hosts file format.
 * 
 * Any number of hosts files can be parsed, and entries will override the system DNS.
 * If an entry is not found, the system DSN will be used however.
 * 
 * See {@link HostNameServiceDescriptor} for information on configuring this service to be used.
 */
public class HostsNameService implements NameService {

    /**
     * Name of this NameService.
     */
    protected static final String NAME = HostsNameService.class.getSimpleName();

    /**
     * Type of this NameService.
     */
    protected static final String TYPE = "dns";

    /**
     * NameService provider to use when we fail to lookup something.
     */
    private static final String DEFAULT_PROVIDER = "default";

    /**
     * System Property to set the NameService Provider used.
     */
    private static final String NAMESERVICE_PROPERTY = "sun.net.spi.nameservice.provider.";

    /**
     * Enable HostsNameService as a System NameService Provider.
     * This default System provider will be added with a lower property.
     * 
     * @note This MUST be called very early to have an effect,
     * typically as one of the first calls in {@code main}.
     */
    public static void enable() {
        System.setProperty(NAMESERVICE_PROPERTY + "1", TYPE + "," + NAME);
        System.setProperty(NAMESERVICE_PROPERTY + "2", DEFAULT_PROVIDER);
    }

    /**
     * Parse a file, in the UNIX hosts file format.
     * Entries in the file will override identical entries in any previous files parsed.
     * 
     * @param in A stream to the hosts file.
     * @throws IOException If an error occurs reading the file.
     */
    public static void parse(InputStream in) throws IOException {
        // TODO
    }

    @Override
    public InetAddress[] lookupAllHostAddr(String host) throws UnknownHostException {
        System.out.println("Looking up " + host);

        // TODO
        throw new UnknownHostException(host);
    }

    @Override
    public String getHostByAddr(byte[] addr) throws UnknownHostException {
        // TODO
        throw new UnknownHostException(Arrays.toString(addr));
    }
}