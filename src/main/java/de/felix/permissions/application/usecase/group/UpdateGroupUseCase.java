package de.felix.permissions.application.usecase.group;

import de.felix.permissions.application.port.GroupRepositoryPort;
import de.felix.permissions.group.Group;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class UpdateGroupUseCase {

    private final GroupRepositoryPort groupRepository;

    public UpdateGroupUseCase(GroupRepositoryPort groupRepository) {
        this.groupRepository = groupRepository;
    }

    public Group execute(String name, String prefix) {
        Optional<Group> existing = groupRepository.getGroup(name);
        int priority = existing.map(Group::priority).orElse(0);
        Set<String> permissions = existing.map(Group::permissions).orElse(Set.of());
        Group group = new Group(name, prefix, priority, permissions);
        groupRepository.updateGroup(group);
        return group;
    }

    public Group execute(String name, String prefix, int priority, Set<String> permissions) {
        Group group = new Group(name, prefix, priority, permissions);
        groupRepository.updateGroup(group);
        return group;
    }

    public Group setPriority(String name, int priority) {
        Optional<Group> existing = groupRepository.getGroup(name);
        if (existing.isEmpty()) {
            return null;
        }
        Group group = new Group(name, existing.get().prefix(), priority, existing.get().permissions());
        groupRepository.updateGroup(group);
        return group;
    }

    public Group addPermission(String name, String permission) {
        Optional<Group> existing = groupRepository.getGroup(name);
        if (existing.isEmpty()) {
            return null;
        }
        Set<String> newPermissions = new HashSet<>(existing.get().permissions());
        newPermissions.add(permission);
        Group group = new Group(name, existing.get().prefix(), existing.get().priority(), newPermissions);
        groupRepository.updateGroup(group);
        return group;
    }

    public Group removePermission(String name, String permission) {
        Optional<Group> existing = groupRepository.getGroup(name);
        if (existing.isEmpty()) {
            return null;
        }
        Set<String> newPermissions = new HashSet<>(existing.get().permissions());
        newPermissions.remove(permission);
        Group group = new Group(name, existing.get().prefix(), existing.get().priority(), newPermissions);
        groupRepository.updateGroup(group);
        return group;
    }
}
