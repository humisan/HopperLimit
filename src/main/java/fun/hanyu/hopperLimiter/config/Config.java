package fun.hanyu.hopperLimiter.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Configuration manager for HopperLimiter plugin
 */
public class Config {
    private final JavaPlugin plugin;
    private FileConfiguration config;

    // Limit settings
    private int hopperLimit;
    private int chestLimit;
    private int barrelLimit;

    // Feature flags
    private boolean enableHopperLimiter;
    private boolean enableChestLimiter;
    private boolean enableBarrelLimiter;

    // Message settings
    private boolean enableLimitMessage;
    private boolean enablePlacedMessage;

    // Sound settings
    private boolean enableSound;
    private String limitSoundType;
    private String placedSoundType;

    public Config(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();

        // Load limits
        hopperLimit = config.getInt("limits.hopper", 32);
        chestLimit = config.getInt("limits.chest", 32);
        barrelLimit = config.getInt("limits.barrel", 32);

        // Load feature flags
        enableHopperLimiter = config.getBoolean("enabled.hopper", true);
        enableChestLimiter = config.getBoolean("enabled.chest", true);
        enableBarrelLimiter = config.getBoolean("enabled.barrel", true);

        // Load message settings
        enableLimitMessage = config.getBoolean("messages.enabled", true);
        enablePlacedMessage = config.getBoolean("messages.placement-notification", true);

        // Load sound settings
        enableSound = config.getBoolean("sounds.enabled", true);
        limitSoundType = config.getString("sounds.limit-sound", "ENTITY_ENDERMAN_TELEPORT");
        placedSoundType = config.getString("sounds.placed-sound", "ENTITY_ITEM_PICKUP");
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        loadConfig();
    }

    // Getters
    public int getHopperLimit() {
        return hopperLimit;
    }

    public int getChestLimit() {
        return chestLimit;
    }

    public int getBarrelLimit() {
        return barrelLimit;
    }

    public boolean isHopperLimiterEnabled() {
        return enableHopperLimiter;
    }

    public boolean isChestLimiterEnabled() {
        return enableChestLimiter;
    }

    public boolean isBarrelLimiterEnabled() {
        return enableBarrelLimiter;
    }

    public boolean isLimitMessageEnabled() {
        return enableLimitMessage;
    }

    public boolean isPlacedMessageEnabled() {
        return enablePlacedMessage;
    }

    public boolean isSoundEnabled() {
        return enableSound;
    }

    public String getLimitSoundType() {
        return limitSoundType;
    }

    public String getPlacedSoundType() {
        return placedSoundType;
    }

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

    public boolean isEnabled(String blockType) {
        switch (blockType.toLowerCase()) {
            case "hopper":
                return enableHopperLimiter;
            case "chest":
                return enableChestLimiter;
            case "barrel":
                return enableBarrelLimiter;
            default:
                return false;
        }
    }

    /**
     * Set limit dynamically (in memory, not persisted)
     */
    public void setLimit(String blockType, int limit) {
        switch (blockType.toLowerCase()) {
            case "hopper":
                this.hopperLimit = limit;
                break;
            case "chest":
                this.chestLimit = limit;
                break;
            case "barrel":
                this.barrelLimit = limit;
                break;
        }
    }

    /**
     * Set enabled status dynamically (in memory, not persisted)
     */
    public void setEnabled(String blockType, boolean enabled) {
        switch (blockType.toLowerCase()) {
            case "hopper":
                this.enableHopperLimiter = enabled;
                break;
            case "chest":
                this.enableChestLimiter = enabled;
                break;
            case "barrel":
                this.enableBarrelLimiter = enabled;
                break;
        }
    }
}
