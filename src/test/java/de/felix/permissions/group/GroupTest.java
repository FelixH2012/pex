package de.felix.permissions.group;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GroupTest {

    @Test
    void hasPermission_ExactMatch() {
        Group group = new Group("Admin", "[Admin]", 50, Set.of("pex.group.create", "pex.user.set"));

        assertTrue(group.hasPermission("pex.group.create"));
        assertTrue(group.hasPermission("pex.user.set"));
        assertFalse(group.hasPermission("pex.group.delete"));
    }

    @Test
    void hasPermission_WildcardMatch() {
        Group group = new Group("Owner", "[Owner]", 100, Set.of("pex.group.*", "pex.user.*"));

        assertTrue(group.hasPermission("pex.group.create"));
        assertTrue(group.hasPermission("pex.group.delete"));
        assertTrue(group.hasPermission("pex.group.setpriority"));
        assertTrue(group.hasPermission("pex.user.set"));
        assertFalse(group.hasPermission("pex.other.something"));
    }

    @Test
    void hasPermission_SuperWildcard() {
        Group group = new Group("SuperAdmin", "[SA]", 200, Set.of("*"));

        assertTrue(group.hasPermission("pex.group.create"));
        assertTrue(group.hasPermission("anything.at.all"));
    }

    @Test
    void hasPermission_EmptyPermissions() {
        Group group = new Group("Default", "[D]", 0, Set.of());

        assertFalse(group.hasPermission("pex.group.create"));
        assertFalse(group.hasPermission("anything"));
    }

    @Test
    void canModify_HigherPriorityCanModifyLower() {
        Group owner = new Group("Owner", "[Owner]", 100, Set.of());
        Group admin = new Group("Admin", "[Admin]", 50, Set.of());
        Group member = new Group("Member", "[Member]", 10, Set.of());

        assertTrue(owner.canModify(admin));
        assertTrue(owner.canModify(member));
        assertTrue(admin.canModify(member));
    }

    @Test
    void canModify_LowerPriorityCannotModifyHigher() {
        Group owner = new Group("Owner", "[Owner]", 100, Set.of());
        Group admin = new Group("Admin", "[Admin]", 50, Set.of());
        Group member = new Group("Member", "[Member]", 10, Set.of());

        assertFalse(admin.canModify(owner));
        assertFalse(member.canModify(owner));
        assertFalse(member.canModify(admin));
    }

    @Test
    void canModify_EqualPriorityCannotModify() {
        Group admin1 = new Group("Admin1", "[A1]", 50, Set.of());
        Group admin2 = new Group("Admin2", "[A2]", 50, Set.of());

        assertFalse(admin1.canModify(admin2));
        assertFalse(admin2.canModify(admin1));
    }

    @Test
    void canModify_CannotModifySelf() {
        Group admin = new Group("Admin", "[Admin]", 50, Set.of());

        assertFalse(admin.canModify(admin));
    }

    @Test
    void convenienceConstructor_SetsDefaults() {
        Group group = new Group("Default", "[D]");

        assertEquals("Default", group.name());
        assertEquals("[D]", group.prefix());
        assertEquals(0, group.priority());
        assertTrue(group.permissions().isEmpty());
    }
}
