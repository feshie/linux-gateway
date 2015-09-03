package org.mountainsensing.fetcher.utils;

import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import java.io.IOException;
import java.util.Map;

/**
 * Utilities for protocol buffers.
 */
public class ProtoBufUtils {

    /**
     * Class for overriding the string representation of a field.
     * See toString().
     * @param <M> The type of the message to which the field belongs.
     */
    public static abstract class FieldOverride<M> {

        /**
         * Get a string representation of a field.
         * @param message The message being decoded
         * @return A string representation of the associated field.
         * @throws IOException If the field could not be properly decoded.
         */
        public abstract String toString(M message) throws IOException;
    }
    
    /**
     * Get a string representation of a protocol buffer message
     * @param <M> The type of the Protocol Buffer
     * @param message The message
     * @return A String representing the message.
     * @throws java.io.IOException If the message could not be decoded properly.
     */
    public static <M extends GeneratedMessage> String toString(M message) throws IOException {
        return toString(message, null);
    }

    /**
     * Get a string representation of a protocol buffer message.
     * This method allows the output to be controlled through the use of FieldOverrides. The `toString()` method of a FieldOverride associated with 
     * an index will be called to print the filed with that index, instead of just calling `toString()` on the field.
     * @param <M> The type of the Protocol Buffer
     * @param message The message
     * @param overrides A map of 1 indexed indexes to FieldOverrides.
     * @return A String representing the message.
     * @throws java.io.IOException If the message could not be decoded properly.
     */
    public static <M extends GeneratedMessage> String toString(M message, Map<Integer, ? extends FieldOverride<M>> overrides) throws IOException {
        String result = new String();
        String separator = "";
        for (Descriptors.FieldDescriptor descriptor : message.getAllFields().keySet()) {

            result += separator + descriptor.getName() + ": ";
            
            separator = System.lineSeparator();

            // The java protobuf implementation seems to be 0 indexed, instead of 1 indexed as per the proto defintions.
            if (overrides != null && overrides.containsKey(descriptor.getIndex() + 1)) {
                result += overrides.get(descriptor.getIndex() + 1).toString(message);
            } else {
                result += message.getAllFields().get(descriptor).toString();
            }
        }

        return result;
    }
}
