package org.mountainsensing.fetcher;

import org.mountainsensing.fetcher.utils.ContextFormatter;

/**
 * Class representing operations. They can be preformed,
 * and provide means of setting the logger context.
 */
public abstract class Operation {

    /**
     * The formatter to use for setting the context.
     */
    private static ContextFormatter formatter;

    /**
     * Set the formatter to use for setting the context.
     * @param formatter The formatter to use.
     */
    protected static void setContextFormatter(ContextFormatter formatter) {
        Operation.formatter = formatter;
    }

    /**
     * Set the current context.
     * @param context The context
     */
    protected void setContext(String context) {
        formatter.setContext(context);
    }

    /**
     * Clear the current context.
     */
    protected void clearContext() {
        formatter.clearContext();
    }

    /**
     * Perform the operation.
     * This is a blocking (ie synchronous) call, and may do
     * a substantial amount of IO (ie be very long)
     */
    public abstract void perform();

}