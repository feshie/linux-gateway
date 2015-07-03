package org.mountainsensing.fetcher.utils;

import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 */
public class LogFormatter extends Formatter {
    
	private static final MessageFormat messageFormat = new MessageFormat("{0,date,HH:mm:ss} [{1}]: {2}\n");
	
	@Override
    public String format(LogRecord record) {
		Object[] arguments = new Object[3];
		arguments[0] = new Date(record.getMillis());
		arguments[1] = record.getLevel();
		arguments[2] = new MessageFormat(record.getMessage()).format(record.getParameters());
		return messageFormat.format(arguments);
	}	
}
