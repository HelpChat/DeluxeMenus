package com.extendedclip.deluxemenus.persistentmeta;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum DataAction {
    SET, REMOVE, ADD, SUBTRACT, SWITCH;

    final static Map<String, DataAction> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(Enum::name, Function.identity()));

    /**
     * Get a {@link DataAction} by its name.
     *
     * @param name The name of the action type.
     * @return The {@link DataAction} or null if it does not exist.
     */
    public static @Nullable DataAction getActionByName(@NotNull final String name) {
        return BY_NAME.get(name.toUpperCase(Locale.ROOT));
    }
}
