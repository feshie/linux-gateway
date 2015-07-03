package org.mountainsensing.fetcher;

import java.util.Date;

/**
 * Date object with convenience methods for getting and setting the UNIX epoch.
 * 
 * The UNIX epoch is defined as the number of seconds since 1970/01/01 00:00:00.
 */
public class EpochDate extends Date {
    
    public static final long serialVersionUID = 1;
    
    /**
     * Scaling factor between the UNIX epoch and the Java representation of time.
     * (factor between seconds and ms).
     */
    private static final int FACTOR = 1000;
    
    /**
     * Create a new Date representing the current system time.
     */
    public EpochDate() {
        super();
    }
    
    /**
     * Create a new Date representing a given UNIX epoch. 
     * @param epoch The UNIX epoch to represent.
     */
    public EpochDate(long epoch) {
        super(epochToMs(epoch));
    }
    
    /**
     * Get the UNIX epoch.
     * @return The UNIX epoch represented by this.
     */
    public long getEpoch() {
        return msToEpoch(getTime());     
    }
    
    /**
     * Set the UNIX epoch.
     * @param epoch The UNIX epoch to represent.
     */
    public void setEpoch(long epoch) {
        setTime(epochToMs(epoch));
    }

    /**
     * Convert java system time to UNIX Epoch. 
     * @param ms The Java System Time.
     * @return The corresponding UNIX Epoch.
     */
    private static long msToEpoch(long ms) {
        return ms / FACTOR;
    }

    /**
     * Convert UNIX Epoch to Java System Time.
     * @param epoch The UNIX Epoch.
     * @return The corresponding Java System Time.
     */
    private static long epochToMs(long epoch) {
        return epoch * FACTOR;        
    }
}
