package com.extendedclip.deluxemenus.command.subcommand;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class OpenCommand extends SubCommand {

    public OpenCommand(final @NotNull DeluxeMenus plugin) {
        super(plugin);
    }

    @Override
    public void execute(final @NotNull CommandSender sender, final @NotNull List<String> args) {
        if (!sender.hasPermission("deluxemenus.open")) {
            plugin.sms(sender, Messages.NO_PERMISSION);
            return;
        }

        boolean player = (sender instanceof Player);

        if (args.isEmpty()) {
            plugin.sms(sender, Messages.WRONG_USAGE_OPEN_COMMAND);
            return;
        }

        if (Menu.getAllMenus().isEmpty()) {
            plugin.sms(sender, Messages.MENUS_LOADED.message().replaceText(AMOUNT_REPLACER_BUILDER.replacement("There are no").build()));
            return;
        }

        Player viewer;
        String placeholderPlayer = null;

        if (args.size() == 2 && args.get(1).startsWith("-p:")) {
            if (!sender.hasPermission("deluxemenus.placeholdersfor")) {
                plugin.sms(sender, Messages.NO_PERMISSION_PLAYER_ARGUMENT);
                return;
            }

            placeholderPlayer = args.get(1).replace("-p:", "");

        } else if (args.size() >= 3 && args.get(2).startsWith("-p:")) {
            if (!sender.hasPermission("deluxemenus.placeholdersfor")) {
                plugin.sms(sender, Messages.NO_PERMISSION_PLAYER_ARGUMENT);
                return;
            }

            placeholderPlayer = args.get(2).replace("-p:", "");
        }

        if (args.size() >= 2) {
            if (placeholderPlayer == null) {
                if (player && !sender.hasPermission("deluxemenus.open.others")) {
                    plugin.sms(sender, Messages.NO_PERMISSION);
                    return;
                }

                viewer = Bukkit.getPlayerExact(args.get(1));

            } else {
                if (args.size() >= 3) {
                    if (!sender.hasPermission("deluxemenus.open.others")) {
                        plugin.sms(sender, Messages.NO_PERMISSION);
                        return;
                    }

                    viewer = Bukkit.getPlayerExact(args.get(1));

                } else {
                    if (!player) {
                        plugin.sms(sender, Messages.MUST_SPECIFY_PLAYER);
                        return;
                    }

                    viewer = (Player) sender;
                }
            }

        } else {
            if (!player) {
                plugin.sms(sender, Messages.MUST_SPECIFY_PLAYER);
                return;
            }

            viewer = (Player) sender;
        }

        if (viewer == null) {
            plugin.sms(sender, Messages.PLAYER_IS_NOT_ONLINE.message().replaceText(PLAYER_REPLACER_BUILDER.replacement(args.get(1)).build()));
            return;
        }

        Player placeholder = null;

        if (placeholderPlayer != null) {
            placeholder = Bukkit.getPlayerExact(placeholderPlayer);

            if (placeholder == null) {
                plugin.sms(sender, Messages.PLAYER_IS_NOT_ONLINE.message().replaceText(PLAYER_REPLACER_BUILDER.replacement(placeholderPlayer).build()));
                return;

            } else {
                if (placeholder.hasPermission("deluxemenus.placeholdersfor.exempt")) {
                    plugin.sms(sender, Messages.PLAYER_IS_EXEMPT.message().replaceText(PLAYER_REPLACER_BUILDER.replacement(placeholderPlayer).build()));

                    return;
                }
            }
        }

        Optional<Menu> menu = Menu.getMenuByName(args.get(0));

        if (menu.isEmpty()) {
            plugin.sms(sender, Messages.INVALID_MENU.message().replaceText(MENU_REPLACER_BUILDER.replacement(args.get(0)).build()));
            return;
        }

        menu.get().openMenu(viewer, null, placeholder);
    }
}
