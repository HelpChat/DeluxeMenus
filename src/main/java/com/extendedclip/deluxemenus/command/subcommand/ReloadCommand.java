package com.extendedclip.deluxemenus.command.subcommand;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.utils.Messages;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ReloadCommand extends SubCommand {

    private static final String RELOAD_PERMISSION = "deluxemenus.reload";

    public ReloadCommand(final @NotNull DeluxeMenus plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getName() {
        return "reload";
    }

    @Override
    public void execute(final @NotNull CommandSender sender, final @NotNull List<String> arguments) {
        if (!sender.hasPermission(RELOAD_PERMISSION)) {
            plugin.sms(sender, Messages.NO_PERMISSION);
            return;
        }

        if (plugin.getConfiguration().checkConfig(null, "config.yml", false) == null) {
            plugin.sms(sender, Messages.RELOAD_FAIL);
            return;
        }

        if (!arguments.isEmpty()) {
            if (Menu.getMenuByName(arguments.get(0)).isEmpty()) {
                plugin.sms(sender, Messages.INVALID_MENU.message().replaceText(MENU_REPLACER_BUILDER.replacement(arguments.get(0)).build()));
                return;
            }

            Menu.unload(plugin, arguments.get(0));

            if (plugin.getConfiguration().loadGUIMenu(arguments.get(0))) {
                plugin.sms(sender, Messages.MENU_RELOADED.message().replaceText(MENU_REPLACER_BUILDER.replacement(arguments.get(0)).build()));
                return;
            }

            plugin.sms(sender, Messages.MENU_NOT_RELOADED.message().replaceText(MENU_REPLACER_BUILDER.replacement(arguments.get(0)).build()));
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

    @Override
    public @Nullable List<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull List<String> arguments) {
        if (!sender.hasPermission(RELOAD_PERMISSION)) {
            return null;
        }

        if (arguments.isEmpty()) {
            return List.of(getName());
        }

        if (arguments.size() > 2) {
            return null;
        }

        if (arguments.size() == 1) {
            if (arguments.get(0).isEmpty()) {
                return List.of(getName());
            }

            final String firstArgument = arguments.get(0).toLowerCase();

            if (getName().startsWith(firstArgument)) {
                return List.of(getName());
            }

            return null;
        }

        final String firstArgument = arguments.get(0).toLowerCase();

        if (!getName().equals(firstArgument)) {
            return null;
        }

        final Collection<String> menuNames = Menu.getAllMenuNames();

        if (menuNames.isEmpty()) {
            return null;
        }

        final String secondArgument = arguments.get(1).toLowerCase();

        if (secondArgument.isEmpty()) {
            return List.copyOf(menuNames);
        }

        return menuNames.stream()
                .filter(menuName -> menuName.toLowerCase().startsWith(secondArgument))
                .collect(Collectors.toList());
    }
}
