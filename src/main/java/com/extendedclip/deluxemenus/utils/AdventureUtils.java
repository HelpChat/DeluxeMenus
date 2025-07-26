package com.extendedclip.deluxemenus.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class AdventureUtils {
    private final static GsonComponentSerializer gson = GsonComponentSerializer.gson();
    private final static MiniMessage mm = MiniMessage.miniMessage();
    private final static Pattern HEX_PATTERN = Pattern.compile(LegacyComponentSerializer.SECTION_CHAR + "x(?:" + LegacyComponentSerializer.SECTION_CHAR +"[a-fA-F0-9]){6}", Pattern.CASE_INSENSITIVE);

    private AdventureUtils() {
        throw new AssertionError("Util classes should not be initialized");
    }

    public static Component fromJson(String json) {
        return gson.deserialize(json);
    }

    public static Component fromString(String string, TagResolver... tagResolvers) {
        return mm.deserialize(string, tagResolvers);
    }

    public static TagResolver createArgumentTagResolver(final Map<String, String> menuArguments) {
        return TagResolver.resolver("arg", (argumentQueue, context) -> {
            if (menuArguments.isEmpty()) {
                return null;
            }

            final Pair<String, Boolean> tagArguments = buildTagArguments(argumentQueue);
            if (tagArguments == null) {
                return null;
            }

            final String argument = tagArguments.getKey();
            final boolean inserting = tagArguments.getValue() != null || tagArguments.getValue();

            if (argument.isBlank() || !menuArguments.containsKey(argument)) {
                return null;
            }

            final var componentPlaceholder = mm.deserialize(menuArguments.get(argument));
            return inserting ? Tag.inserting(componentPlaceholder) : Tag.selfClosingInserting(componentPlaceholder);
        });
    }

    public static TagResolver createPlaceholderAPITagResolver(OfflinePlayer player) {
        return TagResolver.resolver("papi", (argumentQueue, context) -> {
            final Pair<String, Boolean> tagArguments = buildTagArguments(argumentQueue);
            if (tagArguments == null) {
                return null;
            }

            final String placeholder = tagArguments.getKey();
            final boolean inserting = tagArguments.getValue() != null || tagArguments.getValue();

            if (placeholder.isBlank() || !placeholder.contains("_")) {
                return null;
            }

            String parsedPlaceholder = PlaceholderAPI.setPlaceholders(player, '%' + placeholder + '%');

            if (parsedPlaceholder.equals("%" + placeholder + '%')) {
                return null;
            }

            final var kyorifiedPlaceholder = kyorify(parsedPlaceholder);
            final var componentPlaceholder = mm.deserialize(kyorifiedPlaceholder);

            return inserting ? Tag.inserting(componentPlaceholder) : Tag.selfClosingInserting(componentPlaceholder);
        });
    }

    private static Pair<String, Boolean> buildTagArguments(@NotNull final ArgumentQueue argumentQueue) {
        if (!argumentQueue.hasNext()) {
            return null;
        }

        final String next = argumentQueue.pop().value();
        final boolean inserting;
        final boolean append;
        switch (next.toLowerCase(Locale.ROOT)) {
            case "closing":
                inserting = false;
                append = false;
                break;
            case "inserting":
                inserting = true;
                append = false;
                break;
            default:
                inserting = false;
                append = true;
                break;
        }

        final List<String> parts = new ArrayList<>();
        if (append) {
            parts.add(next);
        }

        while (argumentQueue.hasNext()) {
            parts.add(argumentQueue.pop().value());
        }

        final var argument = String.join(":", parts);

        return Pair.of(argument, inserting);
    }


    @SuppressWarnings("deprecation")
    private static String kyorify(String string) {
        string = HEX_PATTERN.matcher(string).replaceAll(result -> {
            String rgb = result.group();
            StringBuilder output = new StringBuilder();
            for (int i = 3; i < rgb.length(); i+=2) {
                output.append(rgb.charAt(i));
            }
            return "<color:#" + output + ">";
        });

        for (ChatColor c : ChatColor.values()) {
            String color = c.name().toLowerCase();
            if (string.equals("underline")) color+="d";
            string = string.replace(c.toString(), "<" + color + ">");
        }
        string = string.replace("&u","<rainbow>");
        return string.replace("<reset>","<bold:false><italic:false><underlined:false><strikethrough:false><obfuscated:false><white>");
    }
}