package com.extendedclip.deluxemenus.utils;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum DebugLevel {
    LOWEST(0, "LOWEST"),
    LOW(1, "LOW"),
    MEDIUM(2, "MEDIUM"),
    HIGH(3, "HIGH"),
    HIGHEST(4, "HIGHEST");

    private final String[] names;
    private final int priority;

    private DebugLevel(final int priority, @NotNull final String... names) {
        this.priority = priority;
        this.names = names;
    }

    public int getPriority() {
        return priority;
    }

    private static final Map<String, DebugLevel> LEVELS = Arrays.stream(values())
        .flatMap(level -> Arrays.stream(level.names).map(name -> Map.entry(name.toLowerCase(Locale.ROOT), level)))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    public static @Nullable DebugLevel getByName(@NotNull final String name) {
        return LEVELS.get(name.toLowerCase(Locale.ROOT));
    }
}
