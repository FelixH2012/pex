package de.felix.permissions.group;

import java.util.Set;

public record Group(String name, String prefix, int priority, Set<String> permissions) {

    public Group(String name, String prefix) {
        this(name, prefix, 0, Set.of());
    }

    public boolean hasPermission(String permission) {
        if (permissions.contains("*") || permissions.contains(permission))
            return true;

        for (String perm : permissions) {
            if (perm.endsWith(".*") && permission.startsWith(perm.substring(0, perm.length() - 1))) {
                return true;
            }
        }
        return false;
    }

    public boolean canModify(Group other) {
        return this.priority > other.priority;
    }
}
