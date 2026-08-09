package com.extendedclip.deluxemenus.placeholder.internal;

import org.jetbrains.annotations.NotNull;

/**
 * Immutable snapshot of the player actually viewing the menu.
 * <p>
 * This is not the same player PlaceholderAPI parses against: when a menu is opened with
 * {@code -p:<target>}, PAPI parses against the target, so {@code %player_name%} returns the target
 * while these values keep pointing at the viewer.
 */
public final class ViewerSnapshot {

    private final String name;
    private final String uuid;
    private final String displayName;

    public ViewerSnapshot(final @NotNull String name, final @NotNull String uuid,
                          final @NotNull String displayName) {
        this.name = name;
        this.uuid = uuid;
        this.displayName = displayName;
    }

    public @NotNull String name() {
        return name;
    }

    public @NotNull String uuid() {
        return uuid;
    }

    public @NotNull String displayName() {
        return displayName;
    }
}
