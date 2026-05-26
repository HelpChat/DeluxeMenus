package com.extendedclip.deluxemenus.utils;

import com.extendedclip.deluxemenus.DeluxeMenus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AdventureUtils {
    private static final GsonComponentSerializer GSON = GsonComponentSerializer.gson();
    private static final LegacyComponentSerializer LEGACY_SECTION_WITH_HEX = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    private static final LegacyComponentSerializer LEGACY_SECTION = LegacyComponentSerializer.legacySection();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private AdventureUtils() {
        throw new AssertionError("Util classes should not be initialized");
    }

    public static void sendJson(@NotNull final DeluxeMenus plugin, CommandSender sender, String json) {
        plugin.audiences().sender(sender).sendMessage(fromJson(json));
    }

    public static @NotNull Component fromJson(@NotNull final String json) {
        return GSON.deserialize(json);
    }

    public static @NotNull Component fromFormattedText(@NotNull final String input) {
        return MINI_MESSAGE.deserialize(legacyToMiniMessage(input));
    }

    public static @NotNull Component fromMiniMessage(@NotNull final String input) {
        return MINI_MESSAGE.deserialize(legacyToMiniMessage(input));
    }

    public static @NotNull String toLegacyString(@NotNull final String input) {
        return toLegacyString(fromFormattedText(input));
    }

    public static @NotNull String toLegacyString(@NotNull final Component component) {
        return VersionHelper.IS_HEX_VERSION
                ? LEGACY_SECTION_WITH_HEX.serialize(component)
                : LEGACY_SECTION.serialize(component);
    }

    private static @NotNull String legacyToMiniMessage(@NotNull final String input) {
        final StringBuilder builder = new StringBuilder(input.length());
        boolean hasFormatting = false;
        int index = 0;

        while (index < input.length()) {
            final char current = input.charAt(index);

            if (!isLegacyPrefix(current) || index + 1 >= input.length()) {
                builder.append(current);
                index++;
                continue;
            }

            final char code = Character.toLowerCase(input.charAt(index + 1));

            if (code == '#') {
                final String hex = parseInlineHex(input, index + 2);
                if (hex == null) {
                    builder.append(current);
                    index++;
                    continue;
                }

                if (hasFormatting) {
                    builder.append("<reset>");
                }

                builder.append("<#").append(hex).append('>');
                hasFormatting = true;
                index += 8;
                continue;
            }

            if (code == 'x') {
                final String hex = parseSectionHex(input, index);
                if (hex == null) {
                    builder.append(current);
                    index++;
                    continue;
                }

                if (hasFormatting) {
                    builder.append("<reset>");
                }

                builder.append("<#").append(hex).append('>');
                hasFormatting = true;
                index += 14;
                continue;
            }

            final String miniMessageTag = getMiniMessageTag(code);
            if (miniMessageTag == null) {
                builder.append(current);
                index++;
                continue;
            }

            if (code == 'r') {
                builder.append("<reset>");
                hasFormatting = false;
                index += 2;
                continue;
            }

            if (isColorCode(code) && hasFormatting) {
                builder.append("<reset>");
            }

            builder.append('<').append(miniMessageTag).append('>');
            hasFormatting = true;
            index += 2;
        }

        return builder.toString();
    }

    private static boolean isLegacyPrefix(final char character) {
        return character == '&' || character == '§';
    }

    private static boolean isColorCode(final char code) {
        return code >= '0' && code <= '9' || code >= 'a' && code <= 'f';
    }

    private static @Nullable String parseInlineHex(@NotNull final String input, final int startIndex) {
        if (startIndex + 6 > input.length()) {
            return null;
        }

        final String hex = input.substring(startIndex, startIndex + 6);
        return isHex(hex) ? hex : null;
    }

    private static @Nullable String parseSectionHex(@NotNull final String input, final int startIndex) {
        if (startIndex + 14 > input.length()) {
            return null;
        }

        final StringBuilder hex = new StringBuilder(6);
        for (int offset = startIndex + 2; offset < startIndex + 14; offset += 2) {
            if (!isLegacyPrefix(input.charAt(offset))) {
                return null;
            }

            final char character = input.charAt(offset + 1);
            if (!isHexCharacter(character)) {
                return null;
            }

            hex.append(character);
        }

        return hex.toString();
    }

    private static boolean isHex(@NotNull final String input) {
        for (int index = 0; index < input.length(); index++) {
            if (!isHexCharacter(input.charAt(index))) {
                return false;
            }
        }

        return true;
    }

    private static boolean isHexCharacter(final char character) {
        return character >= '0' && character <= '9'
                || character >= 'a' && character <= 'f'
                || character >= 'A' && character <= 'F';
    }

    private static @Nullable String getMiniMessageTag(final char code) {
        switch (code) {
            case '0':
                return "black";
            case '1':
                return "dark_blue";
            case '2':
                return "dark_green";
            case '3':
                return "dark_aqua";
            case '4':
                return "dark_red";
            case '5':
                return "dark_purple";
            case '6':
                return "gold";
            case '7':
                return "gray";
            case '8':
                return "dark_gray";
            case '9':
                return "blue";
            case 'a':
                return "green";
            case 'b':
                return "aqua";
            case 'c':
                return "red";
            case 'd':
                return "light_purple";
            case 'e':
                return "yellow";
            case 'f':
                return "white";
            case 'k':
                return "obfuscated";
            case 'l':
                return "bold";
            case 'm':
                return "strikethrough";
            case 'n':
                return "underlined";
            case 'o':
                return "italic";
            case 'r':
                return "reset";
            default:
                return null;
        }
    }
}