package de.felix.permissions.application.usecase.player;

import de.felix.permissions.application.port.PlayerRepositoryPort;
import de.felix.permissions.data.PermissionPlayer;
import java.util.Optional;
import java.util.UUID;

public class GetPlayerUseCase {

    private final PlayerRepositoryPort playerRepository;

    public GetPlayerUseCase(PlayerRepositoryPort playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Optional<PermissionPlayer> execute(UUID uuid) {
        return playerRepository.getPlayer(uuid);
    }
}
