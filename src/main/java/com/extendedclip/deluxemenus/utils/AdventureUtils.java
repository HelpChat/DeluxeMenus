package com.extendedclip.deluxemenus.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class AdventureUtils {
    private final static GsonComponentSerializer gson = GsonComponentSerializer.gson();

    private AdventureUtils() {
        throw new AssertionError("Util classes should not be initialized");
    }

    public static void sendJson(@NotNull final CommandSender sender, @NotNull final String json) {
        sender.sendMessage(fromJson(json));
    }

    public static Component fromJson(String json) {
        return gson.deserialize(json);
    }

    /**
     * Sends a message to every online player. The console is deliberately not included.
     */
    public static void broadcast(@NotNull final Component message) {
        Audience.audience(Bukkit.getOnlinePlayers()).sendMessage(message);
    }
}
