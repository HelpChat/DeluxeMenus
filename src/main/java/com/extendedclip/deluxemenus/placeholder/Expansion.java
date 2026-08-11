package com.extendedclip.deluxemenus.placeholder;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.menu.options.MenuOptions;
import com.extendedclip.deluxemenus.persistentmeta.DataType;
import com.extendedclip.deluxemenus.utils.VersionHelper;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
    public @NotNull List<String> getPlaceholders() {
        return List.of(
                "%deluxemenus_is_in_menu%",
                "%deluxemenus_opened_menu%",
                "%deluxemenus_last_menu%",
                "%deluxemenus_meta_has_value_<key>_[type]%",
                "%deluxemenus_meta_<key>_<type>_[default-value]%",
                "%deluxemenus_has_ephemeral_cooldown_<id>%",
                "%deluxemenus_ephemeral_cooldown_<id>%",
                "%deluxemenus_ephemeral_cooldown_millis_<id>%",
                "%deluxemenus_ephemeral_cooldown_formatted_<id>%"
        );
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

        switch (parsedInputLower) {
            case "is_in_menu" -> {
                return getBooleanAsString(Menu.getMenuHolder(onlinePlayer).isPresent());
            }
            case "opened_menu" -> {
                return Menu.getOpenMenu(onlinePlayer).map(Menu::options).map(MenuOptions::name).orElse("");
            }
            case "last_menu" -> {
                return Menu.getLastMenu(onlinePlayer).map(Menu::options).map(MenuOptions::name).orElse("");
            }
        }

        // %deluxemenus_has_ephemeral_cooldown_<id>%
        if (parsedInputLower.startsWith("has_ephemeral_cooldown_")) {
            return getBooleanAsString(plugin.getEphemeralCooldownManager()
                    .isOnCooldown(onlinePlayer.getUniqueId(), parsedInput.substring(23)));
        }

        if (parsedInputLower.startsWith("ephemeral_cooldown_")) {
            // %deluxemenus_ephemeral_cooldown_millis_<id>%
            if (parsedInputLower.startsWith("ephemeral_cooldown_millis_")) {
                return String.valueOf(getRemainingMillis(onlinePlayer, parsedInput.substring(26)));
            }

            // %deluxemenus_ephemeral_cooldown_formatted_<id>%
            if (parsedInputLower.startsWith("ephemeral_cooldown_formatted_")) {
                return formatDuration(getRemainingMillis(onlinePlayer, parsedInput.substring(29)));
            }

            // %deluxemenus_ephemeral_cooldown_<id>%
            // Rounded up so a cooldown never displays as 0 while it is still running.
            final long remaining = getRemainingMillis(onlinePlayer, parsedInput.substring(19));
            return String.valueOf((remaining + 999L) / 1000L);
        }

        if (!parsedInputLower.startsWith("meta_")) {
            return null;
        }

        if (!VersionHelper.IS_PDC_VERSION || plugin.getPersistentMetaHandler() == null) {
            return null;
        }

        // %deluxemenus_meta_has_value_<key>_[type]%
        if (parsedInputLower.startsWith("meta_has_value_")) {
            final String hasValueInput = parsedInput.substring(15);
            final String[] hasValueParts = hasValueInput.split("_", 2);

            if (hasValueParts.length < 1 || hasValueParts.length > 2) {
                return null;
            }

            final NamespacedKey key = plugin.getPersistentMetaHandler().getKey(hasValueParts[0]);
            if (key == null) {
                return getBooleanAsString(false);
            }

            if (hasValueParts.length == 1) {
                return getBooleanAsString(plugin.getPersistentMetaHandler().hasMetaValue(onlinePlayer, key));
            }

            final DataType<?, ?> type = DataType.getSupportedTypeByName(hasValueParts[1]);
            if (type == null) {
                return getBooleanAsString(false);
            }

            return getBooleanAsString(plugin.getPersistentMetaHandler().hasMetaValue(onlinePlayer, key, type));
        }

        // %deluxemenus_meta_<key>_<type>_[default-value]%
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
            return getBooleanAsString(false);
        }

        final DataType<?, ?> type = DataType.getSupportedTypeByName(parts[1]);
        if (type == null) {
            return getBooleanAsString(false);
        }

        final Object result = plugin.getPersistentMetaHandler().getMetaValue(onlinePlayer, key, type);

        if (result != null) {
            return String.valueOf(result);
        }

        // return the default value
        return parts.length > 2 ? parts[2] : "";
    }

    private @NotNull String getBooleanAsString(final boolean value) {
        return value ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
    }

    private long getRemainingMillis(final @NotNull Player player, final @NotNull String id) {
        return plugin.getEphemeralCooldownManager().getRemainingMillis(player.getUniqueId(), id);
    }

    /**
     * Formats a remaining duration as {@code 1h 2m 3s}, leaving out the units that are zero.
     * Returns an empty string when there is nothing left to wait for.
     */
    private @NotNull String formatDuration(final long millis) {
        if (millis <= 0) {
            return "";
        }

        // Rounded up so a cooldown never displays as 0s while it is still running.
        long seconds = (millis + 999L) / 1000L;

        final long hours = seconds / 3600L;
        seconds -= hours * 3600L;
        final long minutes = seconds / 60L;
        seconds -= minutes * 60L;

        final StringBuilder builder = new StringBuilder();

        if (hours > 0) {
            builder.append(hours).append('h');
        }

        if (minutes > 0) {
            if (builder.length() > 0) builder.append(' ');
            builder.append(minutes).append('m');
        }

        if (seconds > 0) {
            if (builder.length() > 0) builder.append(' ');
            builder.append(seconds).append('s');
        }

        return builder.toString();
    }
}
