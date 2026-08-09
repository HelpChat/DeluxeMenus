package com.extendedclip.deluxemenus.placeholder.internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable snapshot of the menu a {@link PlaceholderContext} was created for.
 * <p>
 * Only plain values are kept here. Nothing in this class references the {@code MenuHolder}, the
 * {@code Menu} or any Bukkit object, which is what makes a context safe to hand to a task running
 * on a later tick.
 */
public final class MenuSnapshot {

    private final String name;
    private final String title;
    private final String type;
    private final int size;
    private final Integer itemCount;
    private final String openCommand;
    private final boolean hasPlaceholderPlayer;

    public MenuSnapshot(final @NotNull String name, final @NotNull String title,
                        final @NotNull String type, final int size, final @Nullable Integer itemCount,
                        final @NotNull String openCommand, final boolean hasPlaceholderPlayer) {
        this.name = name;
        this.title = title;
        this.type = type;
        this.size = size;
        this.itemCount = itemCount;
        this.openCommand = openCommand;
        this.hasPlaceholderPlayer = hasPlaceholderPlayer;
    }

    public @NotNull String name() {
        return name;
    }

    public @NotNull String title() {
        return title;
    }

    public @NotNull String type() {
        return type;
    }

    public int size() {
        return size;
    }

    /**
     * Null until the menu's active items are known - before that the count is left literal rather
     * than reported as 0.
     */
    public @Nullable Integer itemCount() {
        return itemCount;
    }

    public @NotNull String openCommand() {
        return openCommand;
    }

    public boolean hasPlaceholderPlayer() {
        return hasPlaceholderPlayer;
    }
}
