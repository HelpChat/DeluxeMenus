package com.extendedclip.deluxemenus.command.subcommand;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.utils.Messages;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HelpCommand extends SubCommand {

    public HelpCommand(final @NotNull DeluxeMenus plugin) {
        super(plugin);
    }

    @Override
    public void execute(final @NotNull CommandSender sender, final @NotNull List<String> args) {
        if (sender.hasPermission("deluxemenus.admin")) {
            plugin.sms(sender, Messages.HELP_ADMIN);
            return;
        }

        plugin.sms(sender, Messages.HELP);
    }
}
