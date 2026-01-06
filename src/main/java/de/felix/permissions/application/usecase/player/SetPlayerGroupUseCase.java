package de.felix.permissions.application.usecase.player;

import de.felix.permissions.application.port.PlayerRepositoryPort;
import java.util.UUID;

public class SetPlayerGroupUseCase {

    private final PlayerRepositoryPort playerRepository;

    public SetPlayerGroupUseCase(PlayerRepositoryPort playerRepository) {
        this.playerRepository = playerRepository;
    }

    public void execute(UUID uuid, String groupName, long expiry) {
        playerRepository.setGroup(uuid, groupName, expiry);
    }
}
