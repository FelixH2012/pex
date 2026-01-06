package de.felix.permissions.application.port;

import de.felix.permissions.data.PermissionPlayer;
import java.util.Optional;
import java.util.UUID;

public interface PlayerRepositoryPort {

    void setGroup(UUID uuid, String groupName, long expiry);

    Optional<PermissionPlayer> getPlayer(UUID uuid);
}
