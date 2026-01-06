package de.felix.permissions.database;

import de.felix.permissions.application.port.GroupRepositoryPort;
import de.felix.permissions.group.Group;

import java.sql.*;
import java.util.*;

public class GroupRepository implements GroupRepositoryPort {

    private final DatabaseManager db;

    public GroupRepository(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public void createGroup(Group group) {
        String sql = "INSERT INTO permissions_groups (name, prefix, priority, permissions) VALUES (?, ?, ?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, group.name());
            stmt.setString(2, group.prefix());
            stmt.setInt(3, group.priority());
            stmt.setString(4, serializePerms(group.permissions()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteGroup(String name) {
        String sql = "DELETE FROM permissions_groups WHERE name = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateGroup(Group group) {
        String sql = "UPDATE permissions_groups SET prefix = ?, priority = ?, permissions = ? WHERE name = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, group.prefix());
            stmt.setInt(2, group.priority());
            stmt.setString(3, serializePerms(group.permissions()));
            stmt.setString(4, group.name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<Group> getGroup(String name) {
        String sql = "SELECT name, prefix, priority, permissions FROM permissions_groups WHERE name = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapGroup(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Group> getAllGroups() {
        List<Group> groups = new ArrayList<>();
        String sql = "SELECT name, prefix, priority, permissions FROM permissions_groups";
        try (Connection conn = db.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next())
                groups.add(mapGroup(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groups;
    }

    private Group mapGroup(ResultSet rs) throws SQLException {
        return new Group(rs.getString("name"), rs.getString("prefix"), rs.getInt("priority"),
                deserializePerms(rs.getString("permissions")));
    }

    private String serializePerms(Set<String> perms) {
        return (perms == null || perms.isEmpty()) ? "" : String.join(",", perms);
    }

    private Set<String> deserializePerms(String perms) {
        return (perms == null || perms.isBlank()) ? new HashSet<>() : new HashSet<>(Arrays.asList(perms.split(",")));
    }
}
