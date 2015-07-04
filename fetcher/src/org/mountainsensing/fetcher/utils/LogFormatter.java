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
	private static final MessageFormat messageFormat = new MessageFormat("{0,date,HH:mm:ss} [{1}]: {2}\n");

    /**
     * The message format to use when context information is set.
     */
    private static final MessageFormat contextFormat = new MessageFormat("{0,date,HH:mm:ss} [{1}] {3}: {2}\n");

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
        
        arguments[0] = new Date(record.getMillis());
        arguments[1] = record.getLevel();
        arguments[2] = new MessageFormat(record.getMessage()).format(record.getParameters());

        MessageFormat format = messageFormat;
        
        if (context != null) {
            format = contextFormat;
            arguments[3] = context.getHost();
        } 
        
        return format.format(arguments);
	}	
}
