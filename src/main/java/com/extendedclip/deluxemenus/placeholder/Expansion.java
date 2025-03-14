package com.extendedclip.deluxemenus.placeholder;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.menu.options.MenuOptions;
import com.extendedclip.deluxemenus.utils.VersionHelper;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
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
        final String parsedInputLower = parsedInput.toLowerCase();

        if (parsedInputLower.startsWith("meta_")) {
            if (!VersionHelper.IS_PDC_VERSION) {
                return null;
            }

            // %deluxemenus_meta_has_value_<key>_<type>%
            if (parsedInputLower.startsWith("meta_has_value_")) {
                final String hasValueInput = parsedInput.substring(15);

                if (!hasValueInput.contains("_")) {
                    return null;
                }

                String[] hasValueParts = hasValueInput.split("_", 2);

                if (hasValueParts.length < 2) {
                    return null;
                }

                final NamespacedKey key = plugin.getPersistentMetaHandler().getKey(hasValueParts[0]);
                if (key == null) {
                    return "INVALID_KEY";
                }

                final PersistentDataType<?, ?> type = plugin.getPersistentMetaHandler().getSupportedTypeByName(hasValueParts[1]);
                if (type == null) {
                    return "INVALID_TYPE";
                }

                final boolean hasValue = plugin.getPersistentMetaHandler().hasMetaValue(onlinePlayer, key, type);

                return getBooleanAsString(hasValue);
            }

            final String getValueInput = parsedInput.substring(5);

            if (!getValueInput.contains("_")) {
                return null;
            }

            final String[] parts = getValueInput.split("_", 3);

            if (parts.length < 2) {
                return null;
            }

            final NamespacedKey key = plugin.getPersistentMetaHandler().getKey(parts[0]);
            if (key == null) {
                return "INVALID_KEY";
            }

            final PersistentDataType<?, ?> type = plugin.getPersistentMetaHandler().getSupportedTypeByName(parts[1]);
            if (type == null) {
                return "INVALID_TYPE";
            }

            final Object result = plugin.getPersistentMetaHandler().getMetaValue(onlinePlayer, key, type);

            // %deluxemenus_meta_<key>_<type>_[defaultValue]%
            if (result != null) {
                return String.valueOf(result);
            }

            // return the default value
            return parts.length > 2 ? parts[2] : "";
        }

        switch (parsedInputLower) {
            case "is_in_menu": {
                return getBooleanAsString(Menu.getMenuHolder(onlinePlayer).isPresent());
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

    private @NotNull String getBooleanAsString(final boolean value) {
        return value ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
    }
}
