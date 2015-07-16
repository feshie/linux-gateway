package org.mountainsensing.fetcher;

import org.mountainsensing.fetcher.utils.ContextFormatter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.mountainsensing.fetcher.operations.*;

/**
 *
 */
public class Main {
    
    /**
     * Map of all the command names to their associated operation.
     */
    private static final Map<String, Operation> operations = new HashMap<>();
    static {
        operations.put("get-sample", new SampleOperation.Get());
        operations.put("del-sample", new SampleOperation.Delete());
        operations.put("grab-sample", new SampleOperation.Grab());
                
        operations.put("get-config", new ConfigOperation.Get());
        operations.put("set-config", new ConfigOperation.Set());
        
        operations.put("get-date", new DateOperation.Get());
        operations.put("set-date", new DateOperation.Set());

        operations.put("get-reboot", new RebootOperation.Get());
        operations.put("force-reboot", new RebootOperation.Force());

        operations.put("get-routes", new RouteOperation.Get());
    }

    private static final Logger log = Logger.getLogger(SampleOperation.class.getName());
    
    /**
     * Protocol to use for communication with nodes.
     */
    private static final String PROTOCOL = "coap://";

    /**
     * Californium key for the timeout property.
     */
    private static final String COAP_TIMEOUT_KEY = "ACK_TIMEOUT"; 

    /**
     * Exit code indicating success.
     */
    private static final int EXIT_SUCCESS = 0;

    /**
     * Exit code indicating a failure / error.
     */
    private static final int EXIT_FAILURE = 1;

    /**
     * Port to use for the lockSocket.
     * GlacsWeb Bay phone number.
     */
    private static final int LOCK_PORT = 24583;

    /**
     * Socket kept for the lifetime of the application.
     * Ensures that only one instance can run at a time, in a portable, cross platform way.
     */
    private static ServerSocket lockSocket;

    /**
     * The log formatter to use to provide context information.
     */
    private static ContextFormatter logFormatter;

    public static void main(String[] args) {
        Options options = new Options();
        Operation operation = null;

        try {
            operation = operations.get(parseArgs(args, options));
        } catch (ParameterException e) {
            System.err.println(e.getMessage() + ". See --help.");
            System.exit(EXIT_FAILURE);
        }

        setupLogging(options);

        if (!isOnlyInstance()) {
            log.log(Level.SEVERE, "Another instance is already running.");
            System.exit(EXIT_FAILURE);
        }

        log.log(Level.FINE, "Version {0}", getVersion());

        // Don't save / read from the Californium.properties file
        NetworkConfig config = NetworkConfig.createStandardWithoutFile();
        // Nedd to scale the timeout from seconds to ms
        config.setInt(COAP_TIMEOUT_KEY, options.getTimeout() * 1000);
        NetworkConfig.setStandard(config);
        
        // Actually do whatever we should do
        for (String node : operation.getNodes()) {
            URI uri;
            try {
                uri = new URI(PROTOCOL + "[" + options.getPrefix() + node + "]" + "/" + operation.getRessource() + "/");
            } catch (URISyntaxException e) {
                log.log(Level.WARNING, e.getMessage(), e);
                continue;
            }

            logFormatter.setContext(uri);
            
            int retryAttempt = 0;
            
            do {
                try {
                    operation.processNode(uri);
                    // Reset the retry attempt on success
                    retryAttempt = 0;
                    continue;
                } catch (IOException e) {
                    log.log(Level.WARNING, e.getMessage(), e);
                }
                
                retryAttempt++;
                
            /*
                Keep going as long as either:
                        retryAttempt != 0 -> the most recent operation didn't succed
                    and
                        retryAttempt < RETRIES -> we still have attempts to try again left
                or
                        retryAttempt == 0 -> the most recent operation did succeed
                    and
                        shouldKeepProcessingNode() -> that operation needs to process the node more
            */
            } while ((retryAttempt != 0 && retryAttempt < options.getRetries()) || (retryAttempt == 0 && operation.shouldKeepProcessingNode()));

            logFormatter.clearContext();
        }

        log.log(Level.FINE, "Finished execution");
    }
    
    /**
     * Parse an array of arguments into an options object.
     * @param args The arguments to parse.
     * @param options The parameters to use for parsing.
     * @return The name of the command.
     * @throws ParameterException If there was an error parsing, such as a missing required parameter or command.
     */
    private static String parseArgs(String[] args, Options options) throws ParameterException {
        JCommander parser = new JCommander(options);
        
        for (String opName : operations.keySet()) {
            parser.addCommand(opName, operations.get(opName));
        }
        
        parser.parse(args);

        if (options.shouldShowHelp()) {
            parser.usage();
            System.exit(EXIT_SUCCESS);
        }

        if (options.shouldShowVersion()) {
            System.out.println(getVersion());
            System.exit(EXIT_SUCCESS);
        }
        
        if (parser.getParsedCommand() == null) {
            throw new ParameterException("Command is required");
        }

        return parser.getParsedCommand();
    }
    
    /**
     * Setup the logger to use, and it's associated formatter.
     */
    private static void setupLogging(Options options) {
        Logger rootLogger = Logger.getLogger("");

        // Remove any existing handlers
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        Logger fetcherLogger = Logger.getLogger(Main.class.getPackage().getName());
        logFormatter = new ContextFormatter();

        // Enable all logging for everything
        rootLogger.setLevel(Level.ALL);

        // Console handler.
        ConsoleHandler console = new ConsoleHandler();
        console.setLevel(options.getConsoleLevel());
        console.setFormatter(logFormatter.getConsoleFormatter());
        fetcherLogger.addHandler(console);

        // File logger.
        if (options.hasLogFile()) {
            try {
                // Append to the log file if it's present
                FileHandler file = new FileHandler(options.getLogFile(), true);
                file.setLevel(options.getFileLevel());
                file.setFormatter(logFormatter.getFileFormatter());
                fetcherLogger.addHandler(file);
            } catch (IOException | SecurityException e) {
                log.log(Level.WARNING, "Unable to log to file: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Check if this is the only instance of this application running.
     * @return True if this is the only instance, false otherwise.
     */
    private static boolean isOnlyInstance() {
        // Add a shutdown hook to close the socket on shutdown.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (lockSocket != null) {
                    try {
                        lockSocket.close();
                    } catch (IOException ex) {
                        log.log(Level.SEVERE, "Failed to close lock socket!", ex);
                    }
                }
            }
        });

        try {
            lockSocket = new ServerSocket(LOCK_PORT, 1, InetAddress.getLocalHost());
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * Get the version of this JAR.
     * @return A String representing the version.
     */
    private static String getVersion() {
        return Main.class.getPackage().getImplementationVersion();
    }
}