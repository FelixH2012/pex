package de.felix.permissions.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TimeParserExtendedTest {

    @Test
    void parseDuration_Days() {
        assertEquals(86400000L, TimeParser.parseDuration("1 day"));
        assertEquals(86400000L, TimeParser.parseDuration("1 days"));
        assertEquals(86400000L, TimeParser.parseDuration("1d"));
        assertEquals(259200000L, TimeParser.parseDuration("3 days"));
    }

    @Test
    void parseDuration_Hours() {
        assertEquals(3600000L, TimeParser.parseDuration("1 hour"));
        assertEquals(3600000L, TimeParser.parseDuration("1 hours"));
        assertEquals(3600000L, TimeParser.parseDuration("1h"));
        assertEquals(7200000L, TimeParser.parseDuration("2 hours"));
    }

    @Test
    void parseDuration_Minutes() {
        assertEquals(60000L, TimeParser.parseDuration("1 minute"));
        assertEquals(60000L, TimeParser.parseDuration("1 minutes"));
        assertEquals(60000L, TimeParser.parseDuration("1m"));
        assertEquals(300000L, TimeParser.parseDuration("5 minutes"));
    }

    @Test
    void parseDuration_Seconds() {
        assertEquals(1000L, TimeParser.parseDuration("1 second"));
        assertEquals(1000L, TimeParser.parseDuration("1 seconds"));
        assertEquals(1000L, TimeParser.parseDuration("1s"));
        assertEquals(30000L, TimeParser.parseDuration("30 seconds"));
    }

    @Test
    void parseDuration_CombinedFormats() {
        // 1 day, 2 hours
        assertEquals(86400000L + 7200000L, TimeParser.parseDuration("1 day 2 hours"));
        // 2d 5h 30m
        assertEquals(172800000L + 18000000L + 1800000L, TimeParser.parseDuration("2d 5h 30m"));
    }

    @Test
    void parseDuration_ComplexFormat() {
        // 4 days, 7 minutes and 23 seconds (from task requirement)
        long expected = (4 * 86400000L) + (7 * 60000L) + (23 * 1000L);
        assertEquals(expected, TimeParser.parseDuration("4 days, 7 minutes and 23 seconds"));
    }

    @Test
    void parseDuration_CaseInsensitive() {
        assertEquals(3600000L, TimeParser.parseDuration("1 HOUR"));
        assertEquals(60000L, TimeParser.parseDuration("1 MINUTE"));
        assertEquals(86400000L, TimeParser.parseDuration("1 DAY"));
    }

    @Test
    void parseDuration_ThrowsOnInvalid() {
        assertThrows(IllegalArgumentException.class, () -> TimeParser.parseDuration(""));
        assertThrows(IllegalArgumentException.class, () -> TimeParser.parseDuration("invalid"));
        assertThrows(IllegalArgumentException.class, () -> TimeParser.parseDuration("abc days"));
    }
}
