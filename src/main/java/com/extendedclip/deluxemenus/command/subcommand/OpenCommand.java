package com.extendedclip.deluxemenus.command.subcommand;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.utils.DebugLevel;
import com.extendedclip.deluxemenus.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OpenCommand extends SubCommand {

    private static final String OPEN_COMMAND = "deluxemenus.open";
    private static final String ARGS_MARKER = "-args:";
    private static final String PLACEHOLDER_FLAG = "-p:";

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

        boolean player = (sender instanceof Player);

        if (arguments.isEmpty()) {
            plugin.sms(sender, Messages.WRONG_USAGE_OPEN_COMMAND);
            return;
        }

        if (Menu.getAllMenus().isEmpty()) {
            plugin.sms(sender, Messages.MENUS_LOADED.message().replaceText(AMOUNT_REPLACER_BUILDER.replacement("There are no").build()));
            return;
        }

        final String menuName = arguments.get(0);

        String viewerName = null;
        String placeholderPlayerName = null;
        List<String> rawArgumentValues = null;

        int index = 1;

        if (index < arguments.size()
                && !arguments.get(index).startsWith(PLACEHOLDER_FLAG)
                && !arguments.get(index).equals(ARGS_MARKER)) {
            viewerName = arguments.get(index);
            index++;
        }

        if (index < arguments.size() && arguments.get(index).startsWith(PLACEHOLDER_FLAG)) {
            if (!sender.hasPermission("deluxemenus.placeholdersfor")) {
                plugin.sms(sender, Messages.NO_PERMISSION_PLAYER_ARGUMENT);
                return;
            }

            placeholderPlayerName = arguments.get(index).substring(PLACEHOLDER_FLAG.length());
            index++;
        }

        if (index < arguments.size() && arguments.get(index).equals(ARGS_MARKER)) {
            index++;
            rawArgumentValues = index < arguments.size()
                    ? arguments.subList(index, arguments.size())
                    : List.of();
        }

        Player viewer;

        if (viewerName != null) {
            if (player && !sender.hasPermission("deluxemenus.open.others")) {
                plugin.sms(sender, Messages.NO_PERMISSION);
                return;
            }

            viewer = Bukkit.getPlayerExact(viewerName);

            if (viewer == null) {
                plugin.sms(sender, Messages.PLAYER_IS_NOT_ONLINE.message().replaceText(PLAYER_REPLACER_BUILDER.replacement(viewerName).build()));
                return;
            }

        } else {
            if (!player) {
                plugin.sms(sender, Messages.MUST_SPECIFY_PLAYER);
                return;
            }

            viewer = (Player) sender;
        }

        Player placeholder = null;

        if (placeholderPlayerName != null) {
            placeholder = Bukkit.getPlayerExact(placeholderPlayerName);

            if (placeholder == null) {
                plugin.sms(sender, Messages.PLAYER_IS_NOT_ONLINE.message().replaceText(PLAYER_REPLACER_BUILDER.replacement(placeholderPlayerName).build()));
                return;

            } else if (placeholder.hasPermission("deluxemenus.placeholdersfor.exempt")) {
                plugin.sms(sender, Messages.PLAYER_IS_EXEMPT.message().replaceText(PLAYER_REPLACER_BUILDER.replacement(placeholderPlayerName).build()));
                return;
            }
        }

        Optional<Menu> menu = Menu.getMenuByName(menuName);

        if (menu.isEmpty()) {
            plugin.sms(sender, Messages.INVALID_MENU.message().replaceText(MENU_REPLACER_BUILDER.replacement(menuName).build()));
            return;
        }

        final Menu menuToOpen = menu.get();
        final List<String> menuArgumentNames = menuToOpen.options().arguments();

        Map<String, String> argumentsMap = null;

        if (menuArgumentNames.isEmpty()) {
            if (rawArgumentValues != null && !rawArgumentValues.isEmpty()) {
                plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Arguments were given for menu " + menuName + " via /dm open, but the menu does not support arguments!"
                );
            }

        } else if (rawArgumentValues != null && !rawArgumentValues.isEmpty()) {
            if (rawArgumentValues.size() < menuArgumentNames.size()) {
                plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Not enough arguments given for menu " + menuName + " when opening using /dm open!"
                );
                plugin.sms(sender, Messages.WRONG_USAGE_OPEN_COMMAND);
                return;
            }

            argumentsMap = new HashMap<>();

            for (int argIndex = 0; argIndex < menuArgumentNames.size(); argIndex++) {
                final String argumentName = menuArgumentNames.get(argIndex);

                if (menuArgumentNames.size() == argIndex + 1) {
                    final String lastArgumentValue = String.join(
                            " ",
                            rawArgumentValues.subList(argIndex, rawArgumentValues.size())
                    );
                    argumentsMap.put(argumentName, lastArgumentValue);
                    break;
                }

                argumentsMap.put(argumentName, rawArgumentValues.get(argIndex));
            }
        }

        menuToOpen.openMenu(viewer, argumentsMap, placeholder);
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

        boolean viewerConsumed = false;
        boolean placeholderConsumed = false;
        boolean argsMarkerConsumed = false;

        for (int i = 2; i < arguments.size() - 1; i++) {
            final String current = arguments.get(i);

            if (argsMarkerConsumed) {
                continue;
            }

            if (!viewerConsumed && !placeholderConsumed
                    && !current.startsWith(PLACEHOLDER_FLAG)
                    && !current.equals(ARGS_MARKER)) {
                viewerConsumed = true;
            } else if (!placeholderConsumed && current.startsWith(PLACEHOLDER_FLAG)) {
                placeholderConsumed = true;
            } else if (current.equals(ARGS_MARKER)) {
                argsMarkerConsumed = true;
            }
        }

        if (argsMarkerConsumed) {
            return null;
        }

        final String lastArgument = arguments.get(arguments.size() - 1);
        final String lastArgumentLower = lastArgument.toLowerCase();

        if (!viewerConsumed && !placeholderConsumed) {
            // Still in the viewer slot: suggest online players, -p:, or -args:
            if (lastArgumentLower.isEmpty()) {
                return Stream.concat(onlinePlayerNames.stream(), Stream.of(PLACEHOLDER_FLAG, ARGS_MARKER))
                        .collect(Collectors.toList());
            }

            if (lastArgumentLower.startsWith("-")) {
                return Stream.of(PLACEHOLDER_FLAG, ARGS_MARKER)
                        .filter(option -> option.startsWith(lastArgumentLower))
                        .collect(Collectors.toList());
            }

            return onlinePlayerNames.stream()
                    .filter(playerName -> playerName.toLowerCase().startsWith(lastArgumentLower))
                    .collect(Collectors.toList());
        }

        if (viewerConsumed && !placeholderConsumed) {
            // Viewer already given: suggest -p:<target> or -args:
            if (lastArgumentLower.isEmpty()) {
                return List.of(PLACEHOLDER_FLAG, ARGS_MARKER);
            }

            if (lastArgumentLower.startsWith(PLACEHOLDER_FLAG)) {
                return onlinePlayerNames.stream()
                        .map(playerName -> PLACEHOLDER_FLAG + playerName)
                        .filter(option -> option.toLowerCase().startsWith(lastArgumentLower))
                        .collect(Collectors.toList());
            }

            return Stream.of(PLACEHOLDER_FLAG, ARGS_MARKER)
                    .filter(option -> option.startsWith(lastArgumentLower))
                    .collect(Collectors.toList());
        }

        if (lastArgumentLower.isEmpty() || ARGS_MARKER.startsWith(lastArgumentLower)) {
            return List.of(ARGS_MARKER);
        }

        return null;
    }
}
