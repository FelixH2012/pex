package de.felix.permissions.application.usecase.player;

import de.felix.permissions.application.port.PlayerRepositoryPort;
import de.felix.permissions.data.PermissionPlayer;
import java.util.Optional;
import java.util.UUID;

/**
 * Simple orchestrator exposing all player-related use cases in one place.
 */
public class PlayerUseCases {

    private final SetPlayerGroupUseCase setPlayerGroup;
    private final GetPlayerUseCase getPlayer;

    public PlayerUseCases(PlayerRepositoryPort playerRepository) {
        this.setPlayerGroup = new SetPlayerGroupUseCase(playerRepository);
        this.getPlayer = new GetPlayerUseCase(playerRepository);
    }

    public void setGroup(UUID uuid, String groupName, long expiry) {
        setPlayerGroup.execute(uuid, groupName, expiry);
    }

    public Optional<PermissionPlayer> find(UUID uuid) {
        return getPlayer.execute(uuid);
    }
}
