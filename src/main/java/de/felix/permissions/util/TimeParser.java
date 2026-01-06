package de.felix.permissions.util;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeParser {

    private static final Pattern PATTERN_DAYS = Pattern.compile("(\\d+)\\s*(d|day|days)");
    private static final Pattern PATTERN_HOURS = Pattern.compile("(\\d+)\\s*(h|hour|hours)");
    private static final Pattern PATTERN_MINUTES = Pattern.compile("(\\d+)\\s*(m|min|minute|minutes)");
    private static final Pattern PATTERN_SECONDS = Pattern.compile("(\\d+)\\s*(s|sec|second|seconds)");

    /**
     * Parses a duration string into ms.
     *
     * @param durationStr The duration string to parse.
     * @return The duration in milliseconds.
     * @throws IllegalArgumentException If the format is invalid or no duration
     *                                  could be parsed.
     */
    public static long parseDuration(String durationStr) {
        if (durationStr == null || durationStr.isBlank()) {
            throw new IllegalArgumentException("Duration string cannot be empty");
        }

        long totalMillis = 0;
        String normalized = durationStr.toLowerCase().replace(",", " ").replace(" and ", " ");

        boolean matched = false;

        Matcher daysMatcher = PATTERN_DAYS.matcher(normalized);
        if (daysMatcher.find()) {
            totalMillis += Duration.ofDays(Long.parseLong(daysMatcher.group(1))).toMillis();
            matched = true;
        }

        Matcher hoursMatcher = PATTERN_HOURS.matcher(normalized);
        if (hoursMatcher.find()) {
            totalMillis += Duration.ofHours(Long.parseLong(hoursMatcher.group(1))).toMillis();
            matched = true;
        }

        Matcher minutesMatcher = PATTERN_MINUTES.matcher(normalized);
        if (minutesMatcher.find()) {
            totalMillis += Duration.ofMinutes(Long.parseLong(minutesMatcher.group(1))).toMillis();
            matched = true;
        }

        Matcher secondsMatcher = PATTERN_SECONDS.matcher(normalized);
        if (secondsMatcher.find()) {
            totalMillis += Duration.ofSeconds(Long.parseLong(secondsMatcher.group(1))).toMillis();
            matched = true;
        }

        if (!matched) {
            throw new IllegalArgumentException("Invalid duration format: " + durationStr);
        }

        return totalMillis;
    }
}
