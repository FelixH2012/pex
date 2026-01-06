package de.felix.permissions.group;

import de.felix.permissions.application.usecase.group.GroupUseCases;
import de.felix.permissions.database.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupManagerTest {

    @Mock
    private GroupRepository groupRepository;

    private GroupManager groupManager;

    @BeforeEach
    void setUp() {
        when(groupRepository.getAllGroups()).thenReturn(Collections.emptyList());
        GroupUseCases groupUseCases = new GroupUseCases(groupRepository);
        groupManager = new GroupManager(groupUseCases);
    }

    @Test
    void createGroup_AddsToCacheAndRepository() {
        // Arrange
        String name = "Admin";
        String prefix = "[Admin]";

        // Act
        groupManager.createGroup(name, prefix);

        // Assert
        verify(groupRepository, times(1)).createGroup(any(Group.class));
        Optional<Group> cachedGroup = groupManager.getGroup(name);
        assertTrue(cachedGroup.isPresent());
        assertEquals(name, cachedGroup.get().name());
        assertEquals(prefix, cachedGroup.get().prefix());
    }

    @Test
    void createGroup_WithPriorityAndPermissions() {
        // Arrange
        String name = "Owner";
        String prefix = "[Owner]";
        int priority = 100;
        Set<String> permissions = Set.of("pex.group.*");

        // Act
        Group group = groupManager.createGroup(name, prefix, priority, permissions);

        // Assert
        verify(groupRepository, times(1)).createGroup(any(Group.class));
        assertEquals(priority, group.priority());
        assertTrue(group.permissions().contains("pex.group.*"));
    }

    @Test
    void createGroup_DoesNotAddSimpleDuplicate() {
        // Arrange
        String name = "Admin";
        String prefix = "[Admin]";
        groupManager.createGroup(name, prefix); // First creation

        // Act
        groupManager.createGroup(name, prefix); // Second creation

        // Assert
        verify(groupRepository, times(1)).createGroup(any(Group.class)); // Still only 1 call
    }

    @Test
    void deleteGroup_RemovesFromCacheAndRepository() {
        // Arrange
        String name = "Admin";
        groupManager.createGroup(name, "[Admin]");

        // Act
        groupManager.deleteGroup(name);

        // Assert
        verify(groupRepository, times(1)).deleteGroup(name);
        assertFalse(groupManager.getGroup(name).isPresent());
    }

    @Test
    void updateGroup_UpdatesCacheAndRepository() {
        // Arrange
        String name = "Admin";
        groupManager.createGroup(name, "[Admin]");
        String newPrefix = "[Owner]";

        // Act
        groupManager.updateGroup(name, newPrefix);

        // Assert
        verify(groupRepository, times(1)).updateGroup(any(Group.class));
        Optional<Group> updatedGroup = groupManager.getGroup(name);
        assertTrue(updatedGroup.isPresent());
        assertEquals(newPrefix, updatedGroup.get().prefix());
    }

    @Test
    void setPriority_UpdatesPriority() {
        // Arrange
        String name = "Admin";
        groupManager.createGroup(name, "[Admin]", 50, Set.of());
        when(groupRepository.getGroup(name)).thenReturn(Optional.of(new Group(name, "[Admin]", 50, Set.of())));

        // Act
        Group updated = groupManager.setPriority(name, 100);

        // Assert
        assertNotNull(updated);
        assertEquals(100, updated.priority());
    }

    @Test
    void addPermission_AddsToGroup() {
        // Arrange
        String name = "Admin";
        groupManager.createGroup(name, "[Admin]", 50, Set.of());
        when(groupRepository.getGroup(name)).thenReturn(Optional.of(new Group(name, "[Admin]", 50, Set.of())));

        // Act
        Group updated = groupManager.addPermission(name, "pex.group.create");

        // Assert
        assertNotNull(updated);
        assertTrue(updated.permissions().contains("pex.group.create"));
    }
}
