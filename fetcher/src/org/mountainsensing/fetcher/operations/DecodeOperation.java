package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.Parameter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mountainsensing.fetcher.Operation;

/**
 * Abstract operation for decoding data.
 * This is typically used to decode protocol buffer encoded things.
 * Supports reading from stdin, or a file.
 */
public abstract class DecodeOperation extends Operation {

    private static final Logger log = Logger.getLogger(SampleOperation.class.getName());

    /**
     * Context to use for stdin.
     */
    private static final String STDIN_CONTEXT = "stdin";
    
    @Parameter(names = {"-f", "--file"}, description = "File to read from. Reads from stdin if not specified.")
    private String file = null;

    @Override
    public void perform() {
        setContext(file != null ? file : STDIN_CONTEXT);
        
        try {        
            InputStream pb;

            if (file != null) {
                pb = new FileInputStream(file);
            } else {
                pb = System.in;
            }
            
            print(pb);
            
        } catch (IOException e) {
            log.log(Level.WARNING, e.getMessage(), e);
        }

        clearContext();
    }
    
    /**
     * Decode and print the encoded data.
     * @param stream A stream of the encoded data.
     * @throws IOException An any I/O errors.
     */
    protected abstract void print(InputStream stream) throws IOException;
}
