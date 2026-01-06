package de.felix.permissions.application.usecase.group;

import de.felix.permissions.application.port.GroupRepositoryPort;

public class DeleteGroupUseCase {

    private final GroupRepositoryPort groupRepository;

    public DeleteGroupUseCase(GroupRepositoryPort groupRepository) {
        this.groupRepository = groupRepository;
    }

    public void execute(String name) {
        groupRepository.deleteGroup(name);
    }
}
