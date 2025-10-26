package fun.hanyu.hopperLimiter.listener;

import fun.hanyu.hopperLimiter.HopperLimiter;
import fun.hanyu.hopperLimiter.storage.StorageManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Listener for block break events to track removal
 */
public class BlockBreakListener implements Listener {
    private final HopperLimiter plugin;
    private final StorageManager storageManager;

    public BlockBreakListener(HopperLimiter plugin) {
        this.plugin = plugin;
        this.storageManager = plugin.getStorageManager();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Material material = block.getType();
        Player player = event.getPlayer();

        // Check if the block type is one we're tracking
        String blockType = getBlockType(material);
        if (blockType == null) {
            return;
        }

        // Record the removal
        storageManager.recordRemoval(
                blockType,
                block.getWorld().getName(),
                block.getChunk().getX(),
                block.getChunk().getZ(),
                player.getName()
        );

        // Log the break event (only if debug enabled)
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info("Block break recorded: " + player.getName() +
                    " broke " + blockType + " at chunk (" + block.getChunk().getX() +
                    "," + block.getChunk().getZ() + ")");
        }
    }

    /**
     * Get the block type name (hopper, chest, or barrel)
     */
    private String getBlockType(Material material) {
        switch (material) {
            case HOPPER:
                return "Hopper";
            case CHEST:
                return "Chest";
            case BARREL:
                return "Barrel";
            default:
                return null;
        }
    }
}
