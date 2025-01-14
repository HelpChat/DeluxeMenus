package com.extendedclip.deluxemenus.action;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.menu.MenuHolder;
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
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class ClickActionTask extends BukkitRunnable {

    private final DeluxeMenus plugin;
    private final UUID uuid;
    private final ActionType actionType;
    private final String exec;
    // Ugly hack to get around the fact that arguments are not available at task execution time
    private final Map<String, String> arguments;
    private final boolean parsePlaceholdersInArguments;
    private final boolean parsePlaceholdersAfterArguments;

    public ClickActionTask(
            @NotNull final DeluxeMenus plugin,
            @NotNull final UUID uuid,
            @NotNull final ActionType actionType,
            @NotNull final String exec,
            @NotNull final Map<String, String> arguments,
            final boolean parsePlaceholdersInArguments,
            final boolean parsePlaceholdersAfterArguments
    ) {
        this.plugin = plugin;
        this.uuid = uuid;
        this.actionType = actionType;
        this.exec = exec;
        this.arguments = arguments;
        this.parsePlaceholdersInArguments = parsePlaceholdersInArguments;
        this.parsePlaceholdersAfterArguments = parsePlaceholdersAfterArguments;
    }

    @Override
    public void run() {
        final Player player = Bukkit.getPlayer(this.uuid);
        if (player == null) {
            return;
        }

        final Optional<MenuHolder> holder = Menu.getMenuHolder(player);
        final Player target = holder.isPresent() && holder.get().getPlaceholderPlayer() != null
                ? holder.get().getPlaceholderPlayer()
                : player;


        final String executable = StringUtils.replacePlaceholdersAndArguments(
                this.exec,
                this.arguments,
                target,
                this.parsePlaceholdersInArguments,
                this.parsePlaceholdersAfterArguments);

        switch (actionType) {
            case META:
                if (!VersionHelper.IS_PDC_VERSION || plugin.getPersistentMetaHandler() == null) {
                    plugin.debug(DebugLevel.HIGHEST, Level.INFO, "Meta action not supported on this server version.");
                    break;
                }
                try {
                    final boolean result = plugin.getPersistentMetaHandler().setMeta(player, executable);
                    if (!result) {
                        plugin.debug(DebugLevel.HIGHEST, Level.INFO, "Invalid meta action! Make sure you have the right syntax.");
                        break;
                    }
                } catch (final NumberFormatException exception) {
                    plugin.debug(DebugLevel.HIGHEST, Level.INFO, "Invalid integer value for meta action!");
                }
                break;

            case PLAYER:
                player.chat("/" + executable);
                break;

            case PLAYER_COMMAND_EVENT:
                Bukkit.getPluginManager().callEvent(new PlayerCommandPreprocessEvent(player, "/" + executable));
                break;

            case PLACEHOLDER:
                holder.ifPresent(it -> it.setPlaceholders(executable));
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

            case BROADCAST:
                Bukkit.broadcastMessage(StringUtils.color(executable));
                break;

            case CLOSE:
                Menu.closeMenu(plugin, player, true, true);
                break;

            case OPEN_GUI_MENU:
            case OPEN_MENU:
                final String temporaryExecutable = executable.replaceAll("\\s+", " ").replace("  ", " ");
                final String[] executableParts = temporaryExecutable.split(" ", 2);

                if (executableParts.length == 0) {
                    plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Could not find and open menu " + executable);
                    break;
                }

                final String menuName = executableParts[0];

                final Optional<Menu> optionalMenuToOpen = Menu.getMenuByName(menuName);

                if (optionalMenuToOpen.isEmpty()) {
                    plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Could not find and open menu " + executable);
                    break;
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

                    if (holder.isEmpty()) {
                        menuToOpen.openMenu(player);
                        break;
                    }

                    menuToOpen.openMenu(player, holder.get().getTypedArgs(), holder.get().getPlaceholderPlayer());
                    break;
                }

                if (passedArgumentValues == null || passedArgumentValues.length == 0) {
                    // Replicate old behavior: If no arguments are given, open the menu with the arguments from the current menu
                    if (holder.isEmpty()) {
                        menuToOpen.openMenu(player);
                        break;
                    }

                    menuToOpen.openMenu(player, holder.get().getTypedArgs(), holder.get().getPlaceholderPlayer());
                    break;
                }

                if (passedArgumentValues.length < menuArgumentNames.size()) {
                    plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Not enough arguments given for menu " + menuName + " when opening using the [openguimenu] or [openmenu] action!"
                    );
                    break;
                }

                final Map<String, String> argumentsMap = new HashMap<>();
                if (holder.isPresent() && holder.get().getTypedArgs() != null) {
                    // Pass the arguments from the current menu to the new menu. If the new menu has arguments with the
                    // same name, they will be overwritten
                    argumentsMap.putAll(holder.get().getTypedArgs());
                }

                for (int index = 0; index < menuArgumentNames.size(); index++) {
                    final String argumentName = menuArgumentNames.get(index);

                    if (passedArgumentValues.length <= index) {
                        // This should never be the case!
                        plugin.debug(
                                DebugLevel.HIGHEST,
                                Level.WARNING,
                                "Not enough arguments given for menu " + menuName + " when opening using the [openguimenu] or [openmenu] action!"
                        );
                        break;
                    }

                    if (menuArgumentNames.size() == index + 1) {
                        // If this is the last argument, get all remaining values and join them
                        final String lastArgumentValue = String.join(" ", Arrays.asList(passedArgumentValues).subList(index, passedArgumentValues.length));
                        argumentsMap.put(argumentName, lastArgumentValue);
                        break;
                    }

                    argumentsMap.put(argumentName, passedArgumentValues[index]);
                }

                if (holder.isEmpty()) {
                    menuToOpen.openMenu(player, argumentsMap, null);
                    break;
                }

                menuToOpen.openMenu(player, argumentsMap, holder.get().getPlaceholderPlayer());
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
                if (holder.isEmpty()) {
                    plugin.debug(
                            DebugLevel.MEDIUM,
                            Level.WARNING,
                            player.getName() + " does not have menu open! Nothing to refresh!"
                    );
                    break;
                }

                holder.get().refreshMenu();
                break;

            case TAKE_MONEY:
                if (plugin.getVault() == null || !plugin.getVault().hooked()) {
                    plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Vault not hooked! Cannot take money!");
                    break;
                }

                try {
                    plugin.getVault().takeMoney(player, Double.parseDouble(executable));
                } catch (final NumberFormatException exception) {
                    plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Amount for take money action: " + executable + ", is not a valid number!"
                    );
                }
                break;

            case GIVE_MONEY:
                if (plugin.getVault() == null || !plugin.getVault().hooked()) {
                    plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Vault not hooked! Cannot give money!");
                    break;
                }

                try {
                    plugin.getVault().giveMoney(player, Double.parseDouble(executable));
                } catch (final NumberFormatException exception) {
                    plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Amount for give money action: " + executable + ", is not a valid number!"
                    );
                }
                break;

            case TAKE_EXP:
            case GIVE_EXP:
                final String lowerCaseExecutable = executable.toLowerCase();

                try {
                    if (Integer.parseInt(lowerCaseExecutable.replaceAll("l", "")) <= 0) break;

                    if (actionType == ActionType.TAKE_EXP) {
                        ExpUtils.setExp(player, "-" + lowerCaseExecutable);
                        break;
                    }

                    ExpUtils.setExp(player, lowerCaseExecutable);
                    break;

                } catch (final NumberFormatException exception) {
                    if (actionType == ActionType.TAKE_EXP) {
                        plugin.debug(
                                DebugLevel.HIGHEST,
                                Level.WARNING,
                                "Amount for take exp action: " + executable + ", is not a valid number!"
                        );
                        break;
                    }

                    plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Amount for give exp action: " + executable + ", is not a valid number!"
                    );
                    break;
                }

            case GIVE_PERM:
                if (plugin.getVault() == null || !plugin.getVault().hooked()) {
                    plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Vault not hooked! Cannot give permission: " + executable + "!");
                    break;
                }

                plugin.getVault().givePermission(player, executable);
                break;

            case TAKE_PERM:
                if (plugin.getVault() == null || !plugin.getVault().hooked()) {
                    plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Vault not hooked! Cannot take permission: " + executable + "!");
                    break;
                }

                plugin.getVault().takePermission(player, executable);
                break;

            case BROADCAST_SOUND:
            case BROADCAST_WORLD_SOUND:
            case PLAY_SOUND:
                final Sound sound;
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
                        break;
                    }
                } else {
                    String[] parts = executable.split(" ", 3);

                    try {
                        sound = SoundUtils.getSound(parts[0].toUpperCase());
                    } catch (final IllegalArgumentException exception) {
                        plugin.printStacktrace(
                                "Sound name given for sound action: " + parts[0] + ", is not a valid sound!",
                                exception
                        );
                        break;
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
                }
                break;

            default:
                break;
        }
    }
}