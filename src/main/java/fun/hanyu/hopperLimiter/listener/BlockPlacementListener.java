package fun.hanyu.hopperLimiter.listener;

import fun.hanyu.hopperLimiter.HopperLimiter;
import fun.hanyu.hopperLimiter.config.Config;
import fun.hanyu.hopperLimiter.config.WorldLimitManager;
import fun.hanyu.hopperLimiter.message.Message;
import fun.hanyu.hopperLimiter.sound.SoundManager;
import fun.hanyu.hopperLimiter.storage.StorageManager;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Listener for block placement events to enforce chunk limits
 */
public class BlockPlacementListener implements Listener {
    private final HopperLimiter plugin;
    private final Config config;
    private final WorldLimitManager worldLimitManager;
    private final SoundManager soundManager;
    private final StorageManager storageManager;

    public BlockPlacementListener(HopperLimiter plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.worldLimitManager = plugin.getWorldLimitManager();
        this.soundManager = plugin.getSoundManager();
        this.storageManager = plugin.getStorageManager();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Material material = block.getType();
        Player player = event.getPlayer();

        // Check if the block type is one we're limiting
        String blockType = getBlockType(material);
        if (blockType == null || !config.isEnabled(blockType)) {
            return;
        }

        // Count existing blocks of this type in the chunk (before placement)
        int count = countBlocksInChunk(block.getChunk(), material);

        // Get the limit for this block type (world-specific or default)
        String worldName = block.getWorld().getName();
        int limit = worldLimitManager.getLimit(worldName, blockType, config);

        // Check if limit is exceeded (count is the number BEFORE this block is placed)
        if (count >= limit) {
            // Limit reached - cancel the event and notify
            event.setCancelled(true);

            // Send message to player
            if (config.isLimitMessageEnabled()) {
                Message.sendLimitExceededMessage(player, blockType, limit);
            }

            // Play sound if enabled
            if (config.isSoundEnabled()) {
                soundManager.playLimitSound(player);
            }

            // Log the failed placement attempt (only if debug enabled)
            if (config.isDebugEnabled()) {
                plugin.getLogger().info("Block placement denied for " + player.getName() +
                        " at chunk (" + block.getChunk().getX() + "," + block.getChunk().getZ() +
                        ") - Limit reached for " + blockType);
            }
        } else {
            // Block placed successfully
            if (config.isPlacedMessageEnabled()) {
                Message.sendPlacedMessage(player, blockType, count + 1, limit);
            }

            // Play success sound if enabled
            if (config.isSoundEnabled()) {
                soundManager.playPlacedSound(player);
            }

            // Record placement in storage
            storageManager.recordPlacement(
                    player.getName(),
                    blockType,
                    worldName,
                    block.getChunk().getX(),
                    block.getChunk().getZ()
            );

            // Log the successful placement (only if debug enabled)
            if (config.isDebugEnabled()) {
                plugin.getLogger().info("Block placement logged: " + player.getName() +
                        " placed " + blockType + " at chunk (" + block.getChunk().getX() +
                        "," + block.getChunk().getZ() + ")");
            }
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

    /**
     * Count blocks of a specific type in the chunk
     */
    private int countBlocksInChunk(org.bukkit.Chunk chunk, Material material) {
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
