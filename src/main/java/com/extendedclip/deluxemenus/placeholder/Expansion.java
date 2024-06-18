package com.extendedclip.deluxemenus.placeholder;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.menu.options.MenuOptions;
import com.extendedclip.deluxemenus.utils.VersionHelper;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Expansion extends PlaceholderExpansion {

    private final DeluxeMenus plugin;

    public Expansion(@NotNull final DeluxeMenus instance) {
        this.plugin = instance;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return plugin.getName().toLowerCase();
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public @Nullable String onRequest(final OfflinePlayer offlinePlayer, @NotNull final String input) {
        if (offlinePlayer == null || !offlinePlayer.isOnline()) {
            return null;
        }

        final Player onlinePlayer = offlinePlayer.getPlayer();
        if (onlinePlayer == null) {
            return null;
        }

        final String parsedInput = PlaceholderAPI.setBracketPlaceholders(onlinePlayer, input);

        if (input.startsWith("meta_")) {
            if (!VersionHelper.IS_PDC_VERSION) {
                return null;
            }

            final boolean isHasValueRequest = parsedInput.startsWith("meta_has_value_");

            final String finalInput = parsedInput.startsWith("meta_has_value_")
                    ? parsedInput.substring(15)
                    : parsedInput.substring(5);

            if (!finalInput.contains("_")) {
                return null;
            }

            String[] parts = isHasValueRequest
                    ? finalInput.split("_", 2)
                    : finalInput.split("_", 3);

            if (parts.length < 2) {
                return null;
            }

            final String result = plugin.getPersistentMetaHandler().getMeta(
                    onlinePlayer,
                    parts[0],
                    parts[1],
                    null
            );

            // %deluxemenus_meta_has_value_<key>_<type>%
            if (isHasValueRequest) {
                return result == null
                        ? PlaceholderAPIPlugin.booleanFalse()
                        : PlaceholderAPIPlugin.booleanTrue();

            }

            // %deluxemenus_meta_<key>_<type>_[defaultValue]%
            if (result != null) {
                return result;
            }

            // return the default value
            return parts.length > 2 ? parts[2] : "";
        }

        switch (input) {
            case "is_in_menu": {
                return Menu.getMenuHolder(onlinePlayer).isPresent() ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
            }
            case "opened_menu": {
                return Menu.getOpenMenu(onlinePlayer).map(Menu::options).map(MenuOptions::name).orElse("");
            }
            case "last_menu": {
                return Menu.getLastMenu(onlinePlayer).map(Menu::options).map(MenuOptions::name).orElse("");
            }
        }
        return null;
    }
}
