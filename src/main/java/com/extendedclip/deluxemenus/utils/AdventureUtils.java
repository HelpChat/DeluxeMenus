package com.extendedclip.deluxemenus.utils;

import com.extendedclip.deluxemenus.DeluxeMenus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.command.CommandSender;

public final class AdventureUtils {
    private final static GsonComponentSerializer gson = GsonComponentSerializer.gson();

    private AdventureUtils() {
        throw new AssertionError("Util classes should not be initialized");
    }

    public static void sendJson(CommandSender sender, String json) {
        DeluxeMenus.getInstance().adventure().sender(sender).sendMessage(fromJson(json));
    }

    public static Component fromJson(String json) {
        return gson.deserialize(json);
    }
}