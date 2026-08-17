package com.extendedclip.deluxemenus.config;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.utils.DebugLevel;
import org.jetbrains.annotations.NotNull;

public class GeneralConfig {
    private final DeluxeMenus plugin;

    private boolean checkForUpdates = true;
    private DebugLevel debugLevel = getDefaultDebugLevel();
    private boolean useAdminCommandsInMenusList = false;
    private int menusListPageSize = 10;
    private int metasListPageSize = 15;
    private int maxEphemeralCooldownSeconds = -1;
    private boolean suppressDefaultItalics = true;

    public GeneralConfig(final @NotNull DeluxeMenus plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.getConfig().addDefault("check_updates", checkForUpdates);
        plugin.getConfig().addDefault("debug", debugLevel.name());
        plugin.getConfig().addDefault("use_admin_commands_in_menus_list", false);
        plugin.getConfig().addDefault("menus_list_page_size", menusListPageSize);
        plugin.getConfig().addDefault("metas_list_page_size", metasListPageSize);
        plugin.getConfig().addDefault("max_ephemeral_cooldown_seconds", maxEphemeralCooldownSeconds);
        plugin.getConfig().addDefault("suppress_default_italics", suppressDefaultItalics);

        checkForUpdates = plugin.getConfig().getBoolean("check_updates", false);
        debugLevel = loadDebugLevel();
        useAdminCommandsInMenusList = plugin.getConfig().getBoolean("use_admin_commands_in_menus_list", false);
        menusListPageSize = plugin.getConfig().getInt("menus_list_page_size", 10);
        metasListPageSize = plugin.getConfig().getInt("metas_list_page_size", 15);
        maxEphemeralCooldownSeconds = plugin.getConfig().getInt("max_ephemeral_cooldown_seconds", maxEphemeralCooldownSeconds);
        suppressDefaultItalics = plugin.getConfig().getBoolean("suppress_default_italics", true);
    }

    public void reload() {
        plugin.reloadConfig();
        load();
    }

    public boolean checkForUpdates() {
        return checkForUpdates;
    }

    public DebugLevel debugLevel() {
        return debugLevel;
    }

    public boolean useAdminCommandsInMenusList() {
        return useAdminCommandsInMenusList;
    }

    public int menusListPageSize() {
        return menusListPageSize;
    }

    public int metasListPageSize() {
        return metasListPageSize;
    }

    /**
     * The longest duration the {@code [ephemeralcooldown]} action is allowed to set, in seconds.
     * A value of zero or less means there is no maximum.
     */
    public int maxEphemeralCooldownSeconds() {
        return maxEphemeralCooldownSeconds;
    }

    public boolean suppressDefaultItalics() {
        return suppressDefaultItalics;
    }

    private @NotNull DebugLevel loadDebugLevel() {
        String configDebugLevel = plugin.getConfig().getString("debug", "HIGHEST");

        if (configDebugLevel.equalsIgnoreCase("true")) {
            configDebugLevel = "LOWEST";
            plugin.getConfig().set("debug", "LOWEST");
        }

        if (configDebugLevel.equalsIgnoreCase("false")) {
            configDebugLevel = "HIGHEST";
            plugin.getConfig().set("debug", "HIGHEST");
        }

        final DebugLevel debugLevel = DebugLevel.getByName(configDebugLevel);
        return debugLevel == null ? getDefaultDebugLevel() : debugLevel;
    }

    private DebugLevel getDefaultDebugLevel() {
        return DebugLevel.LOW;
    }
}
