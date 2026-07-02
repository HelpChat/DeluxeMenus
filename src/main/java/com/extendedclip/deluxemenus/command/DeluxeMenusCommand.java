package com.extendedclip.deluxemenus.command;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.command.subcommand.*;
import com.extendedclip.deluxemenus.utils.DebugLevel;
import com.extendedclip.deluxemenus.utils.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.text;

public class DeluxeMenusCommand implements TabExecutor {

    private static final TextReplacementConfig.Builder VERSION_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<version>");
    private static final TextReplacementConfig.Builder AUTHORS_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<authors>");

    private final DeluxeMenus plugin;
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public DeluxeMenusCommand(final @NotNull DeluxeMenus plugin) {
        this.plugin = plugin;
    }

    public boolean register() {
        final PluginCommand command = this.plugin.getCommand("deluxemenus");
        if (command == null) {
            return false;
        }

        command.setExecutor(this);
        registerSubCommands();
        return true;
    }

    @Override
    public boolean onCommand(
            final @NotNull CommandSender sender,
            final @NotNull Command command,
            final @NotNull String label,
            final @NotNull String[] args
    ) {
        final List<String> arguments = Arrays.asList(args);

        if (arguments.isEmpty()) {
            plugin.sms(sender, Messages.PLUGIN_VERSION.message().replaceText(VERSION_REPLACER_BUILDER.replacement(plugin.getDescription().getVersion()).build()).replaceText(AUTHORS_REPLACER_BUILDER.replacement(plugin.getDescription().getAuthors().stream().map(author -> text(author, NamedTextColor.WHITE)).collect(Component.toComponent(text(", ", NamedTextColor.GRAY)))).build()));
            return true;
        }

        final SubCommand subCommand = subCommands.get(arguments.get(0).toLowerCase());

        if (subCommand != null) {
            subCommand.execute(sender, arguments.subList(1, arguments.size()));
            return true;
        }

        plugin.sms(sender, Messages.WRONG_USAGE);
        return true;
    }

    @Override
    public List<String> onTabComplete(
            final @NotNull CommandSender sender,
            final @NotNull Command command,
            final @NotNull String label,
            final @NotNull String[] args
    ) {
        final List<String> arguments = Arrays.asList(args);

        return subCommands.values()
                .stream()
                .map(sc -> sc.onTabComplete(sender, arguments))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void registerSubCommands() {
        final List<SubCommand> commands = List.of(
                new DumpCommand(plugin),
                new ExecuteCommand(plugin),
                new HelpCommand(plugin),
                new ListCommand(plugin),
                new MetaCommand(plugin),
                new OpenCommand(plugin),
                new RefreshCommand(plugin),
                new ReloadCommand(plugin)
        );

        for (final SubCommand subCommand : commands) {
            subCommands.put(subCommand.getName(), subCommand);
        }
    }
}
