package fun.hanyu.hopperLimiter.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages per-world and per-region block limits
 */
public class WorldLimitManager {
    private final JavaPlugin plugin;
    private final FileConfiguration config;
    private final Map<String, WorldLimits> worldLimits;

    public WorldLimitManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.worldLimits = new HashMap<>();
        loadWorldLimits();
    }

    /**
     * Load world-specific limits from config
     */
    private void loadWorldLimits() {
        worldLimits.clear();

        if (!config.contains("world-limits")) {
            return;
        }

        // Get all world names from config
        for (String world : config.getConfigurationSection("world-limits").getKeys(false)) {
            String path = "world-limits." + world;

            int hopperLimit = config.getInt(path + ".hopper", -1);
            int chestLimit = config.getInt(path + ".chest", -1);
            int barrelLimit = config.getInt(path + ".barrel", -1);

            worldLimits.put(world, new WorldLimits(world, hopperLimit, chestLimit, barrelLimit));
        }
    }

    /**
     * Get limits for a specific world
     * Returns the world-specific limits, or default if not configured
     */
    public WorldLimits getWorldLimits(String world, Config defaultConfig) {
        if (worldLimits.containsKey(world)) {
            return worldLimits.get(world);
        }

        // Return default limits if world not configured
        return new WorldLimits(
                world,
                defaultConfig.getHopperLimit(),
                defaultConfig.getChestLimit(),
                defaultConfig.getBarrelLimit()
        );
    }

    /**
     * Get hopper limit for a world
     */
    public int getHopperLimit(String world, Config defaultConfig) {
        WorldLimits limits = getWorldLimits(world, defaultConfig);
        return limits.getHopperLimit();
    }

    /**
     * Get chest limit for a world
     */
    public int getChestLimit(String world, Config defaultConfig) {
        WorldLimits limits = getWorldLimits(world, defaultConfig);
        return limits.getChestLimit();
    }

    /**
     * Get barrel limit for a world
     */
    public int getBarrelLimit(String world, Config defaultConfig) {
        WorldLimits limits = getWorldLimits(world, defaultConfig);
        return limits.getBarrelLimit();
    }

    /**
     * Get limit for a specific block type in a world
     */
    public int getLimit(String world, String blockType, Config defaultConfig) {
        WorldLimits limits = getWorldLimits(world, defaultConfig);
        switch (blockType.toLowerCase()) {
            case "hopper":
                return limits.getHopperLimit();
            case "chest":
                return limits.getChestLimit();
            case "barrel":
                return limits.getBarrelLimit();
            default:
                return 0;
        }
    }

    /**
     * Check if world has custom limits
     */
    public boolean hasCustomLimits(String world) {
        return worldLimits.containsKey(world);
    }

    /**
     * Get all configured worlds
     */
    public Map<String, WorldLimits> getAllWorldLimits() {
        return new HashMap<>(worldLimits);
    }

    /**
     * Reload world limits from config
     */
    public void reload() {
        loadWorldLimits();
    }

    // ==================== Data Class ====================

    public static class WorldLimits {
        private final String world;
        private final int hopperLimit;
        private final int chestLimit;
        private final int barrelLimit;

        public WorldLimits(String world, int hopperLimit, int chestLimit, int barrelLimit) {
            this.world = world;
            this.hopperLimit = hopperLimit;
            this.chestLimit = chestLimit;
            this.barrelLimit = barrelLimit;
        }

        // Getters
        public String getWorld() { return world; }
        public int getHopperLimit() { return hopperLimit; }
        public int getChestLimit() { return chestLimit; }
        public int getBarrelLimit() { return barrelLimit; }

        public int getLimit(String blockType) {
            switch (blockType.toLowerCase()) {
                case "hopper":
                    return hopperLimit;
                case "chest":
                    return chestLimit;
                case "barrel":
                    return barrelLimit;
                default:
                    return 0;
            }
        }
    }
}
