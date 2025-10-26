package fun.hanyu.hopperLimiter.sound;

import fun.hanyu.hopperLimiter.HopperLimiter;
import fun.hanyu.hopperLimiter.config.Config;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Manager for playing sounds
 */
public class SoundManager {
    private final HopperLimiter plugin;
    private final Config config;

    public SoundManager(HopperLimiter plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    /**
     * Play sound when limit is exceeded
     */
    public void playLimitSound(Player player) {
        playSound(player, config.getLimitSoundType());
    }

    /**
     * Play sound when block is successfully placed
     */
    public void playPlacedSound(Player player) {
        playSound(player, config.getPlacedSoundType());
    }

    /**
     * Play a sound to the player
     */
    private void playSound(Player player, String soundName) {
        try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound type: " + soundName);
        }
    }
}
