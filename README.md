# HopperLimiter 🎯

A high-performance Minecraft plugin for limiting hoppers, chests, and barrels per chunk with SQLite-based statistics tracking.

## Features ✨

### Block Placement Limiting
- Limit hoppers, chests, and barrels per chunk
- Prevent placement when limit is reached
- Customizable limits per block type
- Real-time notifications with ChatColor formatting

### World & Region Management
- **World-specific limits**: Different limits for different worlds
- **Dynamic configuration**: Change limits without restarting
- **Hot reload**: `/hoplimit reload` to apply changes immediately

### Advanced Statistics
- **Block placement history**: Automatic recording of all placements
- **Player statistics**: Track total placements per player
- **Global statistics**: Server-wide aggregated data
- **Persistent storage**: SQLite database for long-term tracking

### User Experience
- **Formatted messages**: ChatColor-enhanced notifications
- **Sound effects**: Audio feedback for placement success/failure
- **Tab completion**: Full command autocomplete support
- **Admin commands**: Comprehensive management interface

## Installation 📦

1. **Download** the latest JAR from [Releases](https://github.com/humisan/HopperLimiter/releases)
2. **Copy** `HopperLimiter.jar` to your server's `plugins/` folder
3. **Start** the server (or reload plugins)
4. **Configure** via `plugins/HopperLimiter/config.yml`

## Configuration ⚙️

Edit `plugins/HopperLimiter/config.yml`:

```yaml
# Default block limits per chunk
limits:
  hopper: 32
  chest: 32
  barrel: 32

# World-specific limits (optional)
world-limits:
  world_nether:
    hopper: 16
    chest: 16
    barrel: 16

# Enable/disable limiters
enabled:
  hopper: true
  chest: true
  barrel: true

# Message settings
messages:
  enabled: true
  placement-notification: true

# Sound effects
sounds:
  enabled: true
  limit-sound: ENTITY_ENDERMAN_TELEPORT
  placed-sound: ENTITY_ITEM_PICKUP
```

## Commands 🎮

**Permission**: `hoplimit.admin` (default: ops)

```bash
/hoplimit help                    # Show command help
/hoplimit stats [player]          # Show chunk/player statistics
/hoplimit limits                  # Show configured limits
/hoplimit set <block> <limit>     # Set limit dynamically
/hoplimit get <block>             # Get current limit
/hoplimit global                  # Show global statistics
/hoplimit player <name>           # Show player statistics
/hoplimit world <name>            # Show world-specific limits
/hoplimit reload                  # Reload configuration
/hoplimit version                 # Show plugin version
```

**Aliases**: `/hl` (short form for all commands)

## Statistics 📊

### Global Statistics
```
/hoplimit global
```
Shows:
- Total block placements
- Total players tracked
- Distribution by block type

### Player Statistics
```
/hoplimit player <playerName>
```
Shows:
- Hopper count
- Chest count
- Barrel count
- Last placement time

### World Statistics
```
/hoplimit world <worldName>
```
Shows:
- Per-world limits
- World-specific configurations

## Database 💾

### Location
```
plugins/HopperLimiter/data/hopperlimiter.db
```

### Tables
- **placement_history**: Records every block placement event
- **player_statistics**: Aggregated stats per player

### Features
- Automatic initialization
- Persistent storage across restarts
- Optimized queries with proper indexing

## Messages & Sounds 📢

### Placement Success
```
[HopperLimiter] Hopper placed. (5/32)
```
Sound: `ENTITY_ITEM_PICKUP` (customizable)

### Limit Exceeded
```
[Error] Cannot place Hopper! Limit of 32 per chunk reached.
```
Sound: `ENTITY_ENDERMAN_TELEPORT` (customizable)

## Requirements 📋

- **Java**: 17 or higher
- **Spigot/Paper**: 1.20+ (tested on 1.20.4)
- **Memory**: ~50 MB for typical usage

## Permissions 🔐

| Permission | Default | Description |
|-----------|---------|-------------|
| `hoplimit.admin` | Op | Access all commands |

## Performance 🚀

- **Lightweight**: ~8 MB JAR file
- **Efficient**: SQLite-based storage
- **Non-blocking**: Async database operations
- **Optimized**: Native library exclusion for other platforms

## Configuration Examples 🔧

### Example 1: PvP Server (Low Limits)
```yaml
limits:
  hopper: 8
  chest: 10
  barrel: 8
```

### Example 2: Survival Server (Medium Limits)
```yaml
limits:
  hopper: 32
  chest: 32
  barrel: 32
```

### Example 3: Creative Server with World Limits
```yaml
limits:
  hopper: 64
  chest: 64
  barrel: 64

world-limits:
  creative_redstone:
    hopper: 128
    chest: 128
    barrel: 128
```

## Troubleshooting 🔧

### Plugin doesn't load
- Check Java version (17+)
- Verify JAR is in `plugins/` folder
- Check console for error messages

### Commands not working
- Ensure player has `hoplimit.admin` permission
- Try `/hoplimit help` to verify plugin is loaded

### Database errors
- Check `plugins/HopperLimiter/data/` folder exists
- Delete database file to reset (will recreate on startup)

### No sound effects
- Verify `sounds.enabled: true` in config
- Check sound type is valid (see Spigot docs)

## Version History 📝

### v1.0 (Initial Release)
- ✅ Block placement limiting
- ✅ World-specific limits
- ✅ SQLite persistence
- ✅ Admin commands
- ✅ Statistics tracking
- ✅ Tab completion

## Contributing 🤝

Contributions welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Submit a pull request

## License 📄

[Specify your license here]

## Support 💬

For issues, feature requests, or questions:
- Open an issue on [GitHub](https://github.com/humisan/HopperLimiter/issues)
- Check existing issues first

## Credits 👏

Created with ❤️ by [Hanyu]

---

**Project Status**: ✅ Active Development

**Latest Release**: [v2.0](https://github.com/humisan/HopperLimiter/releases/tag/v2.0)

## Version Management 📦

This project follows semantic versioning:
- **Patch updates** (0.1 increment): Bug fixes, minor improvements
  - Example: 1.0 → 1.1 → 1.2
- **Major updates** (1.0 increment): New features, breaking changes
  - Example: 1.0 → 2.0 → 3.0

### Version History
| Version | Release Date | Changes |
|---------|-------------|---------|
| [v2.0](https://github.com/humisan/HopperLimiter/releases/tag/v2.0) | 2025-10-26 | Block break tracking, notifications, visualization |
| [v1.1](https://github.com/humisan/HopperLimiter/releases/tag/v1.1) | 2025-10-26 | Fixed logging verbosity & hopper count |
| [v1.0](https://github.com/humisan/HopperLimiter/releases/tag/v1.0) | 2025-10-26 | Initial release |
