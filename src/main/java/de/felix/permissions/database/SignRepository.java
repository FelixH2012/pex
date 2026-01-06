package de.felix.permissions.database;

import de.felix.permissions.data.PermissionSign;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;

public class SignRepository {

    private final DatabaseManager db;

    public SignRepository(DatabaseManager db) {
        this.db = db;
    }

    public void createSign(PermissionSign sign) {
        String sql = "INSERT INTO permissions_signs (id, world, x, y, z, player_uuid) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE player_uuid = VALUES(player_uuid)";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sign.id());
            stmt.setString(2, sign.world());
            stmt.setInt(3, sign.x());
            stmt.setInt(4, sign.y());
            stmt.setInt(5, sign.z());
            stmt.setString(6, sign.playerUuid() != null ? sign.playerUuid().toString() : null);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteSign(String id) {
        String sql = "DELETE FROM permissions_signs WHERE id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Optional<PermissionSign> getSign(String id) {
        String sql = "SELECT id, world, x, y, z, player_uuid FROM permissions_signs WHERE id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapSign(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private PermissionSign mapSign(ResultSet rs) throws SQLException {
        String uuid = rs.getString("player_uuid");
        return new PermissionSign(rs.getString("id"), rs.getString("world"), rs.getInt("x"), rs.getInt("y"),
                rs.getInt("z"),
                uuid != null && !uuid.isBlank() ? UUID.fromString(uuid) : null);
    }
}
