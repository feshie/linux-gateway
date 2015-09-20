package org.mountainsensing.fetcher.utils;

import java.text.FieldPosition;
import java.util.Date;

/**
 * Format that prints a Date with the associated UNIX epoch.
 */
public class UTCEpochDateFormat extends UTCDateFormat {

    public static final long serialVersionUID = 1;

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        return super.format(date, toAppendTo.append(new EpochDate(date).getEpoch()).append(" ("), fieldPosition).append(")");
    }
}