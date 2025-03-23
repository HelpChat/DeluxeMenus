package com.extendedclip.deluxemenus.command.subcommand;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.utils.Messages;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HelpCommand extends SubCommand {

    private static final String ADMIN_PERMISSION = "deluxemenus.admin";

    public HelpCommand(final @NotNull DeluxeMenus plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getName() {
        return "help";
    }

    @Override
    public void execute(final @NotNull CommandSender sender, final @NotNull List<String> arguments) {
        if (sender.hasPermission(ADMIN_PERMISSION)) {
            plugin.sms(sender, Messages.HELP_ADMIN);
            return;
        }

        plugin.sms(sender, Messages.HELP);
    }

    @Override
    public @Nullable List<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull List<String> arguments) {
        if (arguments.size() > 1) {
            return null;
        }

        if (arguments.isEmpty() || arguments.get(0).isEmpty()) {
            return List.of(getName());
        }

        final String argument = arguments.get(0).toLowerCase();

        if (!getName().startsWith(argument)) {
            return null;
        }

        return List.of(getName());
    }
}
