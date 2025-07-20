package com.extendedclip.deluxemenus.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;

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

    @SuppressWarnings("deprecation")
    public static Component fromString(String string) {
        // support strings with § (placeholders parsing colors or users using it directly
        if (string.contains(LegacyComponentSerializer.SECTION_CHAR + "")) {
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
            string = string.replace("<reset>","<bold:false><italic:false><underlined:false><strikethrough:false><obfuscated:false><white>");
        }
        return mm.deserialize(string);
    }
}