package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.mountainsensing.fetcher.CoapException;
import org.mountainsensing.fetcher.CoapException.Method;
import org.mountainsensing.fetcher.utils.EpochDate;
import org.mountainsensing.fetcher.utils.UTCEpochDateFormat;

/**
 *
 */
public abstract class DateOperation extends NodeOperation {
    
    private static final Logger log = Logger.getLogger(SampleOperation.class.getName());
    
    private static final String RESSOURCE = "date";

    private static final DateFormat dateFormat = new UTCEpochDateFormat();
    
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
                Date date = new EpochDate(Long.parseLong(response.getResponseText()));
                log.log(Level.INFO, "Epoch is {0}", dateFormat.format(date));
                return;
            }

            throw new CoapException(uri, Method.GET, response, "Failed to get date");
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
            EpochDate date = epoch == null ? new EpochDate() : new EpochDate(epoch);
            
            String time = Long.toString(date.getEpoch());
            
            CoapResponse response = client.post(time, MediaTypeRegistry.TEXT_PLAIN);
            if (response != null && response.isSuccess()) {
                log.log(Level.INFO, "Epoch set to {0}", dateFormat.format(date));
                return;
            }
            
            throw new CoapException(uri, Method.POST, response, "Failed to set time");
        }
    }

    @Override
    public String getRessource() {
        return RESSOURCE;
    }
}
