package de.felix.permissions.application.usecase.group;

import de.felix.permissions.application.port.GroupRepositoryPort;
import de.felix.permissions.group.Group;
import java.util.List;

public class ListGroupsUseCase {

    private final GroupRepositoryPort groupRepository;

    public ListGroupsUseCase(GroupRepositoryPort groupRepository) {
        this.groupRepository = groupRepository;
    }

    public List<Group> execute() {
        return groupRepository.getAllGroups();
    }
}
