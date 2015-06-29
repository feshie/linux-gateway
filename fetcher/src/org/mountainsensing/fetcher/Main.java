package org.mountainsensing.fetcher;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    }

    private static final Logger log = Logger.getLogger(SampleOperation.class.getName());
    
    /**
     * Protocol to use for communication with nodes.
     */
    private static final String PROTOCOL = "coap://";

    /**
     * Exit code indicating success.
     */
    private static final int EXIT_SUCCESS = 0;

    /**
     * Exit code indicating a failure / error.
     */
    private static final int EXIT_FAILURE = 1;

    public static void main(String[] args) {
        setupLogging();
        
        Options options = new Options();
        Operation operation = null;
        
        try {
            operation = operations.get(parseArgs(args, options));
        } catch (ParameterException e) {
            System.err.println(e.getMessage() + ". See --help.");
            System.exit(EXIT_FAILURE);
        }
        
        for (String node : operation.getNodes()) {
            URI uri ;
            try {
                uri = new URI(PROTOCOL + "[" + options.getPrefix() + node + "]" + "/" + operation.getRessource() + "/");
            } catch (URISyntaxException e) {
                log.log(Level.WARNING, e.getMessage(), e);
                continue;
            }

            for (int i = 0; i < options.getRetries(); i++) {
                try {
                    operation.processNode(uri, options.getTimeout());
                    break;
                } catch (IOException e) {
                    log.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
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
        
        if (parser.getParsedCommand() == null) {
            throw new ParameterException("Command is required");
        }

        return parser.getParsedCommand();
    }
    
    /**
     * 
     */
    private static void setupLogging() {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.INFO);
        
        // Remove any existing handlers
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        ConsoleHandler console = new ConsoleHandler();
        console.setFormatter(new LogFormatter());
        rootLogger.addHandler(console);
        
        // Supress some of the californium logging
        Logger californiumLogger = Logger.getLogger("org.eclipse.californium");
        californiumLogger.setLevel(Level.WARNING);
    }
}