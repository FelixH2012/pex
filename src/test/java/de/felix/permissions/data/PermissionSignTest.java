package de.felix.permissions.data;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PermissionSignTest {

    @Test
    void locationToId_GeneratesCorrectFormat() {
        World mockWorld = Mockito.mock(World.class);
        Mockito.when(mockWorld.getName()).thenReturn("world");

        Location location = new Location(mockWorld, 10, 64, -20);
        String id = PermissionSign.locationToId(location);

        assertEquals("world_10_64_-20", id);
    }

    @Test
    void fromLocation_CreatesCorrectSign() {
        World mockWorld = Mockito.mock(World.class);
        Mockito.when(mockWorld.getName()).thenReturn("nether");

        Location location = new Location(mockWorld, 5, 100, 15);
        UUID playerUuid = UUID.randomUUID();

        PermissionSign sign = PermissionSign.fromLocation(location, playerUuid);

        assertEquals("nether_5_100_15", sign.id());
        assertEquals("nether", sign.world());
        assertEquals(5, sign.x());
        assertEquals(100, sign.y());
        assertEquals(15, sign.z());
        assertEquals(playerUuid, sign.playerUuid());
    }

    @Test
    void fromLocation_WithNullPlayer() {
        World mockWorld = Mockito.mock(World.class);
        Mockito.when(mockWorld.getName()).thenReturn("end");

        Location location = new Location(mockWorld, 0, 50, 0);

        PermissionSign sign = PermissionSign.fromLocation(location, null);

        assertNull(sign.playerUuid());
    }
}
