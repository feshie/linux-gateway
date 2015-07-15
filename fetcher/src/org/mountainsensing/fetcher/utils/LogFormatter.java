package org.mountainsensing.fetcher.utils;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * A simple LogFormatter that can provide context information in the form of a URI.
 */
public class LogFormatter extends Formatter {

    /**
     * The message format to use when no context information is set.
     */
    private static final MessageFormat MESSAGE_FORMAT = new MessageFormat("{0} [{1}]: {2}\n");

    /**
     * The message format to use when context information is set.
     */
    private static final MessageFormat CONTEXT_FORMAT = new MessageFormat("{0} [{1}] {3}: {2}\n");

    /**
     * The date format to use for logging.
     */
    private static final String DATE_FORMAT = "HH:mm:ss";

    /**
     * The context information.
     * Can be null.
     */
    private URI context;

    /**
     * Set the context of this formatter.
     * @param context The context of further logs.
     */
    public void setContext(URI context) {
        this.context = context;        
    }

    /**
     * Clear the context of the formatter.
     */
    public void clearContext() {
        setContext(null);
    }
    
	@Override
    public String format(LogRecord record) {
        Object[] arguments = new Object[4];
        
        arguments[0] = new UTCDateFormat(DATE_FORMAT).format(new Date(record.getMillis()));
        arguments[1] = record.getLevel();
        arguments[2] = new MessageFormat(record.getMessage()).format(record.getParameters());

        MessageFormat format = MESSAGE_FORMAT;
        
        if (context != null) {
            format = CONTEXT_FORMAT;
            arguments[3] = context.getHost();
        } 
        
        return format.format(arguments);
	}	
}
