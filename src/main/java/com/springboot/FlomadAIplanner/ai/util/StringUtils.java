package com.springboot.FlomadAIplanner.ai.util;

/**
 * StringUtils provides utility methods for string manipulation.
 */
public class StringUtils {

    /**
     * Trims the string to the specified length and appends "..." if the string is longer than the specified length.
     *
     * @param str       the string to trim
     * @param maxLength the maximum length of the string
     * @return the trimmed string
     */
    public static String safeSubstring(String str, int maxLength) {
        if (str == null) return null;
        String trimmedString = str.substring(0, Math.min(maxLength, str.length()));
        return trimmedString + (str.length() > maxLength ? "..." : "");
    }

}