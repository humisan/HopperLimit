package fun.hanyu.hopperLimiter;

import fun.hanyu.hopperLimiter.command.LimiterCommand;
import fun.hanyu.hopperLimiter.command.TabCompleterManager;
import fun.hanyu.hopperLimiter.config.Config;
import fun.hanyu.hopperLimiter.config.WorldLimitManager;
import fun.hanyu.hopperLimiter.listener.BlockPlacementListener;
import fun.hanyu.hopperLimiter.sound.SoundManager;
import fun.hanyu.hopperLimiter.storage.StorageManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class HopperLimiter extends JavaPlugin {
    private Config configManager;
    private WorldLimitManager worldLimitManager;
    private SoundManager soundManager;
    private StorageManager storageManager;

    @Override
    public void onEnable() {
        // Initialize config
        this.configManager = new Config(this);
        this.worldLimitManager = new WorldLimitManager(this);
        this.soundManager = new SoundManager(this);
        this.storageManager = new StorageManager(this);

        // Register events
        getServer().getPluginManager().registerEvents(new BlockPlacementListener(this), this);

        // Register commands
        LimiterCommand limiterCommand = new LimiterCommand(this);
        TabCompleterManager tabCompleter = new TabCompleterManager(limiterCommand);
        getCommand("hoplimit").setExecutor(limiterCommand);
        getCommand("hoplimit").setTabCompleter(tabCompleter);

        // Log startup
        getLogger().info("HopperLimiter has been enabled!");
        getLogger().info("Hopper Limit: " + configManager.getHopperLimit());
        getLogger().info("Chest Limit: " + configManager.getChestLimit());
        getLogger().info("Barrel Limit: " + configManager.getBarrelLimit());
        getLogger().info("Storage Manager initialized - data folder: " + getDataFolder().getAbsolutePath());
    }

    @Override
    public void onDisable() {
        // Save all data and close database connection
        if (storageManager != null) {
            storageManager.saveData();
            storageManager.close();
        }
        getLogger().info("HopperLimiter has been disabled!");
    }

    public Config getConfigManager() {
        return configManager;
    }

    public WorldLimitManager getWorldLimitManager() {
        return worldLimitManager;
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }
}
