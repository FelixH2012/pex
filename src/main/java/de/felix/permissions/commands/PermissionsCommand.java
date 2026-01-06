package de.felix.permissions.commands;

import de.felix.permissions.Permissions;
import de.felix.permissions.group.Group;
import de.felix.permissions.message.Messages;
import de.felix.permissions.util.TimeParser;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.*;

public class PermissionsCommand implements BasicCommand {

    private final Permissions plugin;
    private final Messages msg;

    public PermissionsCommand(Permissions plugin) {
        this.plugin = plugin;
        this.msg = plugin.getMessages();
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();

        if (args.length < 1) {
            sendHelp(sender);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "user" -> handleUserCommand(sender, args);
            case "group" -> handleGroupCommand(sender, args);
            case "info" -> handleInfoCommand(sender, args);
            default -> sendHelp(sender);
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(msg.getRaw("help-header"));
        sender.sendMessage(msg.getRaw("help-user-set"));
        sender.sendMessage(msg.getRaw("help-group-create"));
        sender.sendMessage(msg.getRaw("help-group-delete"));
        sender.sendMessage(msg.getRaw("help-group-setpriority"));
        sender.sendMessage(msg.getRaw("help-group-addperm"));
        sender.sendMessage(msg.getRaw("help-group-removeperm"));
        sender.sendMessage(msg.getRaw("help-group-info"));
        sender.sendMessage(msg.getRaw("help-group-list"));
        sender.sendMessage(msg.getRaw("help-info"));
    }

    private void handleUserCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(msg.getRaw("user-usage"));
            return;
        }

        String playerName = args[1];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

        if (args.length >= 5 && args[2].equalsIgnoreCase("group") && args[3].equalsIgnoreCase("set")) {
            handleGroupSet(sender, target, args);
        }
    }

    private void handleGroupSet(CommandSender sender, OfflinePlayer target, String[] args) {
        if (!checkPermission(sender, "pex.user.setgroup"))
            return;

        String groupName = args[4];
        Optional<Group> groupOpt = plugin.getGroupManager().getGroup(groupName);

        if (groupOpt.isEmpty()) {
            sender.sendMessage(msg.get("group-not-found", "group", groupName));
            return;
        }

        long expiry = -1;
        if (args.length > 5) {
            String durationStr = String.join(" ", Arrays.copyOfRange(args, 5, args.length));
            try {
                long durationMillis = TimeParser.parseDuration(durationStr);
                expiry = System.currentTimeMillis() + durationMillis;
            } catch (IllegalArgumentException e) {
                sender.sendMessage(msg.get("invalid-duration", "error", e.getMessage()));
                return;
            }
        }

        plugin.getPlayerManager().setPlayerGroup(target.getUniqueId(), groupName, expiry);
        if (expiry == -1) {
            sender.sendMessage(msg.get("user-group-set", "player", target.getName(), "group", groupName));
        } else {
            sender.sendMessage(msg.get("user-group-set-temp",
                    "player", target.getName(),
                    "group", groupName,
                    "expiry", Date.from(Instant.ofEpochMilli(expiry)).toString()));
        }
    }

    private void handleGroupCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(msg.getRaw("group-usage"));
            return;
        }

        String subCommand = args[1].toLowerCase();
        switch (subCommand) {
            case "create" -> handleGroupCreate(sender, args);
            case "delete" -> handleGroupDelete(sender, args);
            case "setpriority" -> handleGroupSetPriority(sender, args);
            case "addperm" -> handleGroupAddPermission(sender, args);
            case "removeperm" -> handleGroupRemovePermission(sender, args);
            case "info" -> handleGroupInfo(sender, args);
            case "list" -> handleGroupList(sender);
            default -> sender.sendMessage(msg.get("group-unknown-subcommand", "subcommand", subCommand));
        }
    }

    private void handleGroupCreate(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(msg.getRaw("help-group-create"));
            return;
        }

        if (!checkPermission(sender, "pex.group.create"))
            return;

        String name = args[2];
        String prefix = args[3];
        int priority = 0;

        if (args.length >= 5) {
            try {
                priority = Integer.parseInt(args[4]);
            } catch (NumberFormatException e) {
                sender.sendMessage(msg.get("invalid-number", "value", args[4]));
                return;
            }
        }

        if (plugin.getGroupManager().getGroup(name).isPresent()) {
            sender.sendMessage(msg.get("group-already-exists", "group", name));
            return;
        }

        Group group = plugin.getGroupManager().createGroup(name, prefix, priority, Set.of());
        sender.sendMessage(msg.get("group-created",
                "group", group.name(),
                "prefix", group.prefix(),
                "priority", String.valueOf(group.priority())));
    }

    private void handleGroupDelete(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(msg.getRaw("help-group-delete"));
            return;
        }

        if (!checkPermission(sender, "pex.group.delete"))
            return;

        String name = args[2];
        Optional<Group> targetOpt = plugin.getGroupManager().getGroup(name);

        if (targetOpt.isEmpty()) {
            sender.sendMessage(msg.get("group-not-found", "group", name));
            return;
        }

        if (!checkCanModify(sender, targetOpt.get()))
            return;

        plugin.getGroupManager().deleteGroup(name);
        sender.sendMessage(msg.get("group-deleted", "group", name));
    }

    private void handleGroupSetPriority(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(msg.getRaw("help-group-setpriority"));
            return;
        }

        if (!checkPermission(sender, "pex.group.setpriority"))
            return;

        String name = args[2];
        int priority;
        try {
            priority = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(msg.get("invalid-number", "value", args[3]));
            return;
        }

        Optional<Group> targetOpt = plugin.getGroupManager().getGroup(name);
        if (targetOpt.isEmpty()) {
            sender.sendMessage(msg.get("group-not-found", "group", name));
            return;
        }

        if (!checkCanModify(sender, targetOpt.get()))
            return;

        Group group = plugin.getGroupManager().setPriority(name, priority);
        if (group != null) {
            sender.sendMessage(msg.get("group-priority-set", "group", name, "priority", String.valueOf(priority)));
        }
    }

    private void handleGroupAddPermission(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(msg.getRaw("help-group-addperm"));
            return;
        }

        if (!checkPermission(sender, "pex.group.addperm"))
            return;

        String name = args[2];
        String permission = args[3];

        Optional<Group> targetOpt = plugin.getGroupManager().getGroup(name);
        if (targetOpt.isEmpty()) {
            sender.sendMessage(msg.get("group-not-found", "group", name));
            return;
        }

        if (!checkCanModify(sender, targetOpt.get()))
            return;

        Group group = plugin.getGroupManager().addPermission(name, permission);
        if (group != null) {
            sender.sendMessage(msg.get("group-permission-added", "permission", permission, "group", name));
        }
    }

    private void handleGroupRemovePermission(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(msg.getRaw("help-group-removeperm"));
            return;
        }

        if (!checkPermission(sender, "pex.group.removeperm"))
            return;

        String name = args[2];
        String permission = args[3];

        Optional<Group> targetOpt = plugin.getGroupManager().getGroup(name);
        if (targetOpt.isEmpty()) {
            sender.sendMessage(msg.get("group-not-found", "group", name));
            return;
        }

        if (!checkCanModify(sender, targetOpt.get()))
            return;

        Group group = plugin.getGroupManager().removePermission(name, permission);
        if (group != null) {
            sender.sendMessage(msg.get("group-permission-removed", "permission", permission, "group", name));
        }
    }

    private void handleGroupInfo(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(msg.getRaw("help-group-info"));
            return;
        }

        // We dont want normal players to use this command
        if (!checkPermission(sender, "pex.group.info"))
            return;

        String name = args[2];
        Optional<Group> groupOpt = plugin.getGroupManager().getGroup(name);

        if (groupOpt.isEmpty()) {
            sender.sendMessage(msg.get("group-not-found", "group", name));
            return;
        }

        Group group = groupOpt.get();
        String permissions = group.permissions().isEmpty()
                ? msg.getRaw("group-info-no-permissions")
                : String.join(", ", group.permissions());

        sender.sendMessage(msg.get("group-info-header", "group", group.name()));
        sender.sendMessage(msg.get("group-info-prefix", "prefix", group.prefix()));
        sender.sendMessage(msg.get("group-info-priority", "priority", String.valueOf(group.priority())));
        sender.sendMessage(msg.get("group-info-permissions", "permissions", permissions));
    }

    private void handleGroupList(CommandSender sender) {
        if (!checkPermission(sender, "pex.group.list"))
            return;

        List<Group> groups = plugin.getGroupManager().getAllGroups();

        if (groups.isEmpty()) {
            sender.sendMessage(msg.getRaw("group-list-empty"));
            return;
        }

        sender.sendMessage(msg.getRaw("group-list-header"));
        groups.stream()
                .sorted(Comparator.comparingInt(Group::priority).reversed())
                .forEach(g -> sender.sendMessage(msg.get("group-list-entry",
                        "group", g.name(),
                        "prefix", g.prefix(),
                        "priority", String.valueOf(g.priority()))));
    }

    private void handleInfoCommand(CommandSender sender, String[] args) {
        OfflinePlayer target;

        if (args.length >= 2) {
            // /pex info <player>
            target = Bukkit.getOfflinePlayer(args[1]);
        } else if (sender instanceof Player player) {
            // /pex info (self)
            target = player;
        } else {
            sender.sendMessage(msg.getRaw("user-usage"));
            return;
        }

        var playerData = plugin.getPlayerManager().getPlayerData(target.getUniqueId());

        if (playerData.isEmpty()) {
            sender.sendMessage(msg.get("player-info-no-group"));
            return;
        }

        var data = playerData.get();

        sender.sendMessage(
                msg.get("player-info-header", "player", target.getName() != null ? target.getName() : "Unknown"));
        sender.sendMessage(msg.get("player-info-group", "group", data.groupName()));

        if (data.expiry() == -1) {
            sender.sendMessage(msg.getRaw("player-info-permanent"));
        } else {
            long remaining = data.expiry() - System.currentTimeMillis();
            if (remaining > 0) {
                String timeStr = formatDuration(remaining);
                sender.sendMessage(msg.get("player-info-expires", "expiry", timeStr));
            } else {
                sender.sendMessage(msg.get("player-info-expires", "expiry", "Expired"));
            }
        }
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long days = seconds / 86400;
        seconds %= 86400;
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0)
            sb.append(days).append("d ");
        if (hours > 0)
            sb.append(hours).append("h ");
        if (minutes > 0)
            sb.append(minutes).append("m ");
        if (seconds > 0 || sb.length() == 0)
            sb.append(seconds).append("s");
        return sb.toString().trim();
    }

    private boolean checkPermission(CommandSender sender, String permission) {
        if (sender.isOp())
            return true;

        if (sender instanceof Player player) {
            String playerGroup = plugin.getPlayerManager().getPlayerGroupName(player.getUniqueId());
            if (playerGroup != null && plugin.getGroupManager().hasPermission(playerGroup, permission)) {
                return true;
            }
        }

        sender.sendMessage(msg.get("no-permission", "permission", permission));
        return false;
    }

    private boolean checkCanModify(CommandSender sender, Group target) {
        if (sender.isOp())
            return true;

        if (sender instanceof Player player) {
            String playerGroup = plugin.getPlayerManager().getPlayerGroupName(player.getUniqueId());
            if (playerGroup != null && plugin.getGroupManager().canModify(playerGroup, target.name())) {
                return true;
            }
        }

        sender.sendMessage(msg.get("no-permission-modify", "group", target.name()));
        return false;
    }
}
