package de.felix.permissions;

import de.felix.permissions.application.port.GroupRepositoryPort;
import de.felix.permissions.application.port.PlayerRepositoryPort;
import de.felix.permissions.application.usecase.group.GroupUseCases;
import de.felix.permissions.application.usecase.player.PlayerUseCases;
import de.felix.permissions.commands.PermissionsCommand;
import de.felix.permissions.database.DatabaseConnectionException;
import de.felix.permissions.database.DatabaseManager;
import de.felix.permissions.database.GroupRepository;
import de.felix.permissions.database.PlayerRepository;
import de.felix.permissions.group.GroupManager;
import de.felix.permissions.listener.PlayerListener;
import de.felix.permissions.manager.PlayerManager;
import de.felix.permissions.message.Messages;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class Permissions extends JavaPlugin {

    private DatabaseManager databaseManager;
    private GroupManager groupManager;
    private PlayerManager playerManager;
    private Messages messages;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        messages = new Messages(getConfig(), this);

        databaseManager = new DatabaseManager(this);
        try {
            databaseManager.connect();
        } catch (DatabaseConnectionException e) {
            getLogger().log(Level.SEVERE, "Failed to connect to database! Disabling plugin.", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        final GroupRepositoryPort groupRepository = new GroupRepository(databaseManager);
        GroupUseCases groupUseCases = new GroupUseCases(groupRepository);
        groupManager = new GroupManager(groupUseCases);

        final PlayerRepositoryPort playerRepository = new PlayerRepository(databaseManager);
        PlayerUseCases playerUseCases = new PlayerUseCases(playerRepository);
        playerManager = new PlayerManager(playerUseCases, groupManager);

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        var signRepository = new de.felix.permissions.database.SignRepository(databaseManager);
        getServer().getPluginManager().registerEvents(
                new de.felix.permissions.listener.SignListener(this, signRepository), this);

        registerCommand("pex", new PermissionsCommand(this));

        getLogger().info("Permissions has been enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        getLogger().info("Permissions has been disabled!");
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public GroupManager getGroupManager() {
        return groupManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public Messages getMessages() {
        return messages;
    }
}
