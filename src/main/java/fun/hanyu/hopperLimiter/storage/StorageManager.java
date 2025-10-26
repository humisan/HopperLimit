package fun.hanyu.hopperLimiter.storage;

import fun.hanyu.hopperLimiter.HopperLimiter;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Manages data storage and persistence using SQLite
 */
public class StorageManager {
    private final HopperLimiter plugin;
    private final File dbFile;
    private Connection connection;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StorageManager(HopperLimiter plugin) {
        this.plugin = plugin;
        this.dbFile = new File(plugin.getDataFolder(), "hopperlimiter.db");

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        try {
            initializeDatabase();
        } catch (Exception e) {
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().warning("Failed to initialize storage: " + e.getMessage());
            }
        }
    }

    /**
     * Initialize database and create tables if needed
     */
    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            connection.setAutoCommit(true);

            // Create tables
            try (Statement stmt = connection.createStatement()) {
                // Placement history table
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS placement_history (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "timestamp LONG NOT NULL," +
                    "player_name TEXT NOT NULL," +
                    "block_type TEXT NOT NULL," +
                    "world TEXT NOT NULL," +
                    "chunk_x INTEGER NOT NULL," +
                    "chunk_z INTEGER NOT NULL," +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")");

                // Player statistics table
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS player_statistics (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "player_name TEXT UNIQUE NOT NULL," +
                    "hopper_count INTEGER DEFAULT 0," +
                    "chest_count INTEGER DEFAULT 0," +
                    "barrel_count INTEGER DEFAULT 0," +
                    "last_placement TEXT," +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")");
            }

            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info("SQLite database initialized successfully!");
            }
        } catch (SQLException e) {
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Record a block placement event
     */
    public void recordPlacement(String playerName, String blockType, String world, int chunkX, int chunkZ) {
        try {
            // Insert into placement history
            String insertHistorySql = "INSERT INTO placement_history (timestamp, player_name, block_type, world, chunk_x, chunk_z) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(insertHistorySql)) {
                pstmt.setLong(1, System.currentTimeMillis());
                pstmt.setString(2, playerName);
                pstmt.setString(3, blockType);
                pstmt.setString(4, world);
                pstmt.setInt(5, chunkX);
                pstmt.setInt(6, chunkZ);
                pstmt.executeUpdate();
            }

            // Update player statistics
            updatePlayerStatistics(playerName, blockType);
        } catch (SQLException e) {
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().warning("Failed to record placement: " + e.getMessage());
            }
        }
    }

    /**
     * Update player statistics
     */
    private void updatePlayerStatistics(String playerName, String blockType) {
        try {
            // Check if player exists
            String checkSql = "SELECT id FROM player_statistics WHERE player_name = ?";
            boolean exists = false;
            try (PreparedStatement pstmt = connection.prepareStatement(checkSql)) {
                pstmt.setString(1, playerName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    exists = rs.next();
                }
            }

            if (!exists) {
                // Insert new player
                String insertSql = "INSERT INTO player_statistics (player_name, last_placement) VALUES (?, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
                    pstmt.setString(1, playerName);
                    pstmt.setString(2, LocalDateTime.now().format(DATE_FORMATTER));
                    pstmt.executeUpdate();
                }
            }

            // Update count
            String blockColumn = blockType.toLowerCase() + "_count";
            String updateSql = "UPDATE player_statistics SET " + blockColumn + " = " + blockColumn + " + 1, " +
                    "last_placement = ?, updated_at = CURRENT_TIMESTAMP WHERE player_name = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(updateSql)) {
                pstmt.setString(1, LocalDateTime.now().format(DATE_FORMATTER));
                pstmt.setString(2, playerName);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().warning("Failed to update player statistics: " + e.getMessage());
            }
        }
    }

    /**
     * Get all placement records for a specific player
     */
    public List<BlockPlacementRecord> getPlayerRecords(String playerName) {
        List<BlockPlacementRecord> records = new ArrayList<>();
        try {
            String sql = "SELECT id, timestamp, player_name, block_type, world, chunk_x, chunk_z " +
                    "FROM placement_history WHERE player_name = ? ORDER BY timestamp DESC";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, playerName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        records.add(new BlockPlacementRecord(
                                rs.getInt("id"),
                                rs.getLong("timestamp"),
                                rs.getString("player_name"),
                                rs.getString("block_type"),
                                rs.getString("world"),
                                rs.getInt("chunk_x"),
                                rs.getInt("chunk_z")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().warning("Failed to get player records: " + e.getMessage());
            }
        }
        return records;
    }

    /**
     * Get all placement records in a specific chunk
     */
    public List<BlockPlacementRecord> getChunkRecords(String world, int chunkX, int chunkZ) {
        List<BlockPlacementRecord> records = new ArrayList<>();
        try {
            String sql = "SELECT id, timestamp, player_name, block_type, world, chunk_x, chunk_z " +
                    "FROM placement_history WHERE world = ? AND chunk_x = ? AND chunk_z = ? ORDER BY timestamp DESC";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, world);
                pstmt.setInt(2, chunkX);
                pstmt.setInt(3, chunkZ);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        records.add(new BlockPlacementRecord(
                                rs.getInt("id"),
                                rs.getLong("timestamp"),
                                rs.getString("player_name"),
                                rs.getString("block_type"),
                                rs.getString("world"),
                                rs.getInt("chunk_x"),
                                rs.getInt("chunk_z")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().warning("Failed to get chunk records: " + e.getMessage());
            }
        }
        return records;
    }

    /**
     * Get statistics for a specific player
     */
    public PlayerStatistics getPlayerStatistics(String playerName) {
        try {
            String sql = "SELECT player_name, hopper_count, chest_count, barrel_count, last_placement " +
                    "FROM player_statistics WHERE player_name = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, playerName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return new PlayerStatistics(
                                rs.getString("player_name"),
                                rs.getInt("hopper_count"),
                                rs.getInt("chest_count"),
                                rs.getInt("barrel_count"),
                                rs.getString("last_placement")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().warning("Failed to get player statistics: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Get all player statistics
     */
    public Map<String, PlayerStatistics> getAllPlayerStatistics() {
        Map<String, PlayerStatistics> stats = new HashMap<>();
        try {
            String sql = "SELECT player_name, hopper_count, chest_count, barrel_count, last_placement " +
                    "FROM player_statistics ORDER BY updated_at DESC";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    PlayerStatistics stat = new PlayerStatistics(
                            rs.getString("player_name"),
                            rs.getInt("hopper_count"),
                            rs.getInt("chest_count"),
                            rs.getInt("barrel_count"),
                            rs.getString("last_placement")
                    );
                    stats.put(rs.getString("player_name"), stat);
                }
            }
        } catch (SQLException e) {
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().warning("Failed to get all player statistics: " + e.getMessage());
            }
        }
        return stats;
    }

    /**
     * Get global statistics
     */
    public GlobalStatistics getGlobalStatistics() {
        try {
            // Get total placements
            int totalPlacements = 0;
            String placementSql = "SELECT COUNT(*) as count FROM placement_history";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(placementSql)) {
                if (rs.next()) {
                    totalPlacements = rs.getInt("count");
                }
            }

            // Get total players
            int totalPlayers = 0;
            String playerSql = "SELECT COUNT(*) as count FROM player_statistics";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(playerSql)) {
                if (rs.next()) {
                    totalPlayers = rs.getInt("count");
                }
            }

            // Get block counts
            Map<String, Integer> blockCounts = new HashMap<>();
            blockCounts.put("hopper", 0);
            blockCounts.put("chest", 0);
            blockCounts.put("barrel", 0);

            String blockSql = "SELECT block_type, COUNT(*) as count FROM placement_history GROUP BY block_type";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(blockSql)) {
                while (rs.next()) {
                    blockCounts.put(rs.getString("block_type").toLowerCase(), rs.getInt("count"));
                }
            }

            return new GlobalStatistics(totalPlacements, totalPlayers, blockCounts);
        } catch (SQLException e) {
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().warning("Failed to get global statistics: " + e.getMessage());
            }
            return new GlobalStatistics(0, 0, new HashMap<>());
        }
    }

    /**
     * Save data (for consistency with JSON version)
     */
    public void saveData() {
        // SQLite auto-commits, so nothing to do here
        // But we can add explicit checkpoint if needed
        try {
            if (connection != null && !connection.isClosed()) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA optimize;");
                }
            }
        } catch (SQLException e) {
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().warning("Failed to optimize database: " + e.getMessage());
            }
        }
    }

    /**
     * Clear all records (for admin reset)
     */
    public void clearAllRecords() {
        try {
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("DELETE FROM placement_history");
                stmt.executeUpdate("DELETE FROM player_statistics");
            }
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info("All records cleared successfully!");
            }
        } catch (SQLException e) {
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().warning("Failed to clear records: " + e.getMessage());
            }
        }
    }

    /**
     * Close database connection
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                if (plugin.getConfigManager().isDebugEnabled()) {
                    plugin.getLogger().info("Database connection closed!");
                }
            }
        } catch (SQLException e) {
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().warning("Failed to close database: " + e.getMessage());
            }
        }
    }

    // ==================== Data Classes ====================

    public static class BlockPlacementRecord {
        private final int id;
        private final long timestamp;
        private final String playerName;
        private final String blockType;
        private final String world;
        private final int chunkX;
        private final int chunkZ;

        public BlockPlacementRecord(int id, long timestamp, String playerName,
                                   String blockType, String world, int chunkX, int chunkZ) {
            this.id = id;
            this.timestamp = timestamp;
            this.playerName = playerName;
            this.blockType = blockType;
            this.world = world;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }

        // Getters
        public int getId() { return id; }
        public long getTimestamp() { return timestamp; }
        public String getPlayerName() { return playerName; }
        public String getBlockType() { return blockType; }
        public String getWorld() { return world; }
        public int getChunkX() { return chunkX; }
        public int getChunkZ() { return chunkZ; }
    }

    public static class PlayerStatistics {
        private final String playerName;
        private final int hopperCount;
        private final int chestCount;
        private final int barrelCount;
        private final String lastPlacement;

        public PlayerStatistics(String playerName, int hopperCount, int chestCount, int barrelCount, String lastPlacement) {
            this.playerName = playerName;
            this.hopperCount = hopperCount;
            this.chestCount = chestCount;
            this.barrelCount = barrelCount;
            this.lastPlacement = lastPlacement != null ? lastPlacement : "Never";
        }

        public PlayerStatistics(String playerName) {
            this(playerName, 0, 0, 0, "Never");
        }

        // Getters
        public String getPlayerName() { return playerName; }
        public int getHopperCount() { return hopperCount; }
        public int getChestCount() { return chestCount; }
        public int getBarrelCount() { return barrelCount; }
        public String getLastPlacement() { return lastPlacement; }
    }

    public static class GlobalStatistics {
        private final int totalPlacements;
        private final int totalPlayers;
        private final Map<String, Integer> blockCounts;

        public GlobalStatistics(int totalPlacements, int totalPlayers, Map<String, Integer> blockCounts) {
            this.totalPlacements = totalPlacements;
            this.totalPlayers = totalPlayers;
            this.blockCounts = blockCounts;
        }

        // Getters
        public int getTotalPlacements() { return totalPlacements; }
        public int getTotalPlayers() { return totalPlayers; }
        public Map<String, Integer> getBlockCounts() { return blockCounts; }
    }
}
