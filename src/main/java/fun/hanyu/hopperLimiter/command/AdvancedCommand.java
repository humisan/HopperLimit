package fun.hanyu.hopperLimiter.command;

import fun.hanyu.hopperLimiter.HopperLimiter;
import fun.hanyu.hopperLimiter.config.Config;
import fun.hanyu.hopperLimiter.config.WorldLimitManager;
import fun.hanyu.hopperLimiter.message.Message;
import fun.hanyu.hopperLimiter.storage.StorageManager;
import fun.hanyu.hopperLimiter.visualization.ChunkVisualizationManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/**
 * Advanced command handler for set, get, and statistics commands
 */
public class AdvancedCommand {
    private final HopperLimiter plugin;
    private final Config config;
    private final WorldLimitManager worldLimitManager;
    private final StorageManager storageManager;

    public AdvancedCommand(HopperLimiter plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.worldLimitManager = plugin.getWorldLimitManager();
        this.storageManager = plugin.getStorageManager();
    }

    /**
     * Handle set command: /hoplimit set <blockType> <limit>
     */
    public void handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hoplimit.admin")) {
            Message.sendError((Player) sender, "You do not have permission!");
            return;
        }

        if (args.length < 3) {
            Message.sendError((Player) sender, "Usage: /hoplimit set <hopper|chest|barrel> <limit>");
            return;
        }

        String blockType = args[1].toLowerCase();
        String limitStr = args[2];

        // Validate block type
        if (!isValidBlockType(blockType)) {
            Message.sendError((Player) sender, "Invalid block type! Use: hopper, chest, or barrel");
            return;
        }

        // Validate limit value
        int limit;
        try {
            limit = Integer.parseInt(limitStr);
            if (limit < 1) {
                Message.sendError((Player) sender, "Limit must be greater than 0!");
                return;
            }
        } catch (NumberFormatException e) {
            Message.sendError((Player) sender, "Limit must be a valid number!");
            return;
        }

        // Update config in memory (not persisted to file)
        config.setLimit(blockType, limit);

        Player player = (Player) sender;
        Message.sendSuccess(player, "Set " + blockType + " limit to " + limit);
        plugin.getLogger().info("Limit for " + blockType + " changed to " + limit + " by " + player.getName());
    }

    /**
     * Handle get command: /hoplimit get <blockType>
     */
    public void handleGet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hoplimit.admin")) {
            Message.sendError((Player) sender, "You do not have permission!");
            return;
        }

        if (args.length < 2) {
            Message.sendError((Player) sender, "Usage: /hoplimit get <hopper|chest|barrel>");
            return;
        }

        String blockType = args[1].toLowerCase();

        // Validate block type
        if (!isValidBlockType(blockType)) {
            Message.sendError((Player) sender, "Invalid block type! Use: hopper, chest, or barrel");
            return;
        }

        Player player = (Player) sender;
        int limit = config.getLimit(blockType);
        Message.sendInfo(player, blockType + " limit is currently set to " + ChatColor.YELLOW + limit);
    }

    /**
     * Handle stats command: /hoplimit stats [player]
     */
    public void handleStats(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hoplimit.admin")) {
            Message.sendError((Player) sender, "You do not have permission!");
            return;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            // Show current chunk stats
            showChunkStats(player);
        } else {
            // Show player stats
            showPlayerStats(player, args[1]);
        }
    }

    /**
     * Handle global statistics: /hoplimit global
     */
    public void handleGlobal(CommandSender sender) {
        if (!sender.hasPermission("hoplimit.admin")) {
            Message.sendError((Player) sender, "You do not have permission!");
            return;
        }

        Player player = (Player) sender;
        StorageManager.GlobalStatistics globalStats = storageManager.getGlobalStatistics();

        player.sendMessage(ChatColor.DARK_AQUA + "=== " + ChatColor.AQUA + "Global Statistics" + ChatColor.DARK_AQUA + " ===");
        player.sendMessage(ChatColor.AQUA + "Total Placements: " + ChatColor.YELLOW + globalStats.getTotalPlacements());
        player.sendMessage(ChatColor.AQUA + "Total Players: " + ChatColor.YELLOW + globalStats.getTotalPlayers());
        player.sendMessage(ChatColor.AQUA + "Block Distribution:");

        for (Map.Entry<String, Integer> entry : globalStats.getBlockCounts().entrySet()) {
            player.sendMessage(ChatColor.GRAY + "  " + ChatColor.AQUA + entry.getKey() + ": " +
                    ChatColor.YELLOW + entry.getValue());
        }
    }

    /**
     * Handle player statistics: /hoplimit player <playerName>
     */
    public void handlePlayer(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hoplimit.admin")) {
            Message.sendError((Player) sender, "You do not have permission!");
            return;
        }

        if (args.length < 2) {
            Message.sendError((Player) sender, "Usage: /hoplimit player <playerName>");
            return;
        }

        Player player = (Player) sender;
        String targetPlayer = args[1];
        StorageManager.PlayerStatistics stats = storageManager.getPlayerStatistics(targetPlayer);

        if (stats == null) {
            Message.sendError(player, "No statistics found for player: " + targetPlayer);
            return;
        }

        player.sendMessage(ChatColor.DARK_AQUA + "=== " + ChatColor.AQUA + "Player Statistics: " +
                ChatColor.YELLOW + targetPlayer + ChatColor.DARK_AQUA + " ===");
        player.sendMessage(ChatColor.AQUA + "Hoppers: " + ChatColor.YELLOW + stats.getHopperCount());
        player.sendMessage(ChatColor.AQUA + "Chests: " + ChatColor.YELLOW + stats.getChestCount());
        player.sendMessage(ChatColor.AQUA + "Barrels: " + ChatColor.YELLOW + stats.getBarrelCount());
        player.sendMessage(ChatColor.AQUA + "Last Placement: " + ChatColor.YELLOW + stats.getLastPlacement());
    }

    /**
     * Handle world-specific limits: /hoplimit world <worldName>
     */
    public void handleWorld(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hoplimit.admin")) {
            Message.sendError((Player) sender, "You do not have permission!");
            return;
        }

        if (args.length < 2) {
            Message.sendError((Player) sender, "Usage: /hoplimit world <worldName>");
            return;
        }

        Player player = (Player) sender;
        String worldName = args[1];
        WorldLimitManager.WorldLimits limits = worldLimitManager.getWorldLimits(worldName, config);

        player.sendMessage(ChatColor.DARK_AQUA + "=== " + ChatColor.AQUA + "World Limits: " +
                ChatColor.YELLOW + worldName + ChatColor.DARK_AQUA + " ===");
        player.sendMessage(ChatColor.AQUA + "Hopper: " + ChatColor.YELLOW + limits.getHopperLimit());
        player.sendMessage(ChatColor.AQUA + "Chest: " + ChatColor.YELLOW + limits.getChestLimit());
        player.sendMessage(ChatColor.AQUA + "Barrel: " + ChatColor.YELLOW + limits.getBarrelLimit());
    }

    // ==================== Private Helper Methods ====================

    private void showChunkStats(Player player) {
        org.bukkit.Chunk chunk = player.getLocation().getChunk();
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        int hopperCount = countBlocksInChunk(chunk, org.bukkit.Material.HOPPER);
        int chestCount = countBlocksInChunk(chunk, org.bukkit.Material.CHEST);
        int barrelCount = countBlocksInChunk(chunk, org.bukkit.Material.BARREL);

        String world = chunk.getWorld().getName();
        int hopperLimit = worldLimitManager.getHopperLimit(world, config);
        int chestLimit = worldLimitManager.getChestLimit(world, config);
        int barrelLimit = worldLimitManager.getBarrelLimit(world, config);

        Message.sendStatsHeader(player, chunkX, chunkZ);
        Message.sendBlockCount(player, "Hopper", hopperCount, hopperLimit);
        Message.sendBlockCount(player, "Chest", chestCount, chestLimit);
        Message.sendBlockCount(player, "Barrel", barrelCount, barrelLimit);
    }

    private void showPlayerStats(Player player, String targetPlayerName) {
        StorageManager.PlayerStatistics stats = storageManager.getPlayerStatistics(targetPlayerName);

        if (stats == null) {
            Message.sendError(player, "No statistics found for player: " + targetPlayerName);
            return;
        }

        player.sendMessage(ChatColor.DARK_AQUA + "=== " + ChatColor.AQUA + "Player Statistics: " +
                ChatColor.YELLOW + targetPlayerName + ChatColor.DARK_AQUA + " ===");
        player.sendMessage(ChatColor.AQUA + "Hoppers: " + ChatColor.YELLOW + stats.getHopperCount());
        player.sendMessage(ChatColor.AQUA + "Chests: " + ChatColor.YELLOW + stats.getChestCount());
        player.sendMessage(ChatColor.AQUA + "Barrels: " + ChatColor.YELLOW + stats.getBarrelCount());
        player.sendMessage(ChatColor.AQUA + "Last Placement: " + ChatColor.YELLOW + stats.getLastPlacement());
    }

    private int countBlocksInChunk(org.bukkit.Chunk chunk, org.bukkit.Material material) {
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

    private boolean isValidBlockType(String blockType) {
        switch (blockType.toLowerCase()) {
            case "hopper":
            case "chest":
            case "barrel":
                return true;
            default:
                return false;
        }
    }

    /**
     * Handle map command: /hoplimit map [radius]
     */
    public void handleMap(Player player, String[] args) {
        if (!player.hasPermission("hoplimit.admin")) {
            Message.sendError(player, "You do not have permission!");
            return;
        }

        int radius = 3;
        if (args.length >= 2) {
            try {
                radius = Integer.parseInt(args[1]);
                radius = Math.min(radius, 10); // Max radius of 10
            } catch (NumberFormatException e) {
                Message.sendError(player, "Invalid radius! Using default (3)");
            }
        }

        ChunkVisualizationManager vizManager = new ChunkVisualizationManager(plugin);
        vizManager.displayNearbyChunks(player, radius);
    }

    /**
     * Handle hotspots command: /hoplimit hotspots [limit]
     */
    public void handleHotspots(Player player, String[] args) {
        if (!player.hasPermission("hoplimit.admin")) {
            Message.sendError(player, "You do not have permission!");
            return;
        }

        int limit = 10;
        if (args.length >= 2) {
            try {
                limit = Integer.parseInt(args[1]);
                limit = Math.min(limit, 50);
            } catch (NumberFormatException e) {
                Message.sendError(player, "Invalid limit! Using default (10)");
            }
        }

        ChunkVisualizationManager vizManager = new ChunkVisualizationManager(plugin);
        List<ChunkVisualizationManager.ChunkHotspot> hotspots = vizManager.getHotspots(player.getWorld().getName(), limit);

        player.sendMessage(ChatColor.DARK_AQUA + "=== " + ChatColor.AQUA + "Top Chunk Hotspots " +
                ChatColor.DARK_AQUA + "===");
        player.sendMessage(ChatColor.AQUA + "World: " + ChatColor.YELLOW + player.getWorld().getName());

        if (hotspots.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No hotspots found!");
            return;
        }

        int rank = 1;
        for (ChunkVisualizationManager.ChunkHotspot hotspot : hotspots) {
            int percentage = (hotspot.count * 100) / config.getHopperLimit(); // Rough estimate
            String color;
            if (percentage >= 90) {
                color = ChatColor.RED.toString();
            } else if (percentage >= 70) {
                color = ChatColor.GOLD.toString();
            } else {
                color = ChatColor.GREEN.toString();
            }
            player.sendMessage(ChatColor.AQUA + "#" + rank + " " + color + hotspot +
                    ChatColor.AQUA + " (" + percentage + "%)");
            rank++;
        }
    }
}
