package com.extendedclip.deluxemenus.command.subcommand;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.utils.Messages;
import com.extendedclip.deluxemenus.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
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

        String placeholderPlayer = null;
        List<String> actualArgs = new ArrayList<>();
        for (String arg : arguments) {
            if (arg.startsWith("-p:")) {
                if (!sender.hasPermission("deluxemenus.placeholdersfor")) {
                    plugin.sms(sender, Messages.NO_PERMISSION_PLAYER_ARGUMENT);
                    return;
                }
                placeholderPlayer = arg.substring(3);
            } else {
                actualArgs.add(arg);
            }
        }

        String menuName = actualArgs.get(0);
        Optional<Menu> menu = Menu.getMenuByName(menuName);

        if (menu.isEmpty()) {
            plugin.sms(sender, Messages.INVALID_MENU.message().replaceText(MENU_REPLACER_BUILDER.replacement(menuName).build()));
            return;
        }

        String targetPlayerName = actualArgs.size() > 1 ? actualArgs.get(1) : null;
        Player viewer;
        boolean isPlayer = (sender instanceof Player);
        if (targetPlayerName != null) {
            if (isPlayer && !sender.hasPermission("deluxemenus.open.others")) {
                plugin.sms(sender, Messages.NO_PERMISSION);
                return;
            }
            viewer = Bukkit.getPlayerExact(targetPlayerName);
            if (viewer == null) {
                plugin.sms(sender, Messages.PLAYER_IS_NOT_ONLINE.message().replaceText(PLAYER_REPLACER_BUILDER.replacement(targetPlayerName).build()));
                return;
            }
        } else {
            if (!isPlayer) {
                plugin.sms(sender, Messages.MUST_SPECIFY_PLAYER);
                return;
            }
            viewer = (Player) sender;
        }

        Player placeholder;
        if (placeholderPlayer != null) {
            placeholder = Bukkit.getPlayerExact(placeholderPlayer);
            if (placeholder == null) {
                plugin.sms(sender, Messages.PLAYER_IS_NOT_ONLINE.message().replaceText(PLAYER_REPLACER_BUILDER.replacement(placeholderPlayer).build()));
                return;
            }
            if (placeholder.hasPermission("deluxemenus.placeholdersfor.exempt")) {
                plugin.sms(sender, Messages.PLAYER_IS_EXEMPT.message().replaceText(PLAYER_REPLACER_BUILDER.replacement(placeholderPlayer).build()));
                return;
            }
        }else {
            if (isPlayer && !sender.hasPermission("deluxemenus.open.others")) {
                plugin.sms(sender, Messages.NO_PERMISSION);
                return;
            }
            placeholder = viewer;
        }

        List<String> menuArgs = actualArgs.size() > 2 ? actualArgs.subList(2, actualArgs.size()) : new ArrayList<>();
        Map<String, String> menuArgumentNames = menu.get().options().arguments();
        if (menuArgumentNames.isEmpty()) {
            return;
        }

        Map<String, String> argumentsMap = new HashMap<>();
        List<String> argumentNamesList = new ArrayList<>(menuArgumentNames.keySet());
        for (int index = 0; index < argumentNamesList.size(); index++) {
            String argumentName = argumentNamesList.get(index);
            String valueToPut = null;
            if (index < menuArgs.size()) {
                if (index == argumentNamesList.size() - 1) {
                    valueToPut = String.join(" ", menuArgs.subList(index, menuArgs.size()));
                    argumentsMap.put(argumentName, valueToPut);
                    break;
                }
                valueToPut = menuArgs.get(index);
            } else {
                String defaultValue = menuArgumentNames.get(argumentName);
                if (defaultValue != null) {
                    valueToPut = StringUtils.replacePlaceholders(defaultValue, placeholder);
                }
            }
            if (valueToPut != null) {
                argumentsMap.put(argumentName, valueToPut);
            }
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

        if (arguments.size() > 4) {
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

        if (arguments.size() == 3) {
            final String thirdArgument = arguments.get(2).toLowerCase();

            if (thirdArgument.isEmpty()) {
                return Stream.concat(onlinePlayerNames.stream(), Stream.of("-p:")).collect(Collectors.toList());
            }

            if (!thirdArgument.startsWith("-")) {
                return onlinePlayerNames.stream()
                        .filter(playerName -> playerName.toLowerCase().startsWith(thirdArgument))
                        .collect(Collectors.toList());
            }

            return onlinePlayerNames.stream()
                    .map(playerName -> "-p:" + playerName)
                    .filter(playerName -> playerName.toLowerCase().startsWith(thirdArgument))
                    .collect(Collectors.toList());
        }

        if (arguments.size() == 4) {
            final String thirdArgument = arguments.get(2).toLowerCase();
            final String fourthArgument = arguments.get(3).toLowerCase();

            if (!thirdArgument.startsWith("-p:")) {
                return null;
            }

            if (fourthArgument.isEmpty()) {
                return onlinePlayerNames;
            }

            return onlinePlayerNames.stream()
                    .filter(playerName -> playerName.toLowerCase().startsWith(fourthArgument))
                    .collect(Collectors.toList());
        }

        return null;
    }
}
