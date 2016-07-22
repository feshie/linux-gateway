package org.mountainsensing.fetcher;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import java.util.logging.Level;

/**
 * Main options parsed from the command line.
 */
public class Options {
    @Parameter(names = {"-t", "--timeout"}, description = "CoAP timeout in seconds")
    private int timeout = 10;

    @Parameter(names = {"-r", "--retries"}, description = "Number of retries for node / comms errors before giving up. Client induced errors are never retried")
    private int retries = 3;

    @Parameter(names = {"-h", "--help"}, description = "Show usage help and exit", help = true)
    private boolean help = false;

    @Parameter(names = {"-v", "--version"}, description = "Print the version and exit")
    private boolean version = false;

    @Parameter(names = {"--console-level"}, converter = LevelConverter.class, validateWith=LevelValidator.class, description = "Minimum log level of messages displayed on the console")
    private Level consoleLevel = Level.INFO;

    @Parameter(names = {"--file-level"}, converter = LevelConverter.class, validateWith=LevelValidator.class, description = "Minimum log level of messages printed to the log file. Not valid without --log-file.")
    private Level fileLevel = Level.FINE;

    @Parameter(names = {"--log-file"}, description = "Log messages to a seperate file")
    private String logFile = null;

    /**
     * Ensure a log Level is valid.
     */
    public static class LevelValidator implements IParameterValidator {
        @Override
        public void validate(String name, String value) throws ParameterException {
            // The knownLoggers are not publicaly exposed, the only way of checking value is valid are try/catch, or reflection...
            try {
                Level.parse(value);
            } catch (IllegalArgumentException e) {
                throw new ParameterException("Parameter " + name + " must be a valid log level", e);
            }
        }
    }

    /**
     * Convert a log Level to it's proper enum.
     */
    public static class LevelConverter implements IStringConverter<Level> {
        @Override
        public Level convert(String value) {
            return Level.parse(value);
        }
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
