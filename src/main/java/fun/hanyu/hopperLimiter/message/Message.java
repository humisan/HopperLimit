package fun.hanyu.hopperLimiter.message;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Message formatting and delivery class
 */
public class Message {
    private static final String PREFIX = ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "HopperLimiter" + ChatColor.DARK_AQUA + "] " + ChatColor.RESET;
    private static final String ERROR_PREFIX = ChatColor.DARK_RED + "[" + ChatColor.RED + "Error" + ChatColor.DARK_RED + "] " + ChatColor.RESET;
    private static final String SUCCESS_PREFIX = ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "Success" + ChatColor.DARK_GREEN + "] " + ChatColor.RESET;

    /**
     * Send a limit exceeded message
     */
    public static void sendLimitExceededMessage(Player player, String blockType, int limit) {
        String message = PREFIX + ChatColor.RED + "Cannot place " + ChatColor.YELLOW + blockType +
                ChatColor.RED + "! Limit of " + ChatColor.YELLOW + limit +
                ChatColor.RED + " per chunk reached.";
        player.sendMessage(message);
    }

    /**
     * Send a block placement notification
     */
    public static void sendPlacedMessage(Player player, String blockType, int count, int limit) {
        String message = PREFIX + ChatColor.GREEN + blockType + " placed. " +
                ChatColor.GRAY + "(" + ChatColor.YELLOW + count + ChatColor.GRAY + "/" +
                ChatColor.YELLOW + limit + ChatColor.GRAY + ")";
        player.sendMessage(message);
    }

    /**
     * Send an info message
     */
    public static void sendInfo(Player player, String message) {
        player.sendMessage(PREFIX + ChatColor.GRAY + message);
    }

    /**
     * Send a success message
     */
    public static void sendSuccess(Player player, String message) {
        player.sendMessage(SUCCESS_PREFIX + ChatColor.GREEN + message);
    }

    /**
     * Send an error message
     */
    public static void sendError(Player player, String message) {
        player.sendMessage(ERROR_PREFIX + ChatColor.RED + message);
    }

    /**
     * Send a command help header
     */
    public static void sendHelpHeader(Player player) {
        player.sendMessage(ChatColor.DARK_AQUA + "=== " + ChatColor.AQUA + "HopperLimiter Commands" + ChatColor.DARK_AQUA + " ===");
    }

    /**
     * Send a command help line
     */
    public static void sendHelpLine(Player player, String command, String description) {
        player.sendMessage(ChatColor.AQUA + "  " + command + ChatColor.GRAY + " - " + description);
    }

    /**
     * Send chunk statistics header
     */
    public static void sendStatsHeader(Player player, int chunkX, int chunkZ) {
        player.sendMessage(ChatColor.DARK_AQUA + "=== " + ChatColor.AQUA + "Chunk Statistics (X:" +
                ChatColor.YELLOW + chunkX + ChatColor.AQUA + ", Z:" + ChatColor.YELLOW + chunkZ + ChatColor.AQUA + ")" +
                ChatColor.DARK_AQUA + " ===");
    }

    /**
     * Send block count line
     */
    public static void sendBlockCount(Player player, String blockType, int count, int limit) {
        String status;
        ChatColor statusColor;
        if (count >= limit) {
            statusColor = ChatColor.RED;
            status = "FULL";
        } else if (count >= limit * 0.75) {
            statusColor = ChatColor.YELLOW;
            status = "HIGH";
        } else {
            statusColor = ChatColor.GREEN;
            status = "OK";
        }

        player.sendMessage(ChatColor.GRAY + "  " + ChatColor.AQUA + blockType + ": " +
                ChatColor.YELLOW + count + ChatColor.GRAY + "/" + ChatColor.YELLOW + limit +
                ChatColor.GRAY + " [" + statusColor + status + ChatColor.GRAY + "]");
    }

    /**
     * Send config reload confirmation
     */
    public static void sendConfigReloaded(Player player) {
        player.sendMessage(SUCCESS_PREFIX + ChatColor.GREEN + "Configuration reloaded successfully!");
    }

    /**
     * Send current limits info
     */
    public static void sendCurrentLimits(Player player, int hopperLimit, int chestLimit, int barrelLimit) {
        player.sendMessage(ChatColor.DARK_AQUA + "=== " + ChatColor.AQUA + "Current Limits" + ChatColor.DARK_AQUA + " ===");
        player.sendMessage(ChatColor.AQUA + "  Hopper: " + ChatColor.YELLOW + hopperLimit);
        player.sendMessage(ChatColor.AQUA + "  Chest: " + ChatColor.YELLOW + chestLimit);
        player.sendMessage(ChatColor.AQUA + "  Barrel: " + ChatColor.YELLOW + barrelLimit);
    }
}
