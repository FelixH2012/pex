package de.felix.permissions.manager;

import de.felix.permissions.application.usecase.player.PlayerUseCases;
import de.felix.permissions.data.PermissionPlayer;
import de.felix.permissions.group.Group;
import de.felix.permissions.group.GroupManager;
import java.util.Optional;
import java.util.UUID;

public class PlayerManager {

    private final PlayerUseCases playerUseCases;
    private final GroupManager groupManager;

    public PlayerManager(PlayerUseCases playerUseCases, GroupManager groupManager) {
        this.playerUseCases = playerUseCases;
        this.groupManager = groupManager;
    }

    public void setPlayerGroup(UUID uuid, String groupName, long expiry) {
        playerUseCases.setGroup(uuid, groupName, expiry);
    }

    public Optional<Group> getPlayerGroup(UUID uuid) {
        Optional<PermissionPlayer> playerOpt = playerUseCases.find(uuid);
        if (playerOpt.isPresent()) {
            PermissionPlayer player = playerOpt.get();
            if (player.isExpired()) {
                // Expired, maybe remove it?
                // For now, treat as no group or default.
                // Optionally remove from DB to clean up.
                // Let's just return empty (which usually means default group) i dont really
                // know.
                return Optional.empty();
            }
            return groupManager.getGroup(player.groupName());
        }
        return Optional.empty();
    }

    public String getPlayerGroupName(UUID uuid) {
        Optional<PermissionPlayer> playerOpt = playerUseCases.find(uuid);
        if (playerOpt.isPresent()) {
            PermissionPlayer player = playerOpt.get();
            if (!player.isExpired()) {
                return player.groupName();
            }
        }
        return null;
    }

    public Optional<PermissionPlayer> getPlayerData(UUID uuid) {
        Optional<PermissionPlayer> playerOpt = playerUseCases.find(uuid);
        if (playerOpt.isPresent() && !playerOpt.get().isExpired()) {
            return playerOpt;
        }
        return Optional.empty();
    }
}
