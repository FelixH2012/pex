package de.felix.permissions.application.usecase.group;

import de.felix.permissions.application.port.GroupRepositoryPort;
import de.felix.permissions.group.Group;

import java.util.Optional;

/**
 * Use case for checking if a group has permission to perform actions on another
 * group.
 */
public class CheckGroupPermissionUseCase {

    private final GroupRepositoryPort groupRepository;

    public CheckGroupPermissionUseCase(GroupRepositoryPort groupRepository) {
        this.groupRepository = groupRepository;
    }

    /**
     * Check if the actor group can modify the target group.
     */
    public boolean canModify(String actorGroupName, String targetGroupName) {
        Optional<Group> actorOpt = groupRepository.getGroup(actorGroupName);
        Optional<Group> targetOpt = groupRepository.getGroup(targetGroupName);

        if (actorOpt.isEmpty() || targetOpt.isEmpty()) {
            return false;
        }

        return actorOpt.get().canModify(targetOpt.get());
    }

    /**
     * Check if a group has a specific permission node.
     */
    public boolean hasPermission(String groupName, String permission) {
        Optional<Group> groupOpt = groupRepository.getGroup(groupName);
        return groupOpt.map(group -> group.hasPermission(permission)).orElse(false);
    }
}
