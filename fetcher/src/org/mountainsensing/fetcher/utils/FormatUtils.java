package org.mountainsensing.fetcher.utils;

/**
 * Various Utils for Formatting Strings, such as indenting them.
 */
public class FormatUtils {

    /**
     * Prefix indicating a String is in HEX (0x).
     */
    public static final String HEX_PREFIX = "0x";

    /**
     * REGEX to validate hex input.
     */
    public static final String HEX_REGEX = "(" +  HEX_PREFIX + ")?" + "[0-9A-Fa-f]+";

    /**
     * Base for HEX numbers.
     */
    private static final int HEX_BASE = 16;

    /**
     * Indent a String with 4 spaces. Every line in the string will be indented.
     * @param s The String to indented
     * @return An indented version of the String
     */
    public static String indent(String s) {
        // (?m) treats a single string as multiline
        return s.replaceAll("(?m)^", "    ");
    }

    /**
     * Get the HEX representation of an Integer.
     * @param i The Integer
     * @return A String representing the HEX value of i, with a {@link FormatUtils.HEX_PREFIX}.
     */
    public static String toHex(int i) {
        return HEX_PREFIX + Integer.toHexString(i).toUpperCase();
    }

    /**
     * Check whether a String is a valid HEX representation of a number or not.
     * @param s The HEX String to check
     * @return True if the String could be parsed as HEX, false otherwise.
     */
    public static boolean isHex(String s) {
        // A hex string can start with an optional HEX_PREFIX, and then only be followed by digits until the end
        return s.matches("\\A" + HEX_REGEX + "\\z");
    }

    /**
     * Parse a number from a HEX String.
     * @param s The HEX String to parse, with an optional leading {@link FormatUtils.HEX_PREFIX}.
     * @return An Integer representing s.
     */
    public static int fromHex(String s) {
        if (s.startsWith(HEX_PREFIX)) {
            s = s.replaceFirst(HEX_PREFIX, "");
        }

        return Integer.parseInt(s, HEX_BASE);
    }
}