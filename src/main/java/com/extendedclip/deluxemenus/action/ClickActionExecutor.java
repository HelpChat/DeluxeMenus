package com.extendedclip.deluxemenus.action;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.menu.MenuHolder;
import com.extendedclip.deluxemenus.persistentmeta.PersistentMetaHandler;
import com.extendedclip.deluxemenus.utils.AdventureUtils;
import com.extendedclip.deluxemenus.utils.DebugLevel;
import com.extendedclip.deluxemenus.utils.ExpUtils;
import com.extendedclip.deluxemenus.utils.SoundUtils;
import com.extendedclip.deluxemenus.utils.StringUtils;
import com.extendedclip.deluxemenus.utils.VersionHelper;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

final class ClickActionExecutor {

    private final DeluxeMenus plugin;
    private final Player player;
    private final Optional<MenuHolder> holder;
    private final ActionType actionType;
    private final String executable;

    ClickActionExecutor(
            @NotNull final DeluxeMenus plugin,
            @NotNull final Player player,
            @NotNull final Optional<MenuHolder> holder,
            @NotNull final ActionType actionType,
            @NotNull final String executable
    ) {
        this.plugin = plugin;
        this.player = player;
        this.holder = holder;
        this.actionType = actionType;
        this.executable = executable;
    }

    void execute() {
        switch (actionType) {
            case META:
                executeMetaAction();
                break;
            case PLAYER:
            case PLAYER_COMMAND_EVENT:
                player.chat("/" + executable);
                break;
            case PLACEHOLDER:
                holder.ifPresent(value -> value.setPlaceholders(executable));
                break;
            case CHAT:
                player.chat(executable);
                break;
            case CONSOLE:
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), executable);
                break;
            case MINI_MESSAGE:
                plugin.audiences().player(player).sendMessage(MiniMessage.miniMessage().deserialize(executable));
                break;
            case MINI_BROADCAST:
                plugin.audiences().all().sendMessage(MiniMessage.miniMessage().deserialize(executable));
                break;
            case MESSAGE:
                player.sendMessage(StringUtils.color(executable));
                break;
            case LOG:
                executeLogAction();
                break;
            case BROADCAST:
                Bukkit.broadcastMessage(StringUtils.color(executable));
                break;
            case CLOSE:
                Menu.closeMenu(plugin, player, true, true);
                break;
            case OPEN_GUI_MENU:
            case OPEN_MENU:
                executeOpenMenuAction();
                break;
            case CONNECT:
                plugin.connect(player, executable);
                break;
            case JSON_MESSAGE:
                AdventureUtils.sendJson(plugin, player, executable);
                break;
            case JSON_BROADCAST:
            case BROADCAST_JSON:
                plugin.audiences().all().sendMessage(AdventureUtils.fromJson(executable));
                break;
            case REFRESH:
                executeRefreshAction();
                break;
            case TAKE_MONEY:
                executeMoneyAction(false);
                break;
            case GIVE_MONEY:
                executeMoneyAction(true);
                break;
            case TAKE_EXP:
            case GIVE_EXP:
                executeExperienceAction();
                break;
            case GIVE_PERM:
                executePermissionAction(true);
                break;
            case TAKE_PERM:
                executePermissionAction(false);
                break;
            case BROADCAST_SOUND:
            case BROADCAST_RAW_SOUND:
            case BROADCAST_WORLD_SOUND:
            case BROADCAST_WORLD_RAW_SOUND:
            case PLAY_RAW_SOUND:
            case PLAY_SOUND:
                executeSoundAction();
                break;
            default:
                break;
        }
    }

    private void executeMetaAction() {
        if (!VersionHelper.IS_PDC_VERSION || plugin.getPersistentMetaHandler() == null) {
            plugin.debug(DebugLevel.HIGHEST, Level.INFO, "Meta action not supported on this server version.");
            return;
        }

        final PersistentMetaHandler.OperationResult result = plugin.getPersistentMetaHandler().parseAndExecuteMetaActionFromString(player, executable);
        switch (result) {
            case INVALID_SYNTAX:
                plugin.debug(DebugLevel.HIGHEST, Level.INFO, "Invalid meta action! Make sure you have the right syntax.");
                break;
            case NEW_VALUE_IS_DIFFERENT_TYPE:
                plugin.debug(DebugLevel.HIGHEST, Level.INFO, "Invalid meta action! New value is a different type than the old value!");
                break;
            case INVALID_TYPE:
                plugin.debug(DebugLevel.HIGHEST, Level.INFO, "Invalid meta action! The specified type is not supported for the specified action!");
                break;
            case EXISTENT_VALUE_IS_DIFFERENT_TYPE:
                plugin.debug(DebugLevel.HIGHEST, Level.INFO, "Invalid meta action! Existent value is a different type than the new value!");
                break;
            case VALUE_NOT_FOUND:
            case SUCCESS:
            default:
                break;
        }
    }

    private void executeLogAction() {
        final String[] logParts = executable.split(" ", 2);

        if (logParts.length == 0 || logParts[0].isBlank()) {
            plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "LOG command requires at least a message");
            return;
        }

        Level logLevel;
        String message;

        if (logParts.length == 1) {
            logLevel = Level.INFO;
            message = logParts[0];
        } else {
            message = logParts[1];

            try {
                logLevel = Level.parse(logParts[0].toUpperCase());
            } catch (IllegalArgumentException exception) {
                logLevel = Level.INFO;
                plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Log level " + logParts[0] + " is not a valid log level! Using INFO instead.");
            }
        }

        plugin.getLogger().log(logLevel, String.format("[%s]: %s", holder.map(MenuHolder::getMenuName).orElse("Unknown Menu"), message));
    }

    private void executeOpenMenuAction() {
        final String temporaryExecutable = executable.replaceAll("\\s+", " ").replace("  ", " ");
        final String[] executableParts = temporaryExecutable.split(" ", 2);

        if (executableParts.length == 0) {
            plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Could not find and open menu " + executable);
            return;
        }

        final String menuName = executableParts[0];
        final Optional<Menu> optionalMenuToOpen = Menu.getMenuByName(menuName);

        if (optionalMenuToOpen.isEmpty()) {
            plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Could not find and open menu " + executable);
            return;
        }

        final Menu menuToOpen = optionalMenuToOpen.get();
        final List<String> menuArgumentNames = menuToOpen.options().arguments();

        String[] passedArgumentValues = null;
        if (executableParts.length > 1) {
            passedArgumentValues = executableParts[1].split(" ");
        }

        if (menuArgumentNames.isEmpty()) {
            if (passedArgumentValues != null && passedArgumentValues.length > 0) {
                plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Arguments were given for menu " + menuName + " in action [openguimenu] or [openmenu], but the menu does not support arguments!"
                );
            }

            openMenuWithCurrentArguments(menuToOpen);
            return;
        }

        if (passedArgumentValues == null || passedArgumentValues.length == 0) {
            openMenuWithCurrentArguments(menuToOpen);
            return;
        }

        if (passedArgumentValues.length < menuArgumentNames.size()) {
            plugin.debug(
                    DebugLevel.HIGHEST,
                    Level.WARNING,
                    "Not enough arguments given for menu " + menuName + " when opening using the [openguimenu] or [openmenu] action!"
            );
            return;
        }

        final Map<String, String> argumentsMap = new HashMap<>();
        if (holder.isPresent() && holder.get().getTypedArgs() != null) {
            argumentsMap.putAll(holder.get().getTypedArgs());
        }

        for (int index = 0; index < menuArgumentNames.size(); index++) {
            final String argumentName = menuArgumentNames.get(index);

            if (passedArgumentValues.length <= index) {
                plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Not enough arguments given for menu " + menuName + " when opening using the [openguimenu] or [openmenu] action!"
                );
                break;
            }

            if (menuArgumentNames.size() == index + 1) {
                final String lastArgumentValue = String.join(" ", Arrays.asList(passedArgumentValues).subList(index, passedArgumentValues.length));
                argumentsMap.put(argumentName, lastArgumentValue);
                break;
            }

            argumentsMap.put(argumentName, passedArgumentValues[index]);
        }

        if (holder.isEmpty()) {
            menuToOpen.openMenu(player, argumentsMap, null);
            return;
        }

        menuToOpen.openMenu(player, argumentsMap, holder.get().getPlaceholderPlayer());
    }

    private void openMenuWithCurrentArguments(@NotNull final Menu menuToOpen) {
        if (holder.isEmpty()) {
            menuToOpen.openMenu(player);
            return;
        }

        menuToOpen.openMenu(player, holder.get().getTypedArgs(), holder.get().getPlaceholderPlayer());
    }

    private void executeRefreshAction() {
        if (holder.isEmpty()) {
            plugin.debug(
                    DebugLevel.MEDIUM,
                    Level.WARNING,
                    player.getName() + " does not have menu open! Nothing to refresh!"
            );
            return;
        }

        holder.get().refreshMenu();
    }

    private void executeMoneyAction(final boolean give) {
        if (plugin.getVault() == null || !plugin.getVault().hooked()) {
            plugin.debug(DebugLevel.HIGHEST, Level.WARNING, give ? "Vault not hooked! Cannot give money!" : "Vault not hooked! Cannot take money!");
            return;
        }

        try {
            final double amount = Double.parseDouble(executable);
            if (give) {
                plugin.getVault().giveMoney(player, amount);
                return;
            }

            plugin.getVault().takeMoney(player, amount);
        } catch (final NumberFormatException exception) {
            plugin.debug(
                    DebugLevel.HIGHEST,
                    Level.WARNING,
                    give
                            ? "Amount for give money action: " + executable + ", is not a valid number!"
                            : "Amount for take money action: " + executable + ", is not a valid number!"
            );
        }
    }

    private void executeExperienceAction() {
        final String lowerCaseExecutable = executable.toLowerCase();

        try {
            if (Integer.parseInt(lowerCaseExecutable.replaceAll("l", "")) <= 0) {
                return;
            }

            if (actionType == ActionType.TAKE_EXP) {
                ExpUtils.setExp(player, "-" + lowerCaseExecutable);
                return;
            }

            ExpUtils.setExp(player, lowerCaseExecutable);
        } catch (final NumberFormatException exception) {
            if (actionType == ActionType.TAKE_EXP) {
                plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Amount for take exp action: " + executable + ", is not a valid number!"
                );
                return;
            }

            plugin.debug(
                    DebugLevel.HIGHEST,
                    Level.WARNING,
                    "Amount for give exp action: " + executable + ", is not a valid number!"
            );
        }
    }

    private void executePermissionAction(final boolean give) {
        if (plugin.getVault() == null || !plugin.getVault().hooked()) {
            plugin.debug(
                    DebugLevel.HIGHEST,
                    Level.WARNING,
                    give
                            ? "Vault not hooked! Cannot give permission: " + executable + "!"
                            : "Vault not hooked! Cannot take permission: " + executable + "!"
            );
            return;
        }

        if (give) {
            plugin.getVault().givePermission(player, executable);
            return;
        }

        plugin.getVault().takePermission(player, executable);
    }

    private void executeSoundAction() {
        final boolean raw = isRaw(actionType);
        Sound sound = null;
        String soundName = executable;
        float volume = 1;
        float pitch = 1;

        if (!executable.contains(" ")) {
            try {
                sound = SoundUtils.getSound(executable.toUpperCase());
            } catch (final IllegalArgumentException exception) {
                plugin.printStacktrace(
                        "Sound name given for sound action: " + executable + ", is not a valid sound!",
                        exception
                );
                return;
            }
        } else {
            final String[] parts = executable.split(" ", 3);

            try {
                sound = SoundUtils.getSound(parts[0].toUpperCase());
            } catch (final IllegalArgumentException exception) {
                plugin.printStacktrace(
                        "Sound name given for sound action: " + parts[0] + ", is not a valid sound!",
                        exception
                );
                return;
            }

            if (parts.length == 3) {
                try {
                    pitch = Float.parseFloat(parts[2]);
                } catch (final NumberFormatException exception) {
                    plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Pitch given for sound action: " + parts[2] + ", is not a valid number!"
                    );

                    plugin.printStacktrace(
                            "Pitch given for sound action: " + parts[2] + ", is not a valid number!",
                            exception
                    );
                }
            }

            try {
                volume = Float.parseFloat(parts[1]);
            } catch (final NumberFormatException exception) {
                plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Volume given for sound action: " + parts[1] + ", is not a valid number!"
                );

                plugin.printStacktrace(
                        "Volume given for sound action: " + parts[1] + ", is not a valid number!",
                        exception
                );
            }
        }

        switch (actionType) {
            case BROADCAST_WORLD_RAW_SOUND:
                for (final Player broadcastTarget : player.getWorld().getPlayers()) {
                    broadcastTarget.playSound(broadcastTarget.getLocation(), soundName, volume, pitch);
                }
                break;
            case BROADCAST_RAW_SOUND:
                for (final Player broadcastTarget : Bukkit.getOnlinePlayers()) {
                    broadcastTarget.playSound(broadcastTarget.getLocation(), soundName, volume, pitch);
                }
                break;
            case PLAY_RAW_SOUND:
                player.playSound(player.getLocation(), soundName, volume, pitch);
                break;
            case BROADCAST_SOUND:
                for (final Player broadcastTarget : Bukkit.getOnlinePlayers()) {
                    broadcastTarget.playSound(broadcastTarget.getLocation(), sound, volume, pitch);
                }
                break;
            case BROADCAST_WORLD_SOUND:
                for (final Player broadcastTarget : player.getWorld().getPlayers()) {
                    broadcastTarget.playSound(broadcastTarget.getLocation(), sound, volume, pitch);
                }
                break;
            case PLAY_SOUND:
                player.playSound(player.getLocation(), sound, volume, pitch);
                break;
            default:
                break;
        }
    }

    private boolean isRaw(final ActionType actionType) {
        return actionType == ActionType.PLAY_RAW_SOUND || actionType == ActionType.BROADCAST_RAW_SOUND || actionType == ActionType.BROADCAST_WORLD_RAW_SOUND;
    }
}
