package com.extendedclip.deluxemenus.menu.command;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.utils.DebugLevel;
import com.extendedclip.deluxemenus.utils.Messages;
import com.extendedclip.deluxemenus.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

public class RegistrableMenuCommand extends Command {

    private static final String FALLBACK_PREFIX = "DeluxeMenus".toLowerCase(Locale.ROOT).trim();

    private final DeluxeMenus plugin;

    private Menu menu;
    private boolean registered = false;
    private boolean unregistered = false;

    public RegistrableMenuCommand(final @NotNull DeluxeMenus plugin,
                                  final @NotNull Menu menu) {
        super(menu.options().commands().isEmpty() ? menu.options().name() : menu.options().commands().getFirst());
        this.plugin = plugin;
        this.menu = menu;

        if (menu.options().commands().size() > 1) {
            this.setAliases(menu.options().commands().subList(1, menu.options().commands().size()));
        }
    }

    @Override
    public boolean execute(final @NotNull CommandSender sender, final @NotNull String commandLabel, final @NotNull String @NotNull [] typedArgs) {
        if (this.unregistered) {
            throw new IllegalStateException("This command was unregistered!");
        }

        if (!(sender instanceof Player player)) {
            plugin.sms(sender, Messages.MENUS_ARE_PLAYER_ONLY);
            return true;
        }

        Map<String, String> argMap = null;

        if (!menu.options().arguments().isEmpty()) {
            plugin.debug(DebugLevel.LOWEST, Level.INFO, "has args");
            if (typedArgs.length < menu.options().arguments().size()) {
                if (menu.options().argumentsUsageMessage().isPresent()) {
                    String usageMessage = menu.options().argumentsUsageMessage().get();
                    plugin.sms(sender, StringUtils.color(StringUtils.replacePlaceholders(usageMessage, player)));
                }
                return true;
            }
            argMap = new HashMap<>();
            int index = 0;
            for (String arg : menu.options().arguments()) {
                if (index + 1 == menu.options().arguments().size()) {
                    String last = String.join(" ", Arrays.asList(typedArgs).subList(index, typedArgs.length));
                    plugin.debug(DebugLevel.LOWEST, Level.INFO, "arg: " + arg + " => " + last);
                    argMap.put(arg, last);
                } else {
                    argMap.put(arg, typedArgs[index]);
                    plugin.debug(DebugLevel.LOWEST, Level.INFO, "arg: " + arg + " => " + typedArgs[index]);
                }
                index++;
            }
        }

        plugin.debug(DebugLevel.LOWEST, Level.INFO, "opening menu: " + menu.options().name());
        menu.openMenu(player, argMap, null);
        return true;
    }

    public void register() {
        if (registered) {
            throw new IllegalStateException("This command was already registered!");
        }

        registered = true;

        boolean registered = Bukkit.getCommandMap().register(FALLBACK_PREFIX, this);
        if (registered) {
            plugin.debug(
                    DebugLevel.LOW,
                    Level.INFO,
                    "Registered command: " + this.getName() + " for menu: " + menu.options().name()
            );
        } else {
            plugin.debug(
                    DebugLevel.HIGHEST,
                    Level.WARNING,
                    "Failed to register command: " + this.getName() + " for menu: " + menu.options().name()
                            + ". A command with that name already exists. Use /deluxemenus:" + this.getName() + " instead."
            );
        }
    }

    public void unregister() {
        if (!registered) {
            throw new IllegalStateException("This command was not registered!");
        }

        if (unregistered) {
            throw new IllegalStateException("This command was already unregistered!");
        }

        unregistered = true;

        try {
            final CommandMap commandMap = Bukkit.getCommandMap();
            final Map<String, Command> knownCommandsMap = commandMap.getKnownCommands();

            // We need to remove every single alias because CommandMap#register() adds them all to the map.
            // If we do not remove them, then we will have dangling references to the command.
            knownCommandsMap.remove(this.getName());
            knownCommandsMap.remove(FALLBACK_PREFIX + ":" + this.getName());

            for (String alias : this.getAliases()) {
                knownCommandsMap.remove(alias);
                knownCommandsMap.remove(FALLBACK_PREFIX + ":" + alias);
            }

            boolean unregistered = this.unregister(commandMap);
            if (unregistered) {
                plugin.debug(
                        DebugLevel.HIGH,
                        Level.INFO,
                        "Successfully unregistered command: " + this.getName()
                );
            } else {
                plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Failed to unregister command: " + this.getName()
                );
            }
        } catch (final @NotNull Exception exception) {
            plugin.printStacktrace(
                    "Something went wrong while trying to unregister command: " + this.getName(),
                    exception
            );
        }

        this.menu = null;
    }

    public boolean registered() {
        return registered;
    }
}
