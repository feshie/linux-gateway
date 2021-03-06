package org.mountainsensing.fetcher;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.mountainsensing.fetcher.utils.ContextFormatter;

/**
 * Class representing operations. They can be preformed,
 * and provide means of setting the logger context.
 */
public abstract class Operation {

    // Hidden to reduce clutter
    @Parameter(names = {"-h", "--help"}, description = "Show usage help and exit", help = true, hidden = true)
    private boolean help = false;

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
     * Check if usage help specific to this operation should be displayed.
     * @return True if it should be, false otherwise.
     */
    protected boolean shouldShowHelp() {
        return help;
    }

    /**
     * Perform the operation.
     * This is a blocking (ie synchronous) call, and may do
     * a substantial amount of IO (ie be very long)
     * @param timeout A requested timeout for the operation to complete, in seconds.
     * @param retries The number of times this operation can retry before aborting.
     */
    public abstract void perform(int timeout, int retries);

    /**
     * Perform any validation required.
     * This can be used to validate command line parameters.
     * This should be called before any call to {@link #perform()}.
     * @throws Exception If the validation failed.
     */
    public void validate() throws Exception, ParameterException {

    }
}