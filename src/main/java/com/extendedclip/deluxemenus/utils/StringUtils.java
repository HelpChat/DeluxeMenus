package com.extendedclip.deluxemenus.utils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringUtils {

    private static final char SECTION_CHAR = '§';
    private static final String COLOR_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";

    private final static Pattern HEX_PATTERN = Pattern
            .compile("&(#[a-f0-9]{6})", Pattern.CASE_INSENSITIVE);

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .character(SECTION_CHAR)
            .hexCharacter('#')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    /**
     * Translates the ampersand color codes like '&7' to their section symbol counterparts like '§7'.
     * <br>
     * It also translates hex colors like '&#aaFF00' to their section symbol counterparts like '§x§a§a§F§F§0§0'.
     *
     * @param input The string in which to translate the color codes.
     * @return The string with the translated colors.
     */
    @NotNull
    public static String legacyColor(@NotNull String input) {
        final Matcher matcher = HEX_PATTERN.matcher(input);
        final StringBuilder builder = new StringBuilder();

        while (matcher.find()) {
            final StringBuilder replacement = new StringBuilder().append(SECTION_CHAR).append('x');
            for (final char character : matcher.group(1).substring(1).toCharArray()) {
                replacement.append(SECTION_CHAR).append(character);
            }
            matcher.appendReplacement(builder, Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(builder);

        final char[] characters = builder.toString().toCharArray();
        for (int i = 0; i < characters.length - 1; i++) {
            if (characters[i] != '&' || COLOR_CODES.indexOf(characters[i + 1]) == -1) continue;
            characters[i] = SECTION_CHAR;
            characters[i + 1] = Character.toLowerCase(characters[i + 1]);
        }

        return new String(characters);
    }

    /**
     * Parses a configured string into a component. Section symbols already present in the input,
     * such as those produced by PlaceholderAPI, are honoured alongside the '&' codes.
     */
    @NotNull
    public static Component color(@NotNull final String input) {
        return SERIALIZER.deserialize(legacyColor(input));
    }

    /**
     * As {@link #color(String)}, but with italics disabled <em>by default</em>. Item display names
     * and lore render italic when set as components, which the legacy string setters suppressed by
     * building on a {@code Style.EMPTY.withItalic(false)} base.
     * <p>
     * This must be a <b>fallback</b>, not an override. The legacy serializer puts a single-format
     * line's style on the root component, so {@code decoration(ITALIC, false)} would overwrite an
     * explicit {@code &o} and make italic text impossible to configure.
     */
    @NotNull
    public static Component colorNonItalic(@NotNull final String input) {
        return color(input).applyFallbackStyle(TextDecoration.ITALIC.withState(false));
    }

    /**
     * Serializes a component back into a legacy section symbol string, for comparison against
     * configured values.
     */
    @NotNull
    public static String legacy(@Nullable final Component component) {
        return component == null ? "" : SERIALIZER.serialize(component);
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
