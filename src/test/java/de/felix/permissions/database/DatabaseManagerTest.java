package de.felix.permissions.database;

import de.felix.permissions.Permissions;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Connection;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DatabaseManagerTest {

    @Mock
    private Permissions plugin;

    @Mock
    private FileConfiguration config;

    @Mock
    private Logger logger;

    private DatabaseManager databaseManager;

    @BeforeEach
    void setUp() {
        when(plugin.getConfig()).thenReturn(config);
        when(plugin.getLogger()).thenReturn(logger);
        databaseManager = new DatabaseManager(plugin);
    }

    @Test
    void testConnectWithH2Success() throws Exception {
        // Use H2 in-memory database for testing
        when(config.getString("database.jdbc-url")).thenReturn("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        when(config.getString("database.username")).thenReturn("sa");
        when(config.getString("database.password")).thenReturn("");

        assertDoesNotThrow(() -> databaseManager.connect());

        // Verify connection works
        Connection connection = databaseManager.getConnection();
        assertNotNull(connection);
        assertFalse(connection.isClosed());
        connection.close();

        databaseManager.disconnect();
    }

    @Test
    void testConnectWithInvalidCredentials() {
        // Invalid MySQL connection - should throw DatabaseConnectionException
        when(config.getString("database.jdbc-url")).thenReturn("jdbc:mysql://localhost:3306/nonexistent");
        when(config.getString("database.username")).thenReturn("invalid_user");
        when(config.getString("database.password")).thenReturn("wrong_password");

        DatabaseConnectionException exception = assertThrows(
                DatabaseConnectionException.class,
                () -> databaseManager.connect());

        assertNotNull(exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    void testGetConnectionWithoutConnect() {
        // Should throw SQLException when not connected
        assertThrows(java.sql.SQLException.class, () -> databaseManager.getConnection());
    }

    @Test
    void testDisconnect() throws Exception {
        when(config.getString("database.jdbc-url")).thenReturn("jdbc:h2:mem:testdb2;DB_CLOSE_DELAY=-1");
        when(config.getString("database.username")).thenReturn("sa");
        when(config.getString("database.password")).thenReturn("");

        databaseManager.connect();
        databaseManager.disconnect();

        // After disconnect, getConnection should throw
        assertThrows(java.sql.SQLException.class, () -> databaseManager.getConnection());
    }

    @Test
    void testTablesCreatedOnConnect() throws Exception {
        when(config.getString("database.jdbc-url")).thenReturn("jdbc:h2:mem:testdb3;DB_CLOSE_DELAY=-1");
        when(config.getString("database.username")).thenReturn("sa");
        when(config.getString("database.password")).thenReturn("");

        databaseManager.connect();

        try (Connection connection = databaseManager.getConnection();
                var statement = connection.createStatement()) {

            // Check if tables exist by querying them
            var groupsResult = statement.executeQuery(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'PERMISSIONS_GROUPS'");
            groupsResult.next();
            assertEquals(1, groupsResult.getInt(1), "permissions_groups table should exist");

            var playersResult = statement.executeQuery(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'PERMISSIONS_PLAYERS'");
            playersResult.next();
            assertEquals(1, playersResult.getInt(1), "permissions_players table should exist");
        }

        databaseManager.disconnect();
    }
}
