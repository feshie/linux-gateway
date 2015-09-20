package org.mountainsensing.fetcher.utils;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * DateFormat that prints the time and date, with the timezone set to UTC.
 */
public class UTCDateFormat extends SimpleDateFormat {

    public static final long serialVersionUID = 1;

    /**
     * Name / ID of the UTC TimeZone.
     */
    private static final String UTC = "UTC";

    /**
     * The default date / time format to use.
     */
    private static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss z";

    /**
     * Create a new UTCDateFormat with a default format,
     * and the timezone set to UTC.
     */
    public UTCDateFormat() {
        this(DEFAULT_FORMAT);
    }

    /**
     * Create a new UTCDateFormat with a given format,
     * and the timezone set to UTC.
     * @param format The format String to use.
     */
    public UTCDateFormat(String format) {
        super(format);
        setTimeZone(TimeZone.getTimeZone(UTC));
    }
}