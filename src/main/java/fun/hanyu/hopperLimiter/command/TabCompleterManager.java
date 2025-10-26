package fun.hanyu.hopperLimiter.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Advanced TabCompleter with filtering and context-aware suggestions
 */
public class TabCompleterManager implements TabCompleter {
    private final LimiterCommand limiterCommand;

    // Main subcommands
    private final List<String> mainSubcommands = Arrays.asList(
            "help",
            "stats",
            "limits",
            "reload",
            "version",
            "set",
            "get",
            "global",
            "player",
            "world"
    );

    // Block types for future expansion
    private final List<String> blockTypes = Arrays.asList(
            "hopper",
            "chest",
            "barrel"
    );

    public TabCompleterManager(LimiterCommand limiterCommand) {
        this.limiterCommand = limiterCommand;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        // Only provide suggestions for players with admin permission
        if (!(sender instanceof Player)) {
            return completions;
        }

        if (!sender.hasPermission("hoplimit.admin")) {
            return completions;
        }

        // Prevent out of bounds errors
        if (args.length == 0) {
            return completions;
        }

        // First argument - subcommand completion
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            completions.addAll(
                    mainSubcommands.stream()
                            .filter(cmd -> cmd.startsWith(input))
                            .collect(Collectors.toList())
            );
        }

        // Second argument - context-aware completion
        if (args.length >= 2) {
            String subcommand = args[0].toLowerCase();
            String input = args[args.length - 1].toLowerCase();

            switch (subcommand) {
                case "set":
                case "get":
                    if (args.length == 2) {
                        completions.addAll(
                                blockTypes.stream()
                                        .filter(block -> block.startsWith(input))
                                        .collect(Collectors.toList())
                        );
                    }
                    break;
                default:
                    // No additional completions for other commands
                    break;
            }
        }

        return completions;
    }

    /**
     * Get all available subcommands
     */
    public List<String> getMainSubcommands() {
        return new ArrayList<>(mainSubcommands);
    }

    /**
     * Get all block types
     */
    public List<String> getBlockTypes() {
        return new ArrayList<>(blockTypes);
    }

    /**
     * Filter completions based on input prefix
     */
    public List<String> filterCompletions(List<String> options, String input) {
        return options.stream()
                .filter(opt -> opt.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
}
