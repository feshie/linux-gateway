/**
 * set config of nodes using CoAP
 * Arthur Fabre, University of Southampton, 2015
 * mountainsensing.org
 */
package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.mountainsensing.fetcher.CoapException;
import org.mountainsensing.fetcher.CoapException.Method;
import org.mountainsensing.fetcher.net.NodeAddress;
import org.mountainsensing.fetcher.utils.FormatUtils;
import org.mountainsensing.fetcher.utils.ProtoBufUtils;
import org.mountainsensing.pb.Settings.SensorConfig;
import org.mountainsensing.pb.Settings.SensorConfig.Builder;
import org.mountainsensing.pb.Settings.SensorConfig.RoutingMode;

/**
 * Operation for getting / setting the config of a node.
 */
public abstract class ConfigOperation extends NodeOperation {

    private static final Logger log = Logger.getLogger(SampleOperation.class.getName());

    /**
     * Map used to override the printing of Configs.
     * The key is the integer id of the field to override the printing for,
     * mapping to a FieldOverride returning the String to use for that field.
     */
    private static final Map<Integer, ProtoBufUtils.FieldOverride<SensorConfig>> configOverrideMap;
    static {
        configOverrideMap = new HashMap<>();

        // Override field 4 to print the AVRID as hex
        configOverrideMap.put(SensorConfig.AVRID_FIELD_NUMBER, new ProtoBufUtils.FieldOverride<SensorConfig>() {
            @Override
            public String toString(SensorConfig message) {
                return FormatUtils.toHex(message.getAvrID());
            }
        });

        // Override field 7 to print the Power Board ID as hex
        configOverrideMap.put(SensorConfig.POWERID_FIELD_NUMBER, new ProtoBufUtils.FieldOverride<SensorConfig>() {
            @Override
            public String toString(SensorConfig message) {
                return FormatUtils.toHex(message.getPowerID());
            }
        });
    }

    public static final String RESSOURCE = "config";

    /**
     * Possible config settings.
     * Used as a delegate for the force and update operations.
     */
    private static class Settings {

        /**
         * Magic canary value to clear an ID.
         */
        private static final int ID_NONE = 0x0;

        @Parameter(names = {"-a1", "--adc1"}, arity = 1, description = "ADC1 should be enabled")
        private Boolean hasAdc1;

        @Parameter(names = {"-a2", "--adc2"}, arity = 1, description = "ADC1 should be enabled")
        private Boolean hasAdc2 ;

        @Parameter(names = {"-r", "--rain"}, arity = 1, description = "Rain sensor is connected to node(s)")
        private Boolean hasRain;

        @Parameter(names = {"-i", "--interval"}, description = "Sampling interval in seconds")
        private Integer interval;

        @Parameter(names = {"-a", "--avr"}, converter = HexConverter.class, validateWith=HexValidator.class, description = "ID of the AVR connected to the node(s), in hex. " + ID_NONE + " for no AVR")
        private Integer avrID;

        @Parameter(names = {"-p", "--power"}, converter = HexConverter.class, validateWith=HexValidator.class, description = "ID of the Power Board connected to the node(s), in hex. " + ID_NONE + " for no Power Board")
        private Integer powerID;

        @Parameter(names = {"-m", "--routing-mode"}, converter = RoutingModeConverter.class, validateWith=RoutingModeValidator.class, description = "Routing mode of the node(s).")
        private RoutingMode routingMode;

        /**
         * Check if an ID specified is valid.
         * An ID is valid when it is not null and not ID_NONE
         * @param id The ID to check
         * @return True if the ID is valid, false otherwise
         */
        public static boolean isValidID(Integer id) {
            return id != null && id != ID_NONE;
        }

        /**
         * Check if an ID specified is ID_NONE.
         * @param id The ID to check
         * @return True if is ID_NONE, false otherwise
         */
        public static boolean isNoneID(Integer id) {
            return id != null && id == ID_NONE;
        }

        /**
         * Validate a HEX String input.
         */
        public static class HexValidator implements IParameterValidator {
            @Override
            public void validate(String name, String value) throws ParameterException {
                if (!FormatUtils.isHex(value)) {
                    throw new ParameterException("Parameter " + name + " should be a valid Hex value " + FormatUtils.HEX_REGEX);
                }
            }
        }

        /**
         * Convert a HEX String to an Integer.
         */
        public static class HexConverter implements IStringConverter<Integer> {
            @Override
            public Integer convert(String value) {
                return FormatUtils.fromHex(value);
            }
        }

        /**
         * Validate a RoutingMode input.
         */
        public static class RoutingModeValidator implements IParameterValidator {
            @Override
            public void validate(String name, String value) throws ParameterException {
                for (RoutingMode mode : RoutingMode.values()) {
                    if (mode.name().equals(value)) {
                        return;
                    }
                }

                throw new ParameterException("Parameter " + name + " should be one of " + Arrays.toString(RoutingMode.values()));
            }
        }

        /**
         * Convert a RoutingMode to the actual enum.
         */
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
        protected void processNode(URI uri, NodeAddress nodeAddr) throws CoapException, IOException {
            log.log(Level.INFO, "Config is \n{0}", configToString(getConfig(uri)));
        }
    }

    /**
     * Force operation. Entirely overwrites the configuration of the node(s), with sane(ish) defaults.
     */
    @Parameters(commandDescription = "Overwrite the configuration of the node(s)")
    public static class Force extends ConfigOperation {

        @ParametersDelegate
        private Settings settings = new Settings();

        public Force() {
            // Set the defaults
            settings.hasAdc1 = false;
            settings.hasAdc2 = false;
            settings.hasRain = false;
            settings.interval = 1200;
            settings.avrID = null;
            settings.routingMode = RoutingMode.MESH;
            settings.powerID = null;
        }

        @Override
        protected void processNode(URI uri, NodeAddress nodeAddr) throws CoapException, IOException {
            Builder configBuilder = SensorConfig.newBuilder();

            configBuilder.setInterval(settings.interval);
            configBuilder.setHasADC1(settings.hasAdc1);
            configBuilder.setHasADC2(settings.hasAdc2);
            configBuilder.setHasRain(settings.hasRain);

            configBuilder.setRoutingMode(settings.routingMode);

            if (Settings.isValidID(settings.avrID)) {
                configBuilder.setAvrID(settings.avrID);
            }

            // Set a PowerID only if one has been specified
            if (Settings.isValidID(settings.powerID)) {
                configBuilder.setPowerID(settings.powerID);
            }

            SensorConfig newConfig = configBuilder.build();

            setConfig(uri, newConfig);

            log.log(Level.INFO, "Config set to \n{0}", configToString(newConfig));
        }
    }

    /**
     * Edit operation. Update / edit the configuration of the nodes.
     */
    @Parameters(commandDescription = "Update the configuration of the node(s)")
    public static class Edit extends ConfigOperation {

        @ParametersDelegate
        private Settings settings = new Settings();

        @Override
        protected void processNode(URI uri, NodeAddress nodeAddr) throws CoapException, IOException {
            SensorConfig oldConfig = getConfig(uri);
            Builder editBuilder = SensorConfig.newBuilder();

            editBuilder.setInterval(settings.interval != null ? settings.interval : oldConfig.getInterval());
            editBuilder.setHasADC1(settings.hasAdc1 != null ? settings.hasAdc1 : oldConfig.getHasADC1());
            editBuilder.setHasADC2(settings.hasAdc2 != null ? settings.hasAdc2 : oldConfig.getHasADC2());
            editBuilder.setHasRain(settings.hasRain != null ? settings.hasRain : oldConfig.getHasRain());
            editBuilder.setRoutingMode(settings.routingMode != null ? settings.routingMode : oldConfig.getRoutingMode());

            // Ensure we only set AvrID if a valid ID was specified, or not NONE was specified and the old config had one
            if (Settings.isValidID(settings.avrID)) {
                editBuilder.setAvrID(settings.avrID);
            } else if (!Settings.isNoneID(settings.avrID) && oldConfig.hasAvrID()) {
                editBuilder.setAvrID(oldConfig.getAvrID());
            }

            // Ensure we only set PowerID if a valid ID was specified, or not NONE was specified and the old config had one
            if (Settings.isValidID(settings.powerID)) {
                editBuilder.setPowerID(settings.powerID);
            } else if (!Settings.isNoneID(settings.powerID) && oldConfig.hasPowerID()) {
                editBuilder.setPowerID(oldConfig.getPowerID());
            }

            SensorConfig newConfig = editBuilder.build();

            setConfig(uri, newConfig);

            log.log(Level.INFO, "Config updated to \n{0}", configToString(newConfig));
        }
    }

    /**
     * Decode operation. Decodes a config.
     */
    @Parameters(commandDescription = "Decode a delimited protocol buffer encoded configuration")
    public static class Decode extends DecodeOperation {

        private static final String CONFIG_START = "+++SERIALDUMP+++CONFIG+++START+++";
        private static final String CONFIG_END = "+++SERIALDUMP+++CONFIG+++END+++";

        @Override
        protected void decode(byte[] data, String nodeId) throws IOException {
            log.log(Level.INFO, "Decoded config to \n{0}", configToString(SensorConfig.parseDelimitedFrom(new ByteArrayInputStream(data))));
        }

        @Override
        protected String startMarker() {
            return CONFIG_START;
        }

        @Override
        protected String endMarker() {
            return CONFIG_END;
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
    private static String configToString(SensorConfig config) throws IOException {
        return ProtoBufUtils.toString(config, configOverrideMap);
    }
}
