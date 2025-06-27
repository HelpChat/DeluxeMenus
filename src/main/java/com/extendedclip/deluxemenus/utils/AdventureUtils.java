package com.extendedclip.deluxemenus.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class AdventureUtils {
    private final static GsonComponentSerializer gson = GsonComponentSerializer.gson();
    private final static MiniMessage mm = MiniMessage.miniMessage();
    private final static LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();

    private AdventureUtils() {
        throw new AssertionError("Util classes should not be initialized");
    }

    public static Component fromJson(String json) {
        return gson.deserialize(json);
    }

    public static Component fromString(String string) {
        // support strings with § (placeholders parsing colors or users using it directly
        if (string.contains(LegacyComponentSerializer.SECTION_CHAR + "")) {
            string = mm.serialize(legacy.deserialize(string));
        }
        return mm.deserialize(string);
    }
}