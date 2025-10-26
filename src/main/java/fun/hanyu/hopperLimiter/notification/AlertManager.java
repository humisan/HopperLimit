package fun.hanyu.hopperLimiter.notification;

import fun.hanyu.hopperLimiter.HopperLimiter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Manages notifications and alerts for admins
 */
public class AlertManager {
    private final HopperLimiter plugin;
    private static final String ALERT_PREFIX = "§c[HopperLimiter Alert]§r ";

    public AlertManager(HopperLimiter plugin) {
        this.plugin = plugin;
    }

    /**
     * Notify admins that a chunk is approaching capacity
     */
    public void notifyChunkCapacityWarning(String world, int chunkX, int chunkZ, int current, int limit, double percentage) {
        if (!plugin.getConfigManager().isCapacityAlertEnabled()) {
            return;
        }

        int threshold = plugin.getConfigManager().getCapacityAlertThreshold();
        if (percentage >= threshold) {
            String message = ALERT_PREFIX + String.format(
                    "§6%s (%.0f%%) - Chunk (%d, %d) approaching capacity!",
                    world, percentage, chunkX, chunkZ
            );
            broadcastToAdmins(message);
        }
    }

    /**
     * Notify admins of rapid placement pattern
     */
    public void notifyRapidPlacement(String playerName, String blockType, int count, int timeSeconds) {
        if (!plugin.getConfigManager().isRapidPlacementAlertEnabled()) {
            return;
        }

        int threshold = plugin.getConfigManager().getRapidPlacementThreshold();
        if (count >= threshold) {
            String message = ALERT_PREFIX + String.format(
                    "§e%s§r placed §6%d %ss§r in %d seconds!",
                    playerName, count, blockType, timeSeconds
            );
            broadcastToAdmins(message);
        }
    }

    /**
     * Broadcast message to all admins
     */
    private void broadcastToAdmins(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("hoplimit.admin")) {
                player.sendMessage(message);
            }
        }
    }
}
