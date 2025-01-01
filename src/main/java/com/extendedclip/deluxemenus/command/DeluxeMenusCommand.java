package com.extendedclip.deluxemenus.command;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.command.subcommand.DumpCommand;
import com.extendedclip.deluxemenus.command.subcommand.ExecuteCommand;
import com.extendedclip.deluxemenus.command.subcommand.HelpCommand;
import com.extendedclip.deluxemenus.command.subcommand.ListCommand;
import com.extendedclip.deluxemenus.command.subcommand.OpenCommand;
import com.extendedclip.deluxemenus.command.subcommand.ReloadCommand;
import com.extendedclip.deluxemenus.command.subcommand.SubCommand;
import com.extendedclip.deluxemenus.utils.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.kyori.adventure.text.Component.text;

public class DeluxeMenusCommand implements CommandExecutor {

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

    private void registerSubCommands() {
        subCommands.put("dump", new DumpCommand(plugin));
        subCommands.put("execute", new ExecuteCommand(plugin));
        subCommands.put("help", new HelpCommand(plugin));
        subCommands.put("list", new ListCommand(plugin));
        subCommands.put("open", new OpenCommand(plugin));
        subCommands.put("reload", new ReloadCommand(plugin));
    }
}
