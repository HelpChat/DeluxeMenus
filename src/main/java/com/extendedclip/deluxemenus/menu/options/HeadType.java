package com.extendedclip.deluxemenus.menu.options;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;

import static com.extendedclip.deluxemenus.utils.Constants.*;

public enum HeadType {

    /**
     * @see com.extendedclip.deluxemenus.hooks.NamedHeadHook
     */
    NAMED(NAMED_HEAD_TYPE, NAMED_HEAD_PREFIX),
    /**
     * @see com.extendedclip.deluxemenus.hooks.TextureHeadHook
     */
    TEXTURE(TEXTURE_HEAD_TYPE, TEXTURE_HEAD_PREFIX),
    /**
     * @see com.extendedclip.deluxemenus.hooks.HeadDatabaseHook
     */
    HDB(HDB_HEAD_TYPE, HDB_HEAD_PREFIX),
    /**
     * @see com.extendedclip.deluxemenus.hooks.BaseHeadHook
     */
    BASE64(BASE64_HEAD_TYPE, BASE64_HEAD_PREFIX);

    private static final HeadType[] VALUES = values();
    private final String hookName;
    private final String prefix;

    HeadType(String hookName, String prefix) {
        this.hookName = hookName;
        this.prefix = prefix;
    }

    public String getHookName() {
        return hookName;
    }

    public String getPrefix() {
        return prefix;
    }

    /**
     * Gets the head type from a string's prefix. This is case-insensitive.
     * This checks if a string starts with a head type's prefix.
     * If no head type is found, {@link Optional#empty()} is returned.
     * If multiple head types are found, the first one is returned.
     * If you want to get the head type by exact prefix, use {@link #getHeadType(String)} instead.
     *
     * @param string The string to parse
     * @return The head type, if found
     */
    public static Optional<HeadType> parseHeadType(@NotNull final String string) {
        for (HeadType type : VALUES) {
            if (string.startsWith(type.getPrefix().toLowerCase(Locale.ROOT))) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    /**
     * Gets the head type from a string. This is case-insensitive.
     * This checks if the string is exactly equal to a head type prefix.
     * If no head type is found, {@link Optional#empty()} is returned.
     * If multiple head types are found, the first one is returned.
     * If you want to get the head type by prefix, use {@link #parseHeadType(String)} instead.
     * @param string The string to parse
     * @return The head type, if found
     */
    public static Optional<HeadType> getHeadType(@NotNull final String string) {
        for (HeadType type : VALUES) {
            if (type.getPrefix().equalsIgnoreCase(string)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

}
