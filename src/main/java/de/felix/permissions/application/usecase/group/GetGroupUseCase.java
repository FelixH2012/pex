package de.felix.permissions.application.usecase.group;

import de.felix.permissions.application.port.GroupRepositoryPort;
import de.felix.permissions.group.Group;
import java.util.Optional;

public class GetGroupUseCase {

    private final GroupRepositoryPort groupRepository;

    public GetGroupUseCase(GroupRepositoryPort groupRepository) {
        this.groupRepository = groupRepository;
    }

    public Optional<Group> execute(String name) {
        return groupRepository.getGroup(name);
    }
}
