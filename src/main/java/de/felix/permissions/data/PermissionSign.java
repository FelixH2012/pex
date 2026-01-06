package de.felix.permissions.data;

import org.bukkit.Location;

import java.util.UUID;

public record PermissionSign(String id, String world, int x, int y, int z, UUID playerUuid) {

    public static PermissionSign fromLocation(Location loc, UUID playerUuid) {
        String id = loc.getWorld().getName() + "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
        return new PermissionSign(id, loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(),
                playerUuid);
    }

    public static String locationToId(Location loc) {
        return loc.getWorld().getName() + "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
    }
}
