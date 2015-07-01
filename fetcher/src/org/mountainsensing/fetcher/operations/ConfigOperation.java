package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.mountainsensing.fetcher.Operation;
import org.mountainsensing.pb.Settings.SensorConfig;

/**
 *
 */
public abstract class ConfigOperation extends Operation {

    private static final Logger log = Logger.getLogger(SampleOperation.class.getName());
    
    public static final String RESSOURCE = "config";
    
    /**
     *
     */
    @Parameters(commandDescription = "Get the configuration from the node(s)")
    public static class Get extends ConfigOperation {

        @Override
        public void processNode(URI uri) throws IOException {
			CoapClient client = new CoapClient(uri);
            
            log.log(Level.FINE, "Attempting to get config from: {0}", client.getURI());
            
            CoapResponse response = client.get();
            
            if (response != null && response.isSuccess()) {
                SensorConfig config = SensorConfig.parseDelimitedFrom(new ByteArrayInputStream(response.getPayload()));
                log.log(Level.FINER, "Config succesfully got from {0}", client.getURI());
                System.out.println(config);
                return;
            }

            throw new IOException("Failed to get config from: " + client.getURI());
        }
    }

    /**
     *
     */
    @Parameters(commandDescription = "Set the configuration of the node(s)")
    public static class Set extends ConfigOperation {
        
        @Parameter(names = {"-a1", "--adc1"}, description = "ADC1 should be enabled")
        private boolean hasAdc1 = false;
        
        @Parameter(names = {"-a2", "--adc2"}, description = "ADC1 should be enabled")
        private boolean hasAdc2 = false;

        @Parameter(names = {"-r", "--rain"}, description = "Rain sensor is connected to node(s)")
        private boolean hasRain = false;

        @Parameter(names = {"-i", "--interval"}, description = "Sampling interval in seconds")
        private int interval = 1200;

        @Parameter(names = {"-a", "--avr"}, converter = HexConverter.class, description = "ID of AVR(s) connected to the node(s), in hex")
        private List<Integer> avrs = new ArrayList<>();

        public static class HexConverter implements IStringConverter<Integer> {
            @Override
            public Integer convert(String value) {
                return Integer.parseInt(value, 16);
            }
        }

        @Override
        public void processNode(URI uri) throws IOException {
            SensorConfig config = SensorConfig.newBuilder().setInterval(interval).setHasADC1(hasAdc1).setHasADC2(hasAdc2).setHasRain(hasRain).addAllAvrIDs(avrs).build();
            
			CoapClient client = new CoapClient(uri);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            config.writeDelimitedTo(out);
            
            log.log(Level.FINE, "Attempting to post config to: {0}", client.getURI());
            
			CoapResponse response = client.post(out.toByteArray(), MediaTypeRegistry.APPLICATION_OCTET_STREAM);
			if (response != null && response.isSuccess()) {
                log.log(Level.INFO, "Config set to \n{0}", config);
                return;
            }
            
            log.log(Level.FINER, "Failed to post config to {0}", client.getURI());
            throw new IOException("Failed to post config to: " + client.getURI());
        }
    }

    @Override
    public String getRessource() {
        return RESSOURCE;
    }
}
