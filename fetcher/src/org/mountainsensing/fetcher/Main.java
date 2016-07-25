/**
 * Main CoAP fetcher for sensor nodes
 * Arthur Fabre, University of Southampton, 2015
 * mountainsensing.org
 */
package org.mountainsensing.fetcher;

import org.mountainsensing.fetcher.operations.NodeOperation;
import org.mountainsensing.fetcher.utils.ContextFormatter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mountainsensing.fetcher.operations.*;

/**
 * Main class. Parses command line arguments, and runs required command.
 */
public class Main {

    /**
     * Name the fetcher can be invoked by.
     */
    private static final String PROGRAM_NAME = "fetcher";

    /**
     * Map of all the command names to their associated operation.
     * Ordered to give a sensible output when using --help.
     */
    private static final Map<String, Operation> operations = new LinkedHashMap<>();
    static {
        operations.put("get-sample", new SampleOperation.Get());
        operations.put("grab-sample", new SampleOperation.Grab());
        operations.put("del-sample", new SampleOperation.Delete());
        operations.put("decode-sample", new SampleOperation.Decode());

        operations.put("get-config", new ConfigOperation.Get());
        operations.put("edit-config", new ConfigOperation.Edit());
        operations.put("force-config", new ConfigOperation.Force());
        operations.put("decode-config", new ConfigOperation.Decode());

        operations.put("get-date", new DateOperation.Get());
        operations.put("set-date", new DateOperation.Set());

        operations.put("get-uptime", new UptimeOperation());

        operations.put("get-reboot", new RebootOperation.Get());
        operations.put("force-reboot", new RebootOperation.Force());

        operations.put("get-routes", new RouteOperation());

        operations.put("ping", new PingOperation());
    }

    private static final Logger log = Logger.getLogger(SampleOperation.class.getName());

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
            operation = parseArgs(args, options);
        } catch (ParameterException e) {
            System.err.println(e.getMessage() + ". See --help");
            System.exit(EXIT_FAILURE);
        }

        setupLogging(options);

        log.log(Level.FINE, "Starting. Version {0}", getVersion());

        if (!isOnlyInstance()) {
            log.log(Level.SEVERE, "Another instance is already running");
            exit(false);
        }

        Operation.setContextFormatter(logFormatter);

        // Sketchy hacky magic. Would be nice to get rid of it.
        NodeOperation.init(options.getRetries(), options.getTimeout());

        operation.perform();

        exit(true);
    }

    /**
     * Exit the application.
     * @param isSuccess True if the execution was successful, false otherwise.
     */
    private static void exit(boolean isSuccess) {
        log.log(Level.FINE, "Finished execution");
        System.exit(isSuccess ? EXIT_SUCCESS : EXIT_FAILURE);
    }

    /**
     * Parse an array of arguments into an options object.
     * @param args The arguments to parse.
     * @param options The parameters to use for parsing.
     * @return The operation requested.
     * @throws ParameterException If there was an error parsing, such as a missing required parameter or command.
     */
    private static Operation parseArgs(String[] args, Options options) throws ParameterException {
        JCommander parser = new JCommander(options);
        parser.setProgramName(PROGRAM_NAME);

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

        Operation operation = operations.get(parser.getParsedCommand());

        if (operation.shouldShowHelp()) {
            parser.usage(parser.getParsedCommand());
            System.exit(EXIT_SUCCESS);
        }

        return operation;
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
