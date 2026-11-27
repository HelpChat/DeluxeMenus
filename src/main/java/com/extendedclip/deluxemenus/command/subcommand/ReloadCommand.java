package com.extendedclip.deluxemenus.command.subcommand;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.config.DeluxeMenusConfig;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
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

        if (!arguments.isEmpty() && Menu.getMenuByName(arguments.get(0)).isEmpty()) {
            plugin.sms(sender, Messages.INVALID_MENU.message().replaceText(MENU_REPLACER_BUILDER.replacement(arguments.get(0)).build()));
            return;
        }

        if (!plugin.tryStartReload()) {
            plugin.sms(sender, Messages.RELOAD_IN_PROGRESS);
            return;
        }

        try {
            if (arguments.isEmpty()) {
                reloadAll(sender);
            } else {
                reloadMenu(sender, arguments.get(0));
            }
        } catch (final Throwable throwable) {
            plugin.finishReload();
            throw throwable;
        }
    }

    /**
     * Reloads config.yml and every menu. Reading and parsing the files happens on an async thread so the reload
     * does not cause a lag spike. Only building the menus and swapping them in happens on the main thread.
     */
    private void reloadAll(final @NotNull CommandSender sender) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            DeluxeMenusConfig.PreparedReload prepared;
            try {
                prepared = plugin.getConfiguration().prepareReload();
            } catch (final Throwable throwable) {
                plugin.getLogger().log(Level.SEVERE, "Something went wrong while reloading DeluxeMenus!", throwable);
                prepared = null;
            }

            final DeluxeMenusConfig.PreparedReload result = prepared;
            runOnMainThread(() -> {
                if (result == null) {
                    plugin.sms(sender, Messages.RELOAD_FAIL);
                    return;
                }

                plugin.clearCaches();
                plugin.replaceConfig(result.mainConfig());
                plugin.getGeneralConfig().load();
                Menu.unload(plugin);
                plugin.getConfiguration().applyMenus(result.menus());
                plugin.sms(sender, Messages.RELOAD_SUCCESS);

                int gLoaded = Menu.getLoadedMenuSize();

                if (gLoaded == 1) {
                    plugin.sms(sender, Messages.MENU_LOADED.message().replaceText(AMOUNT_REPLACER_BUILDER.replacement(String.valueOf(gLoaded)).build()));
                    return;
                }

                plugin.sms(sender, Messages.MENUS_LOADED.message().replaceText(AMOUNT_REPLACER_BUILDER.replacement(String.valueOf(gLoaded)).build()));
            });
        });
    }

    /**
     * Reloads a single menu. Reading and parsing its file happens on an async thread.
     */
    private void reloadMenu(final @NotNull CommandSender sender, final @NotNull String menuName) {
        // Read everything we need from the in-memory config.yml here, on the main thread.
        final FileConfiguration config = plugin.getConfig();
        final ConfigurationSection menusSection = config.getConfigurationSection("gui_menus");
        final boolean listed = menusSection != null && menusSection.getKeys(false).contains(menuName);
        final boolean hasFile = listed && config.contains("gui_menus." + menuName + ".file");
        final String fileName = hasFile ? config.getString("gui_menus." + menuName + ".file") : null;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean configValid;
            DeluxeMenusConfig.ParsedMenu parsed = null;
            try {
                configValid = plugin.getConfiguration().checkConfig(null, "config.yml", false) != null;
                if (configValid && hasFile) {
                    parsed = plugin.getConfiguration().parseMenuFile(menuName, fileName);
                }
            } catch (final Throwable throwable) {
                plugin.getLogger().log(Level.SEVERE, "Something went wrong while reloading menu: " + menuName, throwable);
                configValid = false;
            }

            final boolean valid = configValid;
            final DeluxeMenusConfig.ParsedMenu parsedMenu = parsed;
            runOnMainThread(() -> {
                if (!valid) {
                    plugin.sms(sender, Messages.RELOAD_FAIL);
                    return;
                }

                Menu.unload(plugin, menuName);

                boolean loaded = false;
                if (listed && !hasFile) {
                    plugin.getConfiguration().loadMenu(plugin.getConfig(), menuName, true, "config");
                    loaded = true;
                } else if (parsedMenu != null) {
                    plugin.getConfiguration().applyMenus(List.of(parsedMenu));
                    loaded = Menu.getMenuByName(menuName).isPresent();
                }

                if (loaded) {
                    plugin.sms(sender, Messages.MENU_RELOADED.message().replaceText(MENU_REPLACER_BUILDER.replacement(menuName).build()));
                    return;
                }

                plugin.sms(sender, Messages.MENU_NOT_RELOADED.message().replaceText(MENU_REPLACER_BUILDER.replacement(menuName).build()));
            });
        });
    }

    /**
     * Runs the task on the main thread and marks the reload as finished afterwards, even if the task fails.
     * If the plugin was disabled in the meantime, the task is skipped.
     */
    private void runOnMainThread(final @NotNull Runnable task) {
        if (!plugin.isEnabled()) {
            plugin.finishReload();
            return;
        }

        try {
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    task.run();
                } finally {
                    plugin.finishReload();
                }
            });
        } catch (final IllegalStateException exception) {
            // The plugin was disabled between the check above and scheduling the task.
            plugin.finishReload();
        }
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
