package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.mountainsensing.fetcher.Operation;
import org.mountainsensing.fetcher.CoapException;
import org.mountainsensing.fetcher.CoapException.Method;
import org.mountainsensing.fetcher.utils.ProtoBufUtils;
import org.mountainsensing.fetcher.utils.ProtoBufUtils.FieldOverride;
import org.mountainsensing.pb.Settings.SensorConfig;
import org.mountainsensing.pb.Settings.SensorConfig.RoutingMode;

/**
 * Operation for getting / setting the config of a node.
 */
public abstract class ConfigOperation extends Operation {

    private static final Logger log = Logger.getLogger(SampleOperation.class.getName());
    
    public static final String RESSOURCE = "config";

    /**
     * Possible config settings.
     * Used as a delegate for the force and update operations.
     */
    private static class Settings {
        @Parameter(names = {"-a1", "--adc1"}, description = "ADC1 should be enabled")
        private Boolean hasAdc1;
        
        @Parameter(names = {"-a2", "--adc2"}, description = "ADC1 should be enabled")
        private Boolean hasAdc2 ;

        @Parameter(names = {"-r", "--rain"}, description = "Rain sensor is connected to node(s)")
        private Boolean hasRain;

        @Parameter(names = {"-i", "--interval"}, description = "Sampling interval in seconds")
        private Integer interval;

        @Parameter(names = {"-a", "--avr"}, converter = HexConverter.class, description = "ID of AVR(s) connected to the node(s), in hex")
        private List<Integer> avrs;

        @Parameter(names = {"-m", "--routing-mode"}, converter = RoutingModeConverter.class, description = "Routing mode of the node(s).")
        private RoutingMode routingMode;

        public static class HexConverter implements IStringConverter<Integer> {
            @Override
            public Integer convert(String value) {
                return Integer.parseInt(value, 16);
            }
        }

        public static class RoutingModeConverter implements IStringConverter<RoutingMode> {
            @Override
            public RoutingMode convert(String value) {
                return RoutingMode.valueOf(value);
            }
        }
    }

    /**
     * Get operation. Prints out the configuration from the node(s).
     */
    @Parameters(commandDescription = "Get the configuration from the node(s)")
    public static class Get extends ConfigOperation {

        @Override
        public void processNode(URI uri) throws CoapException, IOException {
            log.log(Level.INFO, "Config is \n{0}", configToString(getConfig(uri)));
        }
    }

    /**
     * Force operation. Entirely overwrites the configuration of the node(s), with sane(ish) defaults.
     */
    @Parameters(commandDescription = "Overwrite the configuration of the node(s)")
    public static class Set extends ConfigOperation {

        @ParametersDelegate
        private Settings settings = new Settings();

        public Set() {
            // Set the defaults
            settings.hasAdc1 = false;
            settings.hasAdc2 = false;
            settings.hasRain = false;
            settings.interval = 1200;
            settings.avrs = new ArrayList<>();
            settings.routingMode = RoutingMode.MESH;
        }

        @Override
        public void processNode(URI uri) throws CoapException, IOException {
            SensorConfig config = SensorConfig.newBuilder().setInterval(settings.interval).setHasADC1(settings.hasAdc1).setHasADC2(settings.hasAdc2).setHasRain(settings.hasRain).addAllAvrIDs(settings.avrs).setRoutingMode(settings.routingMode).build();

            setConfig(uri, config);

            log.log(Level.INFO, "Config set to \n{0}", configToString(config));
        }
    }

    /**
     * Get the config from a URI.
     * @param uri The URI to get the config from.
     * @return The parsed config.
     * @throws IOException If we fail to get the config, or parse it.
     */
    protected SensorConfig getConfig(URI uri) throws IOException {
        CoapClient client = new CoapClient(uri);

        log.log(Level.FINE, "Attempting to get config from: {0}", client.getURI());

        CoapResponse response = client.get();

        if (response != null && response.isSuccess()) {
            return SensorConfig.parseDelimitedFrom(new ByteArrayInputStream(response.getPayload()));
        }

        throw new CoapException(uri, Method.GET, response, "Failed to get config");
    }

    /**
     * Set the config on a URI.
     * @param uri The URI to post the config to.
     * @param config The config to post.
     * @throws IOException If we fail to serialize the config, or post it.
     */
    protected void setConfig(URI uri, SensorConfig config) throws IOException {
        CoapClient client = new CoapClient(uri);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        config.writeDelimitedTo(out);

        log.log(Level.FINE, "Attempting to post config to: {0}", client.getURI());

        CoapResponse response = client.post(out.toByteArray(), MediaTypeRegistry.APPLICATION_OCTET_STREAM);
        if (response != null && response.isSuccess()) {
            return;
        }

        throw new CoapException(uri, Method.POST, response, "Failed to post config");
    }

    @Override
    public String getRessource() {
        return RESSOURCE;
    }

    /**
     * Get a String representation of a config.
     * @param config The configuration
     * @return A string representing the config, properly formatted.
     */
    private static String configToString(SensorConfig config) {
        // Override field 4 to print the AVRIDs as hex
        return ProtoBufUtils.toString(config, Collections.singletonMap(4, new FieldOverride<SensorConfig>() {
            @Override
            public String toString(SensorConfig message) {
                String output = "[";
                String sep = "";
                for (int avr : message.getAvrIDsList()) {
                    output += sep + Integer.toHexString(avr);
                    sep = ", ";
                }
                output += "]";
                return output;
            }
        }));
    }
}
