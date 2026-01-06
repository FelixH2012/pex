package de.felix.permissions.application.usecase.group;

import de.felix.permissions.application.port.GroupRepositoryPort;
import de.felix.permissions.group.Group;

import java.util.Set;

public class CreateGroupUseCase {

    private final GroupRepositoryPort groupRepository;

    public CreateGroupUseCase(GroupRepositoryPort groupRepository) {
        this.groupRepository = groupRepository;
    }

    public Group execute(String name, String prefix) {
        return execute(name, prefix, 0, Set.of());
    }

    public Group execute(String name, String prefix, int priority, Set<String> permissions) {
        Group group = new Group(name, prefix, priority, permissions);
        groupRepository.createGroup(group);
        return group;
    }
}
