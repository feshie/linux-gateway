package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.Parameter;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;
import org.mountainsensing.fetcher.Operation;

/**
 * Abstract operation for decoding data.
 * This is typically used to decode protocol buffer encoded things and serial dumps.
 * Supports reading from stdin, or a file.
 */
public abstract class DecodeOperation extends Operation {

    private static final Logger log = Logger.getLogger(SampleOperation.class.getName());

    /**
     * Context to use for stdin.
     */
    private static final String STDIN_CONTEXT = "stdin";
    
    @Parameter(description = "File(s) to read from. Reads from stdin if none specified.", required = false, arity = 1)
    private List<String> files = new ArrayList<>();

    @Parameter(names = {"-s", "--is-serial-dump"}, description = "Input is a hex encoded serial dump, as produced by z1-coap-serial-dump.")
    private boolean isDump = false;

    @Override
    public void perform() {
        boolean fromFile = !files.isEmpty();
        String file = files.get(0);

        setContext(fromFile ? file : STDIN_CONTEXT);
        
        try {        
            InputStream in;

            if (fromFile) {
                in = new FileInputStream(file);
            } else {
                in = System.in;
            }

            if (isDump) {
                decodeDump(in);

            // Otherwise binary
            } else {
                decodeBinary(in);
            }
            
        } catch (IOException e) {
            log.log(Level.WARNING, e.getMessage(), e);
        }

        clearContext();
    }

    /**
     * Decode a serial dump.
     * @param in A stream to the serial dump.
     * @throws IOException If an error occurs.
     */
    private void decodeDump(InputStream in) throws IOException {
        boolean shouldDecode = false;
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;

        while ((line = reader.readLine()) != null) {

            if (line.isEmpty()) {
                continue;
            }

            if (line.equals(startMarker())) {
                shouldDecode = true;
                continue;
            }

            if (line.equals(endMarker())) {
                shouldDecode = false;
                continue;
            }

            if (!shouldDecode) {
                continue;
            }

            decode(DatatypeConverter.parseHexBinary(line));
        }
    }

    /**
     * Decode a binary thing.
     * @param in A stream to the binary things.
     * @throws IOException If an error occurs.
     */
    private void decodeBinary(InputStream in) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream binary = new ByteArrayOutputStream();

        while ((bytesRead = in.read(buffer)) != -1) {
            binary.write(buffer, 0, bytesRead);
        }

        decode(binary.toByteArray());
    }

    /**
     * The start marker used to delimit this field in a serial dump.
     * @return 
     */
    protected abstract String startMarker();

    /**
     * The start marker used to delimit this field in a serial dump.
     * @return 
     */
    protected abstract String endMarker();

    /**
     * Decode and print the encoded data.
     * @param data The encoded data.
     * @throws IOException An any I/O errors.
     */
    protected abstract void decode(byte[] data) throws IOException;
}
