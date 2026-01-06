package de.felix.permissions.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.felix.permissions.group.Group;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class GroupRepositoryTest {

    private HikariDataSource dataSource;
    @Mock
    private DatabaseManager databaseManager;
    private GroupRepository groupRepository;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);

        // Setup in-memory H2 database
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        config.setDriverClassName("org.h2.Driver");
        config.setUsername("sa");
        config.setPassword("");
        dataSource = new HikariDataSource(config);

        // Initialize tables
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS permissions_groups");
            statement.execute(
                    "CREATE TABLE permissions_groups (name VARCHAR(64) PRIMARY KEY, prefix VARCHAR(64) NOT NULL, priority INT DEFAULT 0, permissions TEXT DEFAULT '')");
        }

        // Mock DatabaseManager to return our H2 connection
        when(databaseManager.getConnection()).thenAnswer(invocation -> dataSource.getConnection());

        groupRepository = new GroupRepository(databaseManager);
    }

    @AfterEach
    void tearDown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Test
    void createGroup_InsertsIntoDatabase() {
        // Arrange
        Group group = new Group("Admin", "[Admin]");

        // Act
        groupRepository.createGroup(group);

        // Assert
        Optional<Group> retrieved = groupRepository.getGroup("Admin");
        assertTrue(retrieved.isPresent());
        assertEquals("Admin", retrieved.get().name());
        assertEquals("[Admin]", retrieved.get().prefix());
    }

    @Test
    void createGroup_WithPriorityAndPermissions() {
        // Arrange
        Group group = new Group("Owner", "[Owner]", 100, Set.of("pex.group.*", "pex.user.*"));

        // Act
        groupRepository.createGroup(group);

        // Assert
        Optional<Group> retrieved = groupRepository.getGroup("Owner");
        assertTrue(retrieved.isPresent());
        assertEquals("Owner", retrieved.get().name());
        assertEquals("[Owner]", retrieved.get().prefix());
        assertEquals(100, retrieved.get().priority());
        assertTrue(retrieved.get().permissions().contains("pex.group.*"));
        assertTrue(retrieved.get().permissions().contains("pex.user.*"));
    }

    @Test
    void deleteGroup_RemovesFromDatabase() {
        // Arrange
        Group group = new Group("Moderator", "[Mod]");
        groupRepository.createGroup(group);

        // Act
        groupRepository.deleteGroup("Moderator");

        // Assert
        Optional<Group> retrieved = groupRepository.getGroup("Moderator");
        assertFalse(retrieved.isPresent());
    }

    @Test
    void updateGroup_UpdatesInDatabase() {
        // Arrange
        Group group = new Group("VIP", "[VIP]", 10, Set.of());
        groupRepository.createGroup(group);
        Group updatedGroup = new Group("VIP", "[SuperVIP]", 20, Set.of("vip.special"));

        // Act
        groupRepository.updateGroup(updatedGroup);

        // Assert
        Optional<Group> retrieved = groupRepository.getGroup("VIP");
        assertTrue(retrieved.isPresent());
        assertEquals("[SuperVIP]", retrieved.get().prefix());
        assertEquals(20, retrieved.get().priority());
        assertTrue(retrieved.get().permissions().contains("vip.special"));
    }

    @Test
    void getAllGroups_ReturnsAllGroups() {
        // Arrange
        groupRepository.createGroup(new Group("G1", "P1"));
        groupRepository.createGroup(new Group("G2", "P2"));

        // Act
        List<Group> groups = groupRepository.getAllGroups();

        // Assert
        assertEquals(2, groups.size());
    }
}
