package de.felix.permissions.data;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PermissionPlayerTest {

    @Test
    void isExpired_ReturnsFalseForPermanent() {
        PermissionPlayer player = new PermissionPlayer(UUID.randomUUID(), "Admin", -1);

        assertFalse(player.isExpired());
    }

    @Test
    void isExpired_ReturnsFalseForFutureExpiry() {
        long futureTime = System.currentTimeMillis() + 100000;
        PermissionPlayer player = new PermissionPlayer(UUID.randomUUID(), "Admin", futureTime);

        assertFalse(player.isExpired());
    }

    @Test
    void isExpired_ReturnsTrueForPastExpiry() {
        long pastTime = System.currentTimeMillis() - 1000;
        PermissionPlayer player = new PermissionPlayer(UUID.randomUUID(), "Admin", pastTime);

        assertTrue(player.isExpired());
    }

    @Test
    void recordProperties_AreCorrect() {
        UUID uuid = UUID.randomUUID();
        PermissionPlayer player = new PermissionPlayer(uuid, "Member", 12345L);

        assertEquals(uuid, player.uuid());
        assertEquals("Member", player.groupName());
        assertEquals(12345L, player.expiry());
    }
}
