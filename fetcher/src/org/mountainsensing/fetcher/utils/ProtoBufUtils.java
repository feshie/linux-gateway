package org.mountainsensing.fetcher.utils;

import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import java.io.IOException;
import java.util.Collection;
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
        StringBuilder result = new StringBuilder();
        String separator = "";
        for (Descriptors.FieldDescriptor descriptor : message.getAllFields().keySet()) {

            result.append(separator);
            separator = System.lineSeparator();

            // If the field is repeated, print all them at once
            if (descriptor.isRepeated()) {
                String sep2 = "";
                for (Object field : (Collection)message.getAllFields().get(descriptor)) {
                    result.append(sep2);
                    sep2 = System.lineSeparator();
                    printField(message, descriptor, field, overrides, result);
                };

            // Else just print it
            } else {
                printField(message, descriptor, message.getAllFields().get(descriptor), overrides, result);
            }
        }

        return result.toString();
    }

    /**
     * Print a single field to a StringBuilder
     * @param <M> The type of the ProtocolBuffer containing the field.
     * @param message The ProtocolBuffer containing the field.
     * @param descriptor The descriptor for the Field
     * @param field The actual field to print
     * @param overrides Any overrides for printing fields
     * @param toAppend The StringBuilder to append to
     * @throws IOException If the field is a protocol buffer, and toString() throws one.
     */
    private static <M extends GeneratedMessage> void printField(M message, Descriptors.FieldDescriptor descriptor, Object field, Map<Integer, ? extends FieldOverride<M>> overrides, StringBuilder toAppend) throws IOException {
        toAppend.append(descriptor.getName()).append(": ");

        // If there's an override for this field, use that
        // The java protobuf implementation seems to be 0 indexed, instead of 1 indexed as per the proto defintions.
        if (overrides != null && overrides.containsKey(descriptor.getIndex() + 1)) {
            toAppend.append(overrides.get(descriptor.getIndex() + 1).toString(message));

        // If this field is a protobuf, print it ourselves
        } else if (field instanceof GeneratedMessage) {
            toAppend.append(System.lineSeparator()).append(FormatUtils.indent(ProtoBufUtils.toString((GeneratedMessage) field)));

        // Just a simple field, call toString() on it
        } else {
            toAppend.append(field.toString());
        }
    }
}