package org.mountainsensing.fetcher.utils;

import java.text.FieldPosition;
import java.util.Date;

/**
 *
 */
public class UTCEpochDateFormat extends UTCDateFormat {

    public static final long serialVersionUID = 1;
    
    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        //System.out.println("Date stuff");
        //return toAppendTo.append(" Timethyings");
        //return super.format(date, toAppendTo.append(new EpochDate(date.getTime()).getEpoch()).append(" aka "), fieldPosition);
        return super.format(date, toAppendTo, fieldPosition);
    }
}