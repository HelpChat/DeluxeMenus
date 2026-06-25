package com.extendedclip.deluxemenus.command.subcommand;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OpenCommand extends SubCommand {

    private static final String OPEN_COMMAND = "deluxemenus.open";

    public OpenCommand(final @NotNull DeluxeMenus plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getName() {
        return "open";
    }

    @Override
    public void execute(final @NotNull CommandSender sender, final @NotNull List<String> arguments) {
        if (!sender.hasPermission(OPEN_COMMAND)) {
            plugin.sms(sender, Messages.NO_PERMISSION);
            return;
        }

        if (arguments.isEmpty()) {
            plugin.sms(sender, Messages.WRONG_USAGE_OPEN_COMMAND);
            return;
        }

        if (Menu.getAllMenus().isEmpty()) {
            plugin.sms(sender, Messages.MENUS_LOADED.message().replaceText(AMOUNT_REPLACER_BUILDER.replacement("There are no").build()));
            return;
        }

        boolean isPlayer = (sender instanceof Player);

        // Strict positional parsing:
        // arguments[0] = <menu>
        // arguments[1] = optional <viewer> OR -p:<target>
        // arguments[2] = optional -p:<target> (if arguments[1] was <viewer>) OR start of [args]
        // remaining    = [args]

        int cursor = 0;

        // [0] <menu> — always required
        String menuName = arguments.get(cursor++);

        // [1] optional: <viewer> or -p:<target>
        String viewerName = null;
        String placeholderPlayerName = null;

        if (cursor < arguments.size()) {
            String next = arguments.get(cursor);
            if (next.startsWith("-p:")) {
                // -p:<target> with no explicit viewer
                if (!sender.hasPermission("deluxemenus.placeholdersfor")) {
                    plugin.sms(sender, Messages.NO_PERMISSION_PLAYER_ARGUMENT);
                    return;
                }
                placeholderPlayerName = next.substring(3);
                cursor++;
            } else {
                // treat as <viewer>
                viewerName = next;
                cursor++;

                // [2] optional: -p:<target>
                if (cursor < arguments.size() && arguments.get(cursor).startsWith("-p:")) {
                    if (!sender.hasPermission("deluxemenus.placeholdersfor")) {
                        plugin.sms(sender, Messages.NO_PERMISSION_PLAYER_ARGUMENT);
                        return;
                    }
                    placeholderPlayerName = arguments.get(cursor).substring(3);
                    cursor++;
                }
            }
        }

        // remaining arguments[cursor..] are menu [args]
        List<String> menuArgs = cursor < arguments.size()
                ? arguments.subList(cursor, arguments.size())
                : Collections.emptyList();

        // Resolve viewer
        Player viewer;
        if (viewerName != null) {
            if (isPlayer && !sender.hasPermission("deluxemenus.open.others")) {
                plugin.sms(sender, Messages.NO_PERMISSION);
                return;
            }
            viewer = Bukkit.getPlayerExact(viewerName);
            if (viewer == null) {
                plugin.sms(sender, Messages.PLAYER_IS_NOT_ONLINE.message().replaceText(PLAYER_REPLACER_BUILDER.replacement(viewerName).build()));
                return;
            }
        } else {
            if (!isPlayer) {
                plugin.sms(sender, Messages.MUST_SPECIFY_PLAYER);
                return;
            }
            viewer = (Player) sender;
        }

        // Resolve placeholder player
        Player placeholder = null;
        if (placeholderPlayerName != null) {
            placeholder = Bukkit.getPlayerExact(placeholderPlayerName);
            if (placeholder == null) {
                plugin.sms(sender, Messages.PLAYER_IS_NOT_ONLINE.message().replaceText(PLAYER_REPLACER_BUILDER.replacement(placeholderPlayerName).build()));
                return;
            }
            if (placeholder.hasPermission("deluxemenus.placeholdersfor.exempt")) {
                plugin.sms(sender, Messages.PLAYER_IS_EXEMPT.message().replaceText(PLAYER_REPLACER_BUILDER.replacement(placeholderPlayerName).build()));
                return;
            }
        }

        // Resolve menu
        Optional<Menu> menu = Menu.getMenuByName(menuName);
        if (menu.isEmpty()) {
            plugin.sms(sender, Messages.INVALID_MENU.message().replaceText(MENU_REPLACER_BUILDER.replacement(menuName).build()));
            return;
        }

        // Build menu arguments map
        List<String> menuArgumentNames = menu.get().options().arguments();
        if (menuArgumentNames.isEmpty()) {
            menu.get().openMenu(viewer, null, placeholder);
            return;
        }

        Map<String, String> argumentsMap = new HashMap<>();
        for (int i = 0; i < menuArgumentNames.size() && i < menuArgs.size(); i++) {
            String argName = menuArgumentNames.get(i);
            if (i == menuArgumentNames.size() - 1) {
                // Last named arg consumes all remaining values
                argumentsMap.put(argName, String.join(" ", menuArgs.subList(i, menuArgs.size())));
                break;
            }
            argumentsMap.put(argName, menuArgs.get(i));
        }

        menu.get().openMenu(viewer, argumentsMap, placeholder);
    }

    @Override
    public @Nullable List<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull List<String> arguments) {
        if (!sender.hasPermission(OPEN_COMMAND)) {
            return null;
        }

        if (arguments.isEmpty()) {
            return List.of(getName());
        }

        if (arguments.size() == 1) {
            final String firstArgument = arguments.get(0).toLowerCase();
            if (firstArgument.isEmpty() || getName().startsWith(firstArgument)) {
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

        // [2] <menu>
        if (arguments.size() == 2) {
            final String secondArgument = arguments.get(1).toLowerCase();
            if (secondArgument.isEmpty()) {
                return List.copyOf(menuNames);
            }
            return menuNames.stream()
                    .filter(menuName -> menuName.toLowerCase().startsWith(secondArgument))
                    .collect(Collectors.toList());
        }

        final List<String> onlinePlayerNames = Bukkit.getOnlinePlayers()
                .stream()
                .map(Player::getName)
                .collect(Collectors.toList());

        // [3] <viewer> or -p:<target>
        if (arguments.size() == 3) {
            final String thirdArgument = arguments.get(2).toLowerCase();
            if (thirdArgument.isEmpty()) {
                return Stream.concat(onlinePlayerNames.stream(), Stream.of("-p:"))
                        .collect(Collectors.toList());
            }
            if (thirdArgument.startsWith("-p:")) {
                return onlinePlayerNames.stream()
                        .map(playerName -> "-p:" + playerName)
                        .filter(suggestion -> suggestion.toLowerCase().startsWith(thirdArgument))
                        .collect(Collectors.toList());
            }
            return onlinePlayerNames.stream()
                    .filter(playerName -> playerName.toLowerCase().startsWith(thirdArgument))
                    .collect(Collectors.toList());
        }

        // [4] -p:<target> (only valid if [3] was a <viewer>, not already a -p:)
        if (arguments.size() == 4) {
            final String thirdArgument = arguments.get(2).toLowerCase();
            final String fourthArgument = arguments.get(3).toLowerCase();

            // If slot 3 was already a -p: flag, slot 4 is a menu arg — no suggestions
            if (thirdArgument.startsWith("-p:")) {
                return null;
            }

            if (fourthArgument.isEmpty()) {
                return Stream.concat(onlinePlayerNames.stream(), Stream.of("-p:"))
                        .collect(Collectors.toList());
            }
            if (fourthArgument.startsWith("-p:")) {
                return onlinePlayerNames.stream()
                        .map(playerName -> "-p:" + playerName)
                        .filter(suggestion -> suggestion.toLowerCase().startsWith(fourthArgument))
                        .collect(Collectors.toList());
            }
            // Otherwise it's a menu [arg] — no tab suggestions
            return null;
        }

        return null;
    }
}
