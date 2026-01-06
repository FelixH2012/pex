package de.felix.permissions.listener;

import de.felix.permissions.Permissions;
import de.felix.permissions.group.Group;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Optional;

public class PlayerListener implements Listener {

    private final Permissions plugin;

    public PlayerListener(Permissions plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        Optional<Group> groupOpt = plugin.getPlayerManager().getPlayerGroup(player.getUniqueId());

        Component chatName = groupOpt.isPresent()
                ? buildDisplayName(player, groupOpt.get())
                : Component.text(player.getName()).color(NamedTextColor.WHITE);

        final Component name = chatName;
        event.renderer((source, displayName, message, viewer) -> name
                .append(Component.text(": ").color(NamedTextColor.WHITE)).append(message));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Optional<Group> groupOpt = plugin.getPlayerManager().getPlayerGroup(player.getUniqueId());

        if (groupOpt.isEmpty()) {
            String defaultGroup = plugin.getConfig().getString("default-group", "user");
            Optional<Group> defaultOpt = plugin.getGroupManager().getGroup(defaultGroup);

            if (defaultOpt.isPresent()) {
                plugin.getPlayerManager().setPlayerGroup(player.getUniqueId(), defaultGroup, -1);
                groupOpt = defaultOpt;
            } else {
                plugin.getGroupManager().createGroup(defaultGroup, "&7User", 0, java.util.Set.of());
                plugin.getPlayerManager().setPlayerGroup(player.getUniqueId(), defaultGroup, -1);
                groupOpt = plugin.getGroupManager().getGroup(defaultGroup);
            }
        }

        if (groupOpt.isPresent()) {
            Group group = groupOpt.get();
            Component displayName = buildDisplayName(player, group);

            player.displayName(displayName);
            player.playerListName(displayName);

            String joinMsg = plugin.getMessages().get("player-join", "player", player.getName(), "prefix",
                    getPrefix(group));
            event.joinMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(joinMsg));
        }
    }

    private Component buildDisplayName(Player player, Group group) {
        String prefix = getPrefix(group);
        Component prefixComp = LegacyComponentSerializer.legacyAmpersand().deserialize(prefix);

        return Component.text("[").color(NamedTextColor.GRAY)
                .append(prefixComp)
                .append(Component.text("] ").color(NamedTextColor.GRAY))
                .append(Component.text(player.getName()).color(NamedTextColor.WHITE));
    }

    private String getPrefix(Group group) {
        String prefix = group.prefix();
        return (prefix == null || prefix.isBlank()) ? "&7" + group.name() : prefix;
    }
}
