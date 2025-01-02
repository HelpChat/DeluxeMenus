package com.extendedclip.deluxemenus.command.subcommand;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.utils.Messages;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReloadCommand extends SubCommand {

    public ReloadCommand(final @NotNull DeluxeMenus plugin) {
        super(plugin);
    }

    @Override
    public void execute(final @NotNull CommandSender sender, final @NotNull List<String> args) {
        if (!sender.hasPermission("deluxemenus.reload")) {
            plugin.sms(sender, Messages.NO_PERMISSION);
            return;
        }

        if (plugin.getConfiguration().checkConfig(null, "config.yml", false) == null) {
            plugin.sms(sender, Messages.RELOAD_FAIL);
            return;
        }

        if (!args.isEmpty()) {
            if (Menu.getMenuByName(args.get(0)).isEmpty()) {
                plugin.sms(sender, Messages.INVALID_MENU.message().replaceText(MENU_REPLACER_BUILDER.replacement(args.get(0)).build()));
                return;
            }

            Menu.unload(plugin, args.get(0));

            if (plugin.getConfiguration().loadGUIMenu(args.get(0))) {
                plugin.sms(sender, Messages.MENU_RELOADED.message().replaceText(MENU_REPLACER_BUILDER.replacement(args.get(0)).build()));
                return;
            }

            plugin.sms(sender, Messages.MENU_NOT_RELOADED.message().replaceText(MENU_REPLACER_BUILDER.replacement(args.get(0)).build()));
            return;

        }

        plugin.clearCaches();
        plugin.reloadConfig();
        plugin.saveConfig();
        plugin.reload();
        Menu.unload(plugin);
        plugin.getConfiguration().loadGUIMenus();
        plugin.sms(sender, Messages.RELOAD_SUCCESS);

        int gLoaded = Menu.getLoadedMenuSize();

        if (gLoaded == 1) {
            plugin.sms(sender, Messages.MENU_LOADED.message().replaceText(AMOUNT_REPLACER_BUILDER.replacement(String.valueOf(gLoaded)).build()));
            return;
        }

        plugin.sms(sender, Messages.MENUS_LOADED.message().replaceText(AMOUNT_REPLACER_BUILDER.replacement(String.valueOf(gLoaded)).build()));
    }
}
