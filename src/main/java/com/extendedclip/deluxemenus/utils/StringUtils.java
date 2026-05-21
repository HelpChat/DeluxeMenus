package com.extendedclip.deluxemenus.utils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringUtils {

    private final static Pattern HEX_PATTERN = Pattern
            .compile("&(#[a-f0-9]{6})", Pattern.CASE_INSENSITIVE);
    private final static Pattern MINI_HEX_PATTERN = Pattern
            .compile("<(?:color:)?#([a-f0-9]{6})>", Pattern.CASE_INSENSITIVE);
    private final static MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private final static LegacyComponentSerializer LEGACY_SECTION = LegacyComponentSerializer.legacySection();
    private final static Map<String, String> MINI_TITLE_TAGS = Map.ofEntries(
            Map.entry("black", ChatColor.BLACK.toString()),
            Map.entry("dark_blue", ChatColor.DARK_BLUE.toString()),
            Map.entry("dark_green", ChatColor.DARK_GREEN.toString()),
            Map.entry("dark_aqua", ChatColor.DARK_AQUA.toString()),
            Map.entry("dark_red", ChatColor.DARK_RED.toString()),
            Map.entry("dark_purple", ChatColor.DARK_PURPLE.toString()),
            Map.entry("gold", ChatColor.GOLD.toString()),
            Map.entry("gray", ChatColor.GRAY.toString()),
            Map.entry("grey", ChatColor.GRAY.toString()),
            Map.entry("dark_gray", ChatColor.DARK_GRAY.toString()),
            Map.entry("dark_grey", ChatColor.DARK_GRAY.toString()),
            Map.entry("blue", ChatColor.BLUE.toString()),
            Map.entry("green", ChatColor.GREEN.toString()),
            Map.entry("aqua", ChatColor.AQUA.toString()),
            Map.entry("red", ChatColor.RED.toString()),
            Map.entry("light_purple", ChatColor.LIGHT_PURPLE.toString()),
            Map.entry("yellow", ChatColor.YELLOW.toString()),
            Map.entry("white", ChatColor.WHITE.toString()),
            Map.entry("bold", ChatColor.BOLD.toString()),
            Map.entry("b", ChatColor.BOLD.toString()),
            Map.entry("italic", ChatColor.ITALIC.toString()),
            Map.entry("i", ChatColor.ITALIC.toString()),
            Map.entry("underlined", ChatColor.UNDERLINE.toString()),
            Map.entry("u", ChatColor.UNDERLINE.toString()),
            Map.entry("strikethrough", ChatColor.STRIKETHROUGH.toString()),
            Map.entry("st", ChatColor.STRIKETHROUGH.toString()),
            Map.entry("obfuscated", ChatColor.MAGIC.toString()),
            Map.entry("obf", ChatColor.MAGIC.toString()),
            Map.entry("reset", ChatColor.RESET.toString())
    );

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
    public static String colorMenuTitle(@NotNull final String input) {
        try {
            final String parsed = color(LEGACY_SECTION.serialize(MINI_MESSAGE.deserialize(input)));
            if (!hasMiniTitleTag(parsed)) {
                return parsed;
            }
        } catch (final Exception ignored) {
        }
        return color(replaceMiniTitleTags(input));
    }

    private static boolean hasMiniTitleTag(@NotNull final String input) {
        return input.indexOf('<') != -1 && input.indexOf('>') != -1;
    }

    @NotNull
    private static String replaceMiniTitleTags(@NotNull String input) {
        if (VersionHelper.IS_HEX_VERSION) {
            final Matcher matcher = MINI_HEX_PATTERN.matcher(input);
            final StringBuffer buffer = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(ChatColor.of("#" + matcher.group(1)).toString()));
            }
            matcher.appendTail(buffer);
            input = buffer.toString();
        }

        for (final Map.Entry<String, String> entry : MINI_TITLE_TAGS.entrySet()) {
            input = replaceMiniTitleTag(input, entry.getKey(), entry.getValue());
        }

        return input;
    }

    @NotNull
    private static String replaceMiniTitleTag(
            @NotNull String input,
            @NotNull final String tag,
            @NotNull final String replacement
    ) {
        input = Pattern.compile("<" + tag + ">", Pattern.CASE_INSENSITIVE)
                .matcher(input)
                .replaceAll(Matcher.quoteReplacement(replacement));
        return Pattern.compile("</" + tag + ">", Pattern.CASE_INSENSITIVE)
                .matcher(input)
                .replaceAll(Matcher.quoteReplacement(ChatColor.RESET.toString()));
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
