package com.extendedclip.deluxemenus.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SoundUtils {

    /**
     * Maps the legacy {@code Sound} enum constant names menus are configured with onto the
     * registry. The mapping is derived from the registry rather than by replacing {@code _}
     * with {@code .}, because that naive conversion is wrong for keys whose segments contain
     * underscores: {@code BLOCK_NOTE_BLOCK_HARP} is {@code block.note_block.harp}.
     * <p>
     * Uses {@code Registry.SOUNDS} rather than {@code Registry.SOUND_EVENT}: the latter does
     * not exist on 1.20.6, the minimum supported version. On current versions they are the
     * same registry instance.
     */
    private static final class Lookup {
        static final Map<String, Sound> BY_LEGACY_NAME = build();

        private static Map<String, Sound> build() {
            final Map<String, Sound> map = new HashMap<>();
            for (final Sound sound : Registry.SOUNDS) {
                final NamespacedKey key = Registry.SOUNDS.getKey(sound);
                if (key == null) continue;
                map.put(toLegacyName(key.value()), sound);
            }
            return map;
        }
    }

    private static String toLegacyName(@NotNull final String keyValue) {
        return keyValue.toUpperCase(Locale.ROOT).replace('.', '_');
    }

    /**
     * Resolves a sound from either a namespaced key ({@code entity.player.levelup},
     * {@code minecraft:entity.player.levelup}) or a legacy enum constant name
     * ({@code ENTITY_PLAYER_LEVELUP}).
     *
     * @return the sound, or {@code null} if no sound matches
     */
    public static @Nullable Sound getSound(@NotNull final String name) {
        final NamespacedKey key = NamespacedKey.fromString(name.toLowerCase(Locale.ROOT));
        if (key != null) {
            final Sound sound = Registry.SOUNDS.get(key);
            if (sound != null) {
                return sound;
            }
        }

        return Lookup.BY_LEGACY_NAME.get(toLegacyName(name));
    }
}
