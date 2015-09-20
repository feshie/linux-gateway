package org.mountainsensing.fetcher.utils;

/**
 * Various Utils for Formatting Strings, such as indenting them.
 */
public class FormatUtils {

    /**
     * Indent a String with 4 spaces. Every line in the string will be indented.
     * @param s The String to indented
     * @return An indented version of the String
     */
    public static String indent(String s) {
        // (?m) treats a single string as multiline
        return s.replaceAll("(?m)^", "    ");
    }
}