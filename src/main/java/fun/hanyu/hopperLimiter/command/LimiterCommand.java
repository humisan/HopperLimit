package fun.hanyu.hopperLimiter.command;

import fun.hanyu.hopperLimiter.HopperLimiter;
import fun.hanyu.hopperLimiter.config.Config;
import fun.hanyu.hopperLimiter.config.WorldLimitManager;
import fun.hanyu.hopperLimiter.message.Message;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command handler for /hoplimit command
 */
public class LimiterCommand implements CommandExecutor {
    private final HopperLimiter plugin;
    private final Config config;
    private final WorldLimitManager worldLimitManager;
    private final AdvancedCommand advancedCommand;

    public LimiterCommand(HopperLimiter plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.worldLimitManager = plugin.getWorldLimitManager();
        this.advancedCommand = new AdvancedCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("hoplimit.admin")) {
            if (sender instanceof Player) {
                Message.sendError((Player) sender, "You do not have permission to use this command!");
            }
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        // No arguments - show help
        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "help":
                showHelp(player);
                return true;
            case "stats":
                advancedCommand.handleStats(player, args);
                return true;
            case "limits":
                showLimits(player);
                return true;
            case "reload":
                reloadConfig(player);
                return true;
            case "version":
                showVersion(player);
                return true;
            case "set":
                advancedCommand.handleSet(player, args);
                return true;
            case "get":
                advancedCommand.handleGet(player, args);
                return true;
            case "global":
                advancedCommand.handleGlobal(player);
                return true;
            case "player":
                advancedCommand.handlePlayer(player, args);
                return true;
            case "world":
                advancedCommand.handleWorld(player, args);
                return true;
            case "map":
                advancedCommand.handleMap(player, args);
                return true;
            case "hotspots":
                advancedCommand.handleHotspots(player, args);
                return true;
            default:
                Message.sendError(player, "Unknown subcommand! Use /hoplimit help for help.");
                return true;
        }
    }

    /**
     * Show help message
     */
    private void showHelp(Player player) {
        Message.sendHelpHeader(player);
        Message.sendHelpLine(player, "/hoplimit help", "Show this help message");
        Message.sendHelpLine(player, "/hoplimit stats [player]", "Show chunk/player statistics");
        Message.sendHelpLine(player, "/hoplimit limits", "Show configured limits");
        Message.sendHelpLine(player, "/hoplimit set <block> <limit>", "Set limit dynamically");
        Message.sendHelpLine(player, "/hoplimit get <block>", "Get current limit");
        Message.sendHelpLine(player, "/hoplimit global", "Show global statistics");
        Message.sendHelpLine(player, "/hoplimit player <name>", "Show player statistics");
        Message.sendHelpLine(player, "/hoplimit world <name>", "Show world limits");
        Message.sendHelpLine(player, "/hoplimit map [radius]", "Display chunk visualization");
        Message.sendHelpLine(player, "/hoplimit hotspots [limit]", "Show top chunk hotspots");
        Message.sendHelpLine(player, "/hoplimit reload", "Reload configuration");
        Message.sendHelpLine(player, "/hoplimit version", "Show plugin version");
    }

    /**
     * Show chunk statistics
     */
    private void showStats(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();
        String worldName = chunk.getWorld().getName();

        int hopperCount = countBlocksInChunk(chunk, Material.HOPPER);
        int chestCount = countBlocksInChunk(chunk, Material.CHEST);
        int barrelCount = countBlocksInChunk(chunk, Material.BARREL);

        int hopperLimit = worldLimitManager.getHopperLimit(worldName, config);
        int chestLimit = worldLimitManager.getChestLimit(worldName, config);
        int barrelLimit = worldLimitManager.getBarrelLimit(worldName, config);

        Message.sendStatsHeader(player, chunkX, chunkZ);
        Message.sendBlockCount(player, "Hopper", hopperCount, hopperLimit);
        Message.sendBlockCount(player, "Chest", chestCount, chestLimit);
        Message.sendBlockCount(player, "Barrel", barrelCount, barrelLimit);
    }

    /**
     * Show current limits
     */
    private void showLimits(Player player) {
        Message.sendCurrentLimits(player, config.getHopperLimit(), config.getChestLimit(), config.getBarrelLimit());
    }

    /**
     * Reload configuration
     */
    private void reloadConfig(Player player) {
        config.reloadConfig();
        worldLimitManager.reload();
        plugin.getStorageManager().saveData();
        Message.sendConfigReloaded(player);
        plugin.getLogger().info("Configuration reloaded by " + player.getName());
    }

    /**
     * Show plugin version
     */
    private void showVersion(Player player) {
        Message.sendInfo(player, "HopperLimiter version " + plugin.getDescription().getVersion());
    }

    /**
     * Count blocks of a specific type in the chunk
     */
    private int countBlocksInChunk(Chunk chunk, Material material) {
        int count = 0;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = chunk.getWorld().getMinHeight(); y < chunk.getWorld().getMaxHeight(); y++) {
                    if (chunk.getBlock(x, y, z).getType() == material) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
}
