package org.mountainsensing.fetcher.utils;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * A wrapper class that can provide context for logging.
 */
public class ContextFormatter {

    /**
     * The message format to use when no context information is set.
     */
    private static final MessageFormat MESSAGE_FORMAT = new MessageFormat("{0} [{1}]: {2}\n");

    /**
     * The message format to use when context information is set.
     */
    private static final MessageFormat CONTEXT_FORMAT = new MessageFormat("{0} [{1}] {3}: {2}\n");

    /**
     * The context information.
     * Can be null.
     */
    private URI context;

    /**
     * Set the context of any Console or File formatters.
     * @param context The context of further logs.
     */
    public void setContext(URI context) {
        this.context = context;        
    }

    /**
     * Clear the context of any Console or File formatters.
     */
    public void clearContext() {
        setContext(null);
    }

    /**
     * Get a formatter suitable for Console Logging. This is more terse than File logging.
     * @return A formatter for Console logging.
     */
    public Formatter getConsoleFormatter() {
        return new ConsoleFormatter();
    }

    /**
     * Get a formatter suitable for File Logging. This is more verbose than Console logging.
     * @return A formatter for File logging.
     */
    public Formatter getFileFormatter() {
        return new FileFormatter();
    }

    protected String format(String dateFormat, LogRecord record) {
        Object[] arguments = new Object[5];
        
        arguments[0] = new UTCDateFormat(dateFormat).format(new Date(record.getMillis()));
        arguments[1] = record.getLevel();
        arguments[2] = new MessageFormat(record.getMessage()).format(record.getParameters());

        MessageFormat format = MESSAGE_FORMAT;
        
        if (context != null) {
            format = CONTEXT_FORMAT;
            arguments[3] = context.getHost();
        } 
        
        return format.format(arguments);
    }

    /**
     * A formatter suitable for Console logging.
     */
    public class ConsoleFormatter extends Formatter {

        /**
         * The date format to use for logging.
         */
        private static final String DATE_FORMAT = "HH:mm:ss";

        @Override
        public String format(LogRecord record) {
            return ContextFormatter.this.format(DATE_FORMAT, record);
        }
    }

    /**
     * A formatter suitable for Console logging.
     */
    public class FileFormatter extends Formatter {

        /**
         * The date format to use for logging.
         */
        private static final String DATE_FORMAT = "YYYY-MM-dd HH:mm:ss z";

        @Override
        public String format(LogRecord record) {
            return ContextFormatter.this.format(DATE_FORMAT, record);
        }
    }
}
