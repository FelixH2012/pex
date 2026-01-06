package de.felix.permissions.application.usecase.group;

import de.felix.permissions.application.port.GroupRepositoryPort;
import de.felix.permissions.group.Group;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CheckGroupPermissionUseCaseTest {

    private GroupRepositoryPort mockRepository;
    private CheckGroupPermissionUseCase useCase;

    @BeforeEach
    void setUp() {
        mockRepository = Mockito.mock(GroupRepositoryPort.class);
        useCase = new CheckGroupPermissionUseCase(mockRepository);
    }

    @Test
    void canModify_ReturnsTrueWhenHigherPriority() {
        Group owner = new Group("Owner", "[Owner]", 100, Set.of());
        Group admin = new Group("Admin", "[Admin]", 50, Set.of());

        when(mockRepository.getGroup("Owner")).thenReturn(Optional.of(owner));
        when(mockRepository.getGroup("Admin")).thenReturn(Optional.of(admin));

        assertTrue(useCase.canModify("Owner", "Admin"));
    }

    @Test
    void canModify_ReturnsFalseWhenLowerPriority() {
        Group owner = new Group("Owner", "[Owner]", 100, Set.of());
        Group admin = new Group("Admin", "[Admin]", 50, Set.of());

        when(mockRepository.getGroup("Owner")).thenReturn(Optional.of(owner));
        when(mockRepository.getGroup("Admin")).thenReturn(Optional.of(admin));

        assertFalse(useCase.canModify("Admin", "Owner"));
    }

    @Test
    void canModify_ReturnsFalseWhenGroupNotFound() {
        when(mockRepository.getGroup("NonExistent")).thenReturn(Optional.empty());
        when(mockRepository.getGroup("Owner")).thenReturn(Optional.of(new Group("Owner", "", 100, Set.of())));

        assertFalse(useCase.canModify("NonExistent", "Owner"));
        assertFalse(useCase.canModify("Owner", "NonExistent"));
    }

    @Test
    void hasPermission_ReturnsTrueForExactMatch() {
        Group admin = new Group("Admin", "[Admin]", 50, Set.of("pex.group.create"));
        when(mockRepository.getGroup("Admin")).thenReturn(Optional.of(admin));

        assertTrue(useCase.hasPermission("Admin", "pex.group.create"));
    }

    @Test
    void hasPermission_ReturnsTrueForWildcard() {
        Group admin = new Group("Admin", "[Admin]", 50, Set.of("pex.group.*"));
        when(mockRepository.getGroup("Admin")).thenReturn(Optional.of(admin));

        assertTrue(useCase.hasPermission("Admin", "pex.group.create"));
        assertTrue(useCase.hasPermission("Admin", "pex.group.delete"));
    }

    @Test
    void hasPermission_ReturnsFalseForNoMatch() {
        Group admin = new Group("Admin", "[Admin]", 50, Set.of("pex.user.set"));
        when(mockRepository.getGroup("Admin")).thenReturn(Optional.of(admin));

        assertFalse(useCase.hasPermission("Admin", "pex.group.create"));
    }

    @Test
    void hasPermission_ReturnsFalseForNonExistentGroup() {
        when(mockRepository.getGroup("NonExistent")).thenReturn(Optional.empty());

        assertFalse(useCase.hasPermission("NonExistent", "pex.group.create"));
    }
}
