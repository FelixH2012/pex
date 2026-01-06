package de.felix.permissions.application.usecase.group;

import de.felix.permissions.application.port.GroupRepositoryPort;
import de.felix.permissions.group.Group;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Simple orchestrator exposing all group-related use cases in one place.
 */
public class GroupUseCases {

    private final CreateGroupUseCase createGroup;
    private final DeleteGroupUseCase deleteGroup;
    private final UpdateGroupUseCase updateGroup;
    private final GetGroupUseCase getGroup;
    private final ListGroupsUseCase listGroups;
    private final CheckGroupPermissionUseCase checkPermission;

    public GroupUseCases(GroupRepositoryPort groupRepository) {
        this.createGroup = new CreateGroupUseCase(groupRepository);
        this.deleteGroup = new DeleteGroupUseCase(groupRepository);
        this.updateGroup = new UpdateGroupUseCase(groupRepository);
        this.getGroup = new GetGroupUseCase(groupRepository);
        this.listGroups = new ListGroupsUseCase(groupRepository);
        this.checkPermission = new CheckGroupPermissionUseCase(groupRepository);
    }

    public Group create(String name, String prefix) {
        return createGroup.execute(name, prefix);
    }

    public Group create(String name, String prefix, int priority, Set<String> permissions) {
        return createGroup.execute(name, prefix, priority, permissions);
    }

    public void delete(String name) {
        deleteGroup.execute(name);
    }

    public Group update(String name, String prefix) {
        return updateGroup.execute(name, prefix);
    }

    public Group update(String name, String prefix, int priority, Set<String> permissions) {
        return updateGroup.execute(name, prefix, priority, permissions);
    }

    public Group setPriority(String name, int priority) {
        return updateGroup.setPriority(name, priority);
    }

    public Group addPermission(String name, String permission) {
        return updateGroup.addPermission(name, permission);
    }

    public Group removePermission(String name, String permission) {
        return updateGroup.removePermission(name, permission);
    }

    public Optional<Group> get(String name) {
        return getGroup.execute(name);
    }

    public List<Group> list() {
        return listGroups.execute();
    }

    public boolean canModify(String actorGroupName, String targetGroupName) {
        return checkPermission.canModify(actorGroupName, targetGroupName);
    }

    public boolean hasPermission(String groupName, String permission) {
        return checkPermission.hasPermission(groupName, permission);
    }
}
