package org.mountainsensing.fetcher.net;

import java.io.IOException;

/**
 * Exception indicating a host name / IP address was malformed.
 */
public class MalformedHostException extends IOException {

    private static final long serialVersionUID = 1;

    /**
     * Create a MalformedHostException for a given host.
     * @param msg A message describing what happened.
     * @param host The hostname / IP address it happened on.
     */
    public MalformedHostException(String msg, String host) {
        super(msg + ": " + host);
    }
}
