package de.felix.permissions.database;

import de.felix.permissions.application.port.PlayerRepositoryPort;
import de.felix.permissions.data.PermissionPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class PlayerRepository implements PlayerRepositoryPort {

    private final DatabaseManager databaseManager;

    public PlayerRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public void setGroup(UUID uuid, String groupName, long expiry) {
        try (
                Connection connection = databaseManager.getConnection()) {
            String updateSql = "UPDATE permissions_players SET group_name = ?, expiry = ? WHERE uuid = ?";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                updateStmt.setString(1, groupName);
                updateStmt.setLong(2, expiry);
                updateStmt.setString(3, uuid.toString());
                int rows = updateStmt.executeUpdate();

                if (rows == 0) {
                    // Update failed, so insert
                    String insertSql = "INSERT INTO permissions_players (uuid, group_name, expiry) VALUES (?, ?, ?)";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                        insertStmt.setString(1, uuid.toString());
                        insertStmt.setString(2, groupName);
                        insertStmt.setLong(3, expiry);
                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<PermissionPlayer> getPlayer(UUID uuid) {
        String sql = "SELECT uuid, group_name, expiry FROM permissions_players WHERE uuid = ?";
        try (Connection connection = databaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new PermissionPlayer(
                            UUID.fromString(resultSet.getString("uuid")),
                            resultSet.getString("group_name"),
                            resultSet.getLong("expiry")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
