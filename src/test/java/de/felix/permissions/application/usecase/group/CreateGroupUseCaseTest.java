package de.felix.permissions.application.usecase.group;

import de.felix.permissions.application.port.GroupRepositoryPort;
import de.felix.permissions.group.Group;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateGroupUseCaseTest {

    private GroupRepositoryPort mockRepository;
    private CreateGroupUseCase useCase;

    @BeforeEach
    void setUp() {
        mockRepository = Mockito.mock(GroupRepositoryPort.class);
        useCase = new CreateGroupUseCase(mockRepository);
    }

    @Test
    void execute_CreatesGroupWithBasicParams() {
        Group result = useCase.execute("Admin", "[Admin]");

        assertEquals("Admin", result.name());
        assertEquals("[Admin]", result.prefix());
        assertEquals(0, result.priority());
        assertTrue(result.permissions().isEmpty());

        verify(mockRepository, times(1)).createGroup(any(Group.class));
    }

    @Test
    void execute_CreatesGroupWithFullParams() {
        Set<String> perms = Set.of("pex.group.create", "pex.user.set");
        Group result = useCase.execute("Admin", "[Admin]", 50, perms);

        assertEquals("Admin", result.name());
        assertEquals("[Admin]", result.prefix());
        assertEquals(50, result.priority());
        assertEquals(perms, result.permissions());

        verify(mockRepository, times(1)).createGroup(any(Group.class));
    }
}
