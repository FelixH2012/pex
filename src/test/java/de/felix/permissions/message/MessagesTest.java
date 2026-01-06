package de.felix.permissions.message;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessagesTest {

    private Messages messages;

    @BeforeEach
    void setUp() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("messages.test-simple", "&aHello World");
        config.set("messages.test-placeholder", "&ePlayer {player} joined group {group}");
        config.set("messages.test-multiple-same", "{player} and {player} again");
        config.set("messages.test-no-color", "Plain text message");

        messages = new Messages(config);
    }

    @Test
    void get_TranslatesColorCodes() {
        String result = messages.getRaw("test-simple");

        // & should be converted to § (color code)
        assertTrue(result.contains("§a"));
        assertTrue(result.contains("Hello World"));
    }

    @Test
    void get_ReplacesPlaceholders() {
        String result = messages.get("test-placeholder", "player", "Steve", "group", "Admin");

        assertTrue(result.contains("Steve"));
        assertTrue(result.contains("Admin"));
        assertFalse(result.contains("{player}"));
        assertFalse(result.contains("{group}"));
    }

    @Test
    void get_ReplacesMultipleSamePlaceholders() {
        String result = messages.get("test-multiple-same", "player", "Alex");

        // Both occurrences should be replaced
        assertEquals("Alex and Alex again", result);
    }

    @Test
    void get_ReturnsErrorForMissingKey() {
        String result = messages.get("non-existent-key");

        assertTrue(result.contains("Missing"));
        assertTrue(result.contains("non-existent-key"));
    }

    @Test
    void get_HandlesNoPlaceholders() {
        String result = messages.get("test-no-color");

        assertEquals("Plain text message", result);
    }

    @Test
    void get_HandlesOddNumberOfPlaceholderArgs() {
        // Should not crash with odd number of args
        String result = messages.get("test-placeholder", "player", "Steve", "group");

        assertTrue(result.contains("Steve"));
        // group placeholder not replaced because no value provided
        assertTrue(result.contains("{group}"));
    }

    @Test
    void constructor_LoadsDefaultsWhenNoMessagesSection() {
        YamlConfiguration emptyConfig = new YamlConfiguration();
        Messages emptyMessages = new Messages(emptyConfig);

        // Should have fallback defaults
        String result = emptyMessages.get("group-not-found", "group", "TestGroup");
        assertTrue(result.contains("TestGroup"));
    }
}
