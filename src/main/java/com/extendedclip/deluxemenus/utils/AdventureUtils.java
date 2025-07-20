package com.extendedclip.deluxemenus.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class AdventureUtils {
    private final static GsonComponentSerializer gson = GsonComponentSerializer.gson();
    private final static MiniMessage mm = MiniMessage.miniMessage();
    private final static LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
    private final static Pattern HEX_PATTERN = Pattern.compile(LegacyComponentSerializer.SECTION_CHAR + "x(?:" + LegacyComponentSerializer.SECTION_CHAR +"[a-fA-F0-9]){6}", Pattern.CASE_INSENSITIVE);

    private AdventureUtils() {
        throw new AssertionError("Util classes should not be initialized");
    }

    public static Component fromJson(String json) {
        return gson.deserialize(json);
    }

    public static Component fromString(String string, OfflinePlayer player) {
        if (string.contains(LegacyComponentSerializer.SECTION_CHAR + "")) {
            return legacy.deserialize(string);
        }
        return mm.deserialize(string, createPlaceholderAPITag(player));
    }

    @SuppressWarnings("deprecation")
    public static String kyorify(String string) {
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

    private static TagResolver createPlaceholderAPITag(OfflinePlayer player) {
        return TagResolver.resolver("papi", (argumentQueue, context) -> {
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

            final List<String> arguments = new ArrayList<>();
            if (append) {
                arguments.add(next);
            }

            while (argumentQueue.hasNext()) {
                arguments.add(argumentQueue.pop().value());
            }

            final var placeholder = String.join(":", arguments);
            if (placeholder.isBlank() || !placeholder.contains("_")) {
                return null;
            }

            String parsedPlaceholder = PlaceholderAPI.setPlaceholders(player, '%' + placeholder + '%');

            if (parsedPlaceholder.equals("%" + placeholder + '%')) {
                return null;
            }


            final var kyorifiedPlaceholder = kyorify(parsedPlaceholder);
            final var componentPlaceholder = mm.deserialize(kyorifiedPlaceholder);
            System.out.println(parsedPlaceholder + " | " + kyorifiedPlaceholder + " | " + componentPlaceholder);

            return inserting ? Tag.inserting(componentPlaceholder) : Tag.selfClosingInserting(componentPlaceholder);
        });
    }
}