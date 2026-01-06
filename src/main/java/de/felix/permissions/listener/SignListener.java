package de.felix.permissions.listener;

import de.felix.permissions.Permissions;
import de.felix.permissions.data.PermissionSign;
import de.felix.permissions.database.SignRepository;
import de.felix.permissions.group.Group;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;
import java.util.UUID;

public class SignListener implements Listener {

    private final Permissions plugin;
    private final SignRepository signRepository;

    public SignListener(Permissions plugin, SignRepository signRepository) {
        this.plugin = plugin;
        this.signRepository = signRepository;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        String firstLine = event.line(0) != null ? event.line(0).toString() : "";
        if (!firstLine.toLowerCase().contains("rank"))
            return;

        Player player = event.getPlayer();
        if (!player.isOp() && !hasPermission(player, "pex.sign.create"))
            return;

        PermissionSign sign = PermissionSign.fromLocation(event.getBlock().getLocation(), null);
        signRepository.createSign(sign);

        event.line(0, Component.text("§6[Rank]"));
        event.line(1, Component.text("§7Right-click"));
        event.line(2, Component.text("§7to set player"));
        event.line(3, Component.text(""));

        player.sendMessage(plugin.getMessages().get("sign-created", "player", "none"));
    }

    @EventHandler
    public void onSignInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null)
            return;

        Block block = event.getClickedBlock();
        if (!isSign(block.getType()))
            return;

        String signId = PermissionSign.locationToId(block.getLocation());
        Optional<PermissionSign> signOpt = signRepository.getSign(signId);
        if (signOpt.isEmpty())
            return;

        Player player = event.getPlayer();
        PermissionSign permSign = signOpt.get();

        if (event.getAction().name().contains("RIGHT") && (player.isOp() || hasPermission(player, "pex.sign.set"))) {
            PermissionSign updated = new PermissionSign(
                    permSign.id(), permSign.world(), permSign.x(), permSign.y(), permSign.z(), player.getUniqueId());
            signRepository.createSign(updated);
            updateSignDisplay(block, player.getUniqueId());
            player.sendMessage(plugin.getMessages().get("sign-created", "player", player.getName()));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSignBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!isSign(block.getType()))
            return;

        String signId = PermissionSign.locationToId(block.getLocation());
        Optional<PermissionSign> signOpt = signRepository.getSign(signId);

        if (signOpt.isPresent()) {
            Player player = event.getPlayer();
            if (!player.isOp() && !hasPermission(player, "pex.sign.remove")) {
                event.setCancelled(true);
                return;
            }
            signRepository.deleteSign(signId);
            player.sendMessage(plugin.getMessages().getRaw("sign-removed"));
        }
    }

    private void updateSignDisplay(Block block, UUID playerUuid) {
        if (!(block.getState() instanceof Sign sign))
            return;

        OfflinePlayer target = Bukkit.getOfflinePlayer(playerUuid);
        Optional<Group> groupOpt = plugin.getPlayerManager().getPlayerGroup(playerUuid);

        sign.getSide(Side.FRONT).line(0, Component.text("§6[Rank]"));
        sign.getSide(Side.FRONT).line(1,
                Component.text("§f" + (target.getName() != null ? target.getName() : "Unknown")));

        if (groupOpt.isPresent()) {
            Group group = groupOpt.get();
            sign.getSide(Side.FRONT).line(2, Component.text(group.prefix().replace('&', '§')));
            sign.getSide(Side.FRONT).line(3, Component.text("§7" + group.name()));
        } else {
            sign.getSide(Side.FRONT).line(2, Component.text("§7No group"));
            sign.getSide(Side.FRONT).line(3, Component.text(""));
        }
        sign.update();
    }

    private boolean isSign(Material material) {
        return material.name().contains("SIGN");
    }

    private boolean hasPermission(Player player, String permission) {
        String groupName = plugin.getPlayerManager().getPlayerGroupName(player.getUniqueId());
        return groupName != null && plugin.getGroupManager().hasPermission(groupName, permission);
    }
}
