package com.extendedclip.deluxemenus.command;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.command.subcommand.*;
import com.extendedclip.deluxemenus.utils.Messages;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.text;

public class DeluxeMenusCommand implements BasicCommand {

    private static final TextReplacementConfig.Builder VERSION_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<version>");
    private static final TextReplacementConfig.Builder AUTHORS_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<authors>");

    private final DeluxeMenus plugin;
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public DeluxeMenusCommand(final @NotNull DeluxeMenus plugin) {
        this.plugin = plugin;
    }

    public boolean register() {
        registerSubCommands();
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register(
                        "deluxemenus",
                        "DeluxeMenus main command",
                        List.of("dm"),
                        this
                )
        );
        return true;
    }

    @Override
    public void execute(
            final @NotNull CommandSourceStack stack,
            final @NotNull String @NonNull [] args
    ) {
        final CommandSender sender = stack.getSender();
        final List<String> arguments = Arrays.asList(args);

        if (arguments.isEmpty() || (arguments.size() == 1 && arguments.getFirst().isEmpty())) {
            plugin.sms(sender, Messages.PLUGIN_VERSION.message().replaceText(VERSION_REPLACER_BUILDER.replacement(plugin.getDescription().getVersion()).build()).replaceText(AUTHORS_REPLACER_BUILDER.replacement(plugin.getDescription().getAuthors().stream().map(author -> text(author, NamedTextColor.WHITE)).collect(Component.toComponent(text(", ", NamedTextColor.GRAY)))).build()));
            return;
        }

        final SubCommand subCommand = subCommands.get(arguments.getFirst().toLowerCase());

        if (subCommand != null) {
            subCommand.execute(sender, arguments.subList(1, arguments.size()));
            return;
        }

        plugin.sms(sender, Messages.WRONG_USAGE);
    }

    @Override
    public @NotNull Collection<String> suggest(
            final @NotNull CommandSourceStack stack,
            final @NotNull String @NonNull [] args
    ) {
        final CommandSender sender = stack.getSender();
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
