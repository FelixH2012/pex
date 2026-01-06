package de.felix.permissions.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.felix.permissions.Permissions;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Database Manager to make my life easier.
 * Handles database connections and provides a simple interface to interact with
 * the database.
 */
public class DatabaseManager {

    private final Permissions plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(final Permissions plugin) {
        this.plugin = plugin;
    }

    /**
     * Connects to the database using the configuration in config.yml.
     *
     * @throws DatabaseConnectionException if the connection could not be
     *                                     established
     */
    public void connect() throws DatabaseConnectionException {
        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }

        // https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
        String host = plugin.getConfig().getString("database.host");
        int port = plugin.getConfig().getInt("database.port");
        String database = plugin.getConfig().getString("database.database");
        String username = plugin.getConfig().getString("database.username");
        String password = plugin.getConfig().getString("database.password");
        boolean useSsl = plugin.getConfig().getBoolean("database.use-ssl");
        String jdbcUrl = plugin.getConfig().getString("database.jdbc-url");

        HikariConfig config = new HikariConfig();
        if (jdbcUrl != null && !jdbcUrl.isEmpty()) {
            config.setJdbcUrl(jdbcUrl);
        } else {
            ensureDatabaseExists(host, port, database, username, password, useSsl);
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database
                    + "?useSSL=" + useSsl);
        }
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);

        try {
            dataSource = new HikariDataSource(config);
            initTables();
            plugin.getLogger().info("Successfully connected to the database!");
        } catch (Exception e) {
            throw new DatabaseConnectionException("Could not connect to the database", e);
        }
    }

    private void initTables() {
        // Note: TEXT columns can't have DEFAULT in MySQL, so we handle null in the
        // repository
        String groupTable = "CREATE TABLE IF NOT EXISTS permissions_groups (" +
                "name VARCHAR(64) PRIMARY KEY, " +
                "prefix VARCHAR(64) NOT NULL, " +
                "priority INT DEFAULT 0, " +
                "permissions TEXT" +
                ");";
        String playerTable = "CREATE TABLE IF NOT EXISTS permissions_players (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "group_name VARCHAR(64) NOT NULL, " +
                "expiry BIGINT" +
                ");";
        String signTable = "CREATE TABLE IF NOT EXISTS permissions_signs (" +
                "id VARCHAR(128) PRIMARY KEY, " +
                "world VARCHAR(64) NOT NULL, " +
                "x INT NOT NULL, " +
                "y INT NOT NULL, " +
                "z INT NOT NULL, " +
                "player_uuid VARCHAR(36)" +
                ");";
        try (Connection connection = getConnection();
                java.sql.Statement statement = connection.createStatement()) {
            statement.execute(groupTable);
            statement.execute(playerTable);
            statement.execute(signTable);
            // Migration: Add columns if they don't exist (for existing databases)
            migrateGroupTable(connection);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not initialize database tables!", e);
        }
    }

    private void migrateGroupTable(Connection connection) {
        // MySQL doesn't support "ADD COLUMN IF NOT EXISTS", so we check manually
        try {
            if (!columnExists(connection, "permissions_groups", "priority")) {
                try (var stmt = connection.createStatement()) {
                    stmt.execute("ALTER TABLE permissions_groups ADD COLUMN priority INT DEFAULT 0");
                    plugin.getLogger().info("Added 'priority' column to permissions_groups");
                }
            }
            if (!columnExists(connection, "permissions_groups", "permissions")) {
                try (var stmt = connection.createStatement()) {
                    stmt.execute("ALTER TABLE permissions_groups ADD COLUMN permissions TEXT");
                    plugin.getLogger().info("Added 'permissions' column to permissions_groups");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Could not migrate permissions_groups table", e);
        }
    }

    private boolean columnExists(Connection connection, String table, String column) throws SQLException {
        try (var rs = connection.getMetaData().getColumns(null, null, table, column)) {
            return rs.next();
        }
    }

    /**
     * Closes the database connection.
     */
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Database connection closed.");
        }
    }

    /**
     * Gets the database connection.
     *
     * @return The database connection.
     * @throws SQLException If the database is not connected.
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("Database is not connected.");
        }
        return dataSource.getConnection();
    }

    /**
     * Ensures the database exists before creating the pool. Only runs when using
     * the
     * default MySQL URL (no custom JDBC URL configured).
     */
    private void ensureDatabaseExists(String host, int port, String database, String username,
            String password, boolean useSsl) {
        String adminUrl = "jdbc:mysql://" + host + ":" + port + "/?useSSL=" + useSsl;
        try (Connection connection = DriverManager.getConnection(adminUrl, username, password);
                java.sql.Statement statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE IF NOT EXISTS `" + database + "`");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING,
                    "Could not auto-create database '" + database + "'. Please create it manually.", e);
        }
    }
}
