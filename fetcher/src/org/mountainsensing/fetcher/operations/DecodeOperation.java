package org.mountainsensing.fetcher.operations;

import com.beust.jcommander.Parameter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;
import org.mountainsensing.fetcher.Operation;

/**
 * Abstract operation for decoding data.
 * This is typically used to decode protocol buffer encoded things and serial dumps.
 * Supports reading from stdin, or multiple files.
 */
public abstract class DecodeOperation extends Operation {

    private static final Logger log = Logger.getLogger(SampleOperation.class.getName());

    /**
     * Context to use for stdin.
     */
    private static final String STDIN_NAME = "stdin";
    
    @Parameter(description = "File(s) to read from. Reads from stdin if none specified.", required = false, arity = 1)
    private List<String> files = new ArrayList<>();

    @Parameter(names = {"-s", "--is-serial-dump"}, description = "Input is a hex encoded serial dump, as produced by z1-coap-serial-dump.")
    private boolean isDump = false;

    @Override
    public void perform() {
        // Store humman readable names for the inputs - usefull for setting the context
        Map<InputStream, String> inputs = new LinkedHashMap<>();

        try {
            // If we have files use those, otherwise read from stdin
            if (!files.isEmpty()) {
                for (String file : files) {
                    inputs.put(new FileInputStream(file), file);
                }

            } else {
                inputs.put(System.in, STDIN_NAME);
            }

            for (InputStream in : inputs.keySet()) {
                if (isDump) {
                    decodeDump(in, inputs.get(in));

                // Otherwise binary
                } else {
                    setContext(inputs.get(in));
                    decodeBinary(in);
                }
            }

        } catch (IOException e) {
            log.log(Level.WARNING, e.getMessage(), e);
        }

        clearContext();
    }

    /**
     * Decode a serial dump.
     * @param in A stream to the serial dump.
     * @param name A human readable name for the input stream.
     * @throws IOException If an error occurs.
     */
    private void decodeDump(InputStream in, String name) throws IOException {
        boolean shouldDecode = false;
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(in));
        String line;

        while ((line = reader.readLine()) != null) {

            setContext(name + ":" + reader.getLineNumber());

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
