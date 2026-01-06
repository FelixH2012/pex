package de.felix.permissions.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TimeParserTest {

    @Test
    public void testParseDuration() {
        assertEquals(345600000L, TimeParser.parseDuration("4 days"));
        assertEquals(420000L, TimeParser.parseDuration("7 minutes"));
        assertEquals(23000L, TimeParser.parseDuration("23 seconds"));

        long expected = 345600000L + 420000L + 23000L;
        assertEquals(expected, TimeParser.parseDuration("4 days, 7 minutes and 23 seconds"));
    }

    @Test
    public void testInvalidDuration() {
        assertThrows(IllegalArgumentException.class, () -> TimeParser.parseDuration("invalid"));
        assertThrows(IllegalArgumentException.class, () -> TimeParser.parseDuration(""));
    }
}
