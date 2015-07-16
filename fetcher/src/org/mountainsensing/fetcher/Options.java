package org.mountainsensing.fetcher;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 *
 */
public class Options {
    /**
     * Dummy parameter to make usage clearer. Real nodes parameter is in Operation.
     */
    @Parameter(descriptionKey = "nodes", description = "node(s)")
    private List<String> nodes = new ArrayList<String>();

    @Parameter(names = {"-p", "--prefix"}, description = "/96 IPv6 prefix for the nodes")
    private String prefix = "aaaa::c30c:0:0:";

    @Parameter(names = {"-t", "--timeout"}, description = "CoAP timeout in seconds")
    private int timeout = 10;

    @Parameter(names = {"-r", "--retries"}, description = "Number of retries before giving up on a node")
    private int retries = 3;

    @Parameter(names = {"-h", "--help"}, description = "Show usage help and exit", help = true)
    private boolean help = false;

    @Parameter(names = {"-v", "--version"}, description = "Print the version and exit")
    private boolean version = false;

    @Parameter(names = {"--console-level"}, converter = LevelConverter.class, description = "Minimum log level of messages displayed on the console")
    private Level consoleLevel = Level.INFO;

    @Parameter(names = {"--file-level"}, converter = LevelConverter.class, description = "Minimum log level of messages printed to the log file. Not valid without --log-file.")
    private Level fileLevel = Level.FINE;

    @Parameter(names = {"--log-file"}, description = "Log messages to a seperate file")
    private String logFile = null;

    public static class LevelConverter implements IStringConverter<Level> {
        @Override
        public Level convert(String value) {
            return Level.parse(value);
        }
    }

    /**
     * Get a String representing the IPv6 Prefix.
     * @return The IPV6 /96 Prefix to prepend to use
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Get the timeout for operations.
     * @return The timeout in seconds.
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Get the retires for operations.
     * @return The number of times an operation should be repeated before giving up.
     */
    public int getRetries() {
        return retries;
    }
    
    /**
     * Check if help output should be shown.
     * @return True if help should be shown, false otherwise.
     */
    public boolean shouldShowHelp() {
        return help;
    }

    /**
     * Check if the version should printed.
     * @return True if the version needs printing, false otherwise.
     */
    public boolean shouldShowVersion() {
        return version;
    }

    /**
     * Get the minimum level of log messages to log to the Console.
     * @return The minimum level for Console logging.
     */
    public Level getConsoleLevel() {
        return consoleLevel;
    }

    /**
     * Get the minimum level of log messages to log to the File.
     * @return The minimum level for File logging.
     */
    public Level getFileLevel() {
        return fileLevel;
    }

    /**
     * Get the logfile to log things to.
     * @return A string representing the logfile, null if logging to a file hasn't been request.
     */
    public String getLogFile() {
        return logFile;
    }

    /**
     * Check if file logging has been request.
     * @return True if so, false otherwise.
     */
    public boolean hasLogFile() {
        return logFile != null;
    }
}
