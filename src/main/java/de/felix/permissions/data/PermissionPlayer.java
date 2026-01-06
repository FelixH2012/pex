package de.felix.permissions.data;

import java.util.UUID;

public record PermissionPlayer(UUID uuid, String groupName, long expiry) {

    public boolean isExpired() {
        return expiry != -1 && System.currentTimeMillis() > expiry;
    }
}
