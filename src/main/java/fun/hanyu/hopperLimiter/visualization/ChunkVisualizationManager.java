package fun.hanyu.hopperLimiter.visualization;

import fun.hanyu.hopperLimiter.HopperLimiter;
import fun.hanyu.hopperLimiter.storage.StorageManager;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Manages chunk visualization and heatmap functionality
 */
public class ChunkVisualizationManager {
    private final HopperLimiter plugin;
    private final StorageManager storageManager;

    public ChunkVisualizationManager(HopperLimiter plugin) {
        this.plugin = plugin;
        this.storageManager = plugin.getStorageManager();
    }

    /**
     * Get hotspots (top chunks with most blocks)
     */
    public List<ChunkHotspot> getHotspots(String world, int limit) {
        List<ChunkHotspot> hotspots = new ArrayList<>();

        try {
            // Get chunk block counts from database
            Map<String, Integer> chunkCounts = storageManager.getChunkBlockCounts(world);

            chunkCounts.entrySet().stream()
                    .map(entry -> parseChunkKey(entry.getKey(), entry.getValue()))
                    .filter(Objects::nonNull)
                    .sorted((a, b) -> Integer.compare(b.count, a.count))
                    .limit(limit)
                    .forEach(hotspots::add);
        } catch (Exception e) {
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().warning("Failed to get hotspots: " + e.getMessage());
            }
        }

        return hotspots;
    }

    /**
     * Display chunk visualization for nearby chunks
     */
    public void displayNearbyChunks(Player player, int radius) {
        player.sendMessage("§6=== Nearby Chunks (radius: " + radius + ") ===");
        player.sendMessage("§6World: §f" + player.getWorld().getName());

        int playerChunkX = player.getLocation().getChunk().getX();
        int playerChunkZ = player.getLocation().getChunk().getZ();

        for (int x = playerChunkX - radius; x <= playerChunkX + radius; x++) {
            StringBuilder line = new StringBuilder("§r");
            for (int z = playerChunkZ - radius; z <= playerChunkZ + radius; z++) {
                int count = storageManager.getChunkBlockCount(player.getWorld().getName(), x, z);
                int limit = plugin.getConfigManager().getHopperLimit(); // Simple default

                char indicator = getChunkIndicator(count, limit);
                String color = getChunkColor(count, limit);

                if (x == playerChunkX && z == playerChunkZ) {
                    line.append("§e[").append(indicator).append("]§r");
                } else {
                    line.append(color).append(indicator).append("§r ");
                }
            }
            player.sendMessage(line.toString());
        }

        player.sendMessage("§6Legend: §a■=Empty §e●=Moderate §c■=Full §e[Y]=You");
    }

    /**
     * Get chunk indicator character based on capacity
     */
    private char getChunkIndicator(int count, int limit) {
        if (count == 0) return '□';
        if (count < (limit * 0.5)) return '◐';
        if (count < (limit * 0.8)) return '◑';
        if (count < limit) return '◕';
        return '●';
    }

    /**
     * Get chunk color based on capacity
     */
    private String getChunkColor(int count, int limit) {
        if (count == 0) return "§a";
        if (count < (limit * 0.5)) return "§a";
        if (count < (limit * 0.8)) return "§e";
        if (count < limit) return "§6";
        return "§c";
    }

    /**
     * Parse chunk key from storage format
     */
    private ChunkHotspot parseChunkKey(String key, int count) {
        try {
            String[] parts = key.split("_");
            if (parts.length == 2) {
                int chunkX = Integer.parseInt(parts[0]);
                int chunkZ = Integer.parseInt(parts[1]);
                return new ChunkHotspot(chunkX, chunkZ, count);
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return null;
    }

    /**
     * Inner class for chunk hotspot data
     */
    public static class ChunkHotspot {
        public final int chunkX;
        public final int chunkZ;
        public final int count;

        public ChunkHotspot(int chunkX, int chunkZ, int count) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.count = count;
        }

        @Override
        public String toString() {
            return String.format("(%d, %d): %d blocks", chunkX, chunkZ, count);
        }
    }
}
