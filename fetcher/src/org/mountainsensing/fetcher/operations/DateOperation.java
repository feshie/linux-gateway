package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.mountainsensing.fetcher.Operation;

/**
 *
 */
public abstract class DateOperation extends Operation {
    
    private static final Logger log = Logger.getLogger(SampleOperation.class.getName());
    
    private static final String RESSOURCE = "date";

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    /**
     *
     */
    @Parameters(commandDescription = "Get the date from the node(s)")
    public static class Get extends DateOperation {

        @Override
        public void processNode(URI uri) throws IOException {
            CoapClient client = new CoapClient(uri);
            CoapResponse response = client.get();

            if (response != null && response.isSuccess()) {
                // Scale the seconds to ms
                Date date = new Date(Long.parseLong(response.getResponseText()) * 1000);
                log.log(Level.INFO, "Epoch is {0}, aka {1}", new Object[]{response.getResponseText(), dateFormat.format(date)});
                return;
            }

            log.log(Level.FINER, "Failed to get date from {0}", client.getURI());
            throw new IOException("Failed to get date from {0}: " + client.getURI());
        }
    }
    
    /**
     *
     */
    @Parameters(commandDescription = "Set the date of the node(s)")
    public static class Set extends DateOperation {

        @Parameter(names = {"-e", "--epoch"}, description = "Override the epoch to use.\n             Default: This computer's UTC epoch")
        private Integer epoch = null;

        @Override
        public void processNode(URI uri) throws IOException {
            CoapClient client = new CoapClient(uri);
            
            // Get a date option represeting the current time, or if sepcified the command line param time
            Date date = epoch == null ? new Date() : new Date((long)epoch * 1000);
            
            // Scale from ms to seconds
            String time = Long.toString(date.getTime() / 1000);
            
            CoapResponse response = client.post(time, MediaTypeRegistry.TEXT_PLAIN);
            if (response != null && response.isSuccess()) {
                log.log(Level.INFO, "Epoch set to {0}, aka {1}", new Object[]{time, dateFormat.format(date)});
                return;
            }
            
            log.log(Level.FINER, "Failed to set time to {0}", uri);
            throw new IOException("Failed to set time to: " + uri); 
        }
    }

    @Override
    public String getRessource() {
        return RESSOURCE;
    }
}
