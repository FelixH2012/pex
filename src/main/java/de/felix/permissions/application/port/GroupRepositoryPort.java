package de.felix.permissions.application.port;

import de.felix.permissions.group.Group;
import java.util.List;
import java.util.Optional;

public interface GroupRepositoryPort {

    void createGroup(Group group);

    void deleteGroup(String name);

    void updateGroup(Group group);

    Optional<Group> getGroup(String name);

    List<Group> getAllGroups();
}
