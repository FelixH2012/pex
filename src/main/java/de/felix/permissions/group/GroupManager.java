package de.felix.permissions.group;

import de.felix.permissions.application.usecase.group.GroupUseCases;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GroupManager {

    private final GroupUseCases groupUseCases;
    private final Map<String, Group> groups = new HashMap<>();

    public GroupManager(GroupUseCases groupUseCases) {
        this.groupUseCases = groupUseCases;
        loadGroups();
    }

    public Group createGroup(String name, String prefix) {
        return createGroup(name, prefix, 0, Set.of());
    }

    public Group createGroup(String name, String prefix, int priority, Set<String> permissions) {
        if (groups.containsKey(name)) {
            return groups.get(name);
        }
        Group group = groupUseCases.create(name, prefix, priority, permissions);
        groups.put(name, group);
        return group;
    }

    public void deleteGroup(String name) {
        if (!groups.containsKey(name)) {
            return;
        }
        groupUseCases.delete(name);
        groups.remove(name);
    }

    public Group updateGroup(String name, String prefix) {
        if (!groups.containsKey(name)) {
            return null;
        }
        Group group = groupUseCases.update(name, prefix);
        groups.put(name, group);
        return group;
    }

    public Group setPriority(String name, int priority) {
        if (!groups.containsKey(name)) {
            return null;
        }
        Group group = groupUseCases.setPriority(name, priority);
        if (group != null) {
            groups.put(name, group);
        }
        return group;
    }

    public Group addPermission(String name, String permission) {
        if (!groups.containsKey(name)) {
            return null;
        }
        Group group = groupUseCases.addPermission(name, permission);
        if (group != null) {
            groups.put(name, group);
        }
        return group;
    }

    public Group removePermission(String name, String permission) {
        if (!groups.containsKey(name)) {
            return null;
        }
        Group group = groupUseCases.removePermission(name, permission);
        if (group != null) {
            groups.put(name, group);
        }
        return group;
    }

    public Optional<Group> getGroup(String name) {
        return Optional.ofNullable(groups.get(name));
    }

    public List<Group> getAllGroups() {
        return List.copyOf(groups.values());
    }

    public boolean canModify(String actorGroupName, String targetGroupName) {
        return groupUseCases.canModify(actorGroupName, targetGroupName);
    }

    public boolean hasPermission(String groupName, String permission) {
        return groupUseCases.hasPermission(groupName, permission);
    }

    public void loadGroups() {
        groups.clear();
        List<Group> storedGroups = groupUseCases.list();
        if (storedGroups == null) {
            return;
        }
        for (Group group : storedGroups) {
            groups.put(group.name(), group);
        }
    }
}
