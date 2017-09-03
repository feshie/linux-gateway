/**
 * set date on nodes using CoAP requests
 * Arthur Fabre, University of Southampton, 2015
 * mountainsensing.org
 */
package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.Parameters;
import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.mountainsensing.fetcher.CoapException;
import org.mountainsensing.fetcher.CoapException.Method;
import org.mountainsensing.fetcher.net.NodeAddress;
import org.mountainsensing.fetcher.utils.EpochDate;
import org.mountainsensing.fetcher.utils.FormatUtils;
import org.mountainsensing.fetcher.utils.UTCDateFormat;

/**
 * Operation for getting the uptime.
 */
@Parameters(commandDescription = "Get the uptime from the node(s)")
public class UptimeOperation extends NodeOperation {

    private static final Logger log = Logger.getLogger(UptimeOperation.class.getName());

    private static final String RESSOURCE = "uptime";

    /**
     * The format to use for displaying dates.
     */
    private static final DateFormat dateFormat = new UTCDateFormat();

    @Override
    protected void processNode(URI uri, NodeAddress nodeAddr) throws IOException {
        CoapClient client = new CoapClient(uri);
        CoapResponse response = client.get();

        if (response != null && response.isSuccess()) {
            long uptime = Long.parseLong(response.getResponseText());

            // Boot time is current time - uptime
            EpochDate bootTime = new EpochDate(new EpochDate().getEpoch() - uptime);
            log.log(Level.INFO, "Uptime is {0}s ({1}). Boot time: {2}",
                    new Object[] {
                        uptime,
                        FormatUtils.getInterval(uptime),
                        dateFormat.format(bootTime)
                    }
            );
            return;
        }

        throw new CoapException(uri, Method.GET, response, "Failed to get uptime");
    }

    @Override
    public String getRessource() {
        return RESSOURCE;
    }
}
