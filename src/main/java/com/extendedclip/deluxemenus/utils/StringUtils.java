package com.extendedclip.deluxemenus.utils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringUtils {

    private final static Pattern HEX_PATTERN = Pattern
            .compile("&(#[a-f0-9]{6})", Pattern.CASE_INSENSITIVE);

    /**
     * Translates the ampersand color codes like '&7' to their section symbol counterparts like '§7'.
     * <br>
     * It also translates hex colors like '&#aaFF00' to their section symbol counterparts like '§x§a§a§F§F§0§0'.
     *
     * @param input The string in which to translate the color codes.
     * @return The string with the translated colors.
     */
    @NotNull
    public static String color(@NotNull String input) {
        // Hex Support for 1.16.1+
        Matcher m = HEX_PATTERN.matcher(input);
        if (VersionHelper.IS_HEX_VERSION) {
            while (m.find()) {
                input = input.replace(m.group(), ChatColor.of(m.group(1)).toString());
            }
        }

        return ChatColor.translateAlternateColorCodes('&', input);
    }

    @NotNull
    public static String replacePlaceholdersAndArguments(@NotNull String input, final @Nullable Map<String, String> arguments,
                                                         final @Nullable Player player,
                                                         final boolean parsePlaceholdersInsideArguments,
                                                         final boolean parsePlaceholdersAfterArguments) {
        if (player == null) {
            return replaceArguments(input, arguments, null, parsePlaceholdersInsideArguments);
        }

        if (parsePlaceholdersAfterArguments) {
            return replacePlaceholders(replaceArguments(input, arguments, player, parsePlaceholdersInsideArguments), player);
        }

        return replaceArguments(replacePlaceholders(input, player), arguments, player, parsePlaceholdersInsideArguments);
    }

    @NotNull
    public static String replacePlaceholders(final @NotNull String input, final @NotNull Player player) {
        return PlaceholderAPI.setPlaceholders(player, input);
    }

    @NotNull
    public static String replaceArguments(@NotNull String input, final @Nullable Map<String, String> arguments,
                                          final @Nullable Player player, boolean parsePlaceholdersInsideArguments) {
        if (arguments == null || arguments.isEmpty()) {
            return input;
        }

        for (final Map.Entry<String, String> entry : arguments.entrySet()) {
            final String value = player != null && parsePlaceholdersInsideArguments
                    ? replacePlaceholders(entry.getValue(), player)
                    : entry.getValue();
            input = input.replace("{" + entry.getKey() + "}", value);
        }

        return input;
    }

    @Nullable
    public static Color parseRGBColor(@NotNull final String input) {
        final String[] parts = input.split(",");
        try {
            return Color.fromRGB(
                    Integer.parseInt(parts[0].trim()),
                    Integer.parseInt(parts[1].trim()),
                    Integer.parseInt(parts[2].trim())
            );
        } catch (final Exception exception) {
            return null;
        }
    }
}
