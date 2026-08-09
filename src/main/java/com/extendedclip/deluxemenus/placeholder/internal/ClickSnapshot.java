package com.extendedclip.deluxemenus.placeholder.internal;

import org.jetbrains.annotations.NotNull;

/**
 * Immutable snapshot of an {@code InventoryClickEvent}.
 * <p>
 * The event itself is never kept: it is read once, at click time, and discarded. That is what lets
 * a delayed action still report the click that started it.
 */
public final class ClickSnapshot {

    private final String type;
    private final String action;
    private final int slot;
    private final int rawSlot;
    private final int hotbarButton;
    private final String cursorMaterial;
    private final boolean left;
    private final boolean right;
    private final boolean shift;

    public ClickSnapshot(final @NotNull String type, final @NotNull String action, final int slot,
                         final int rawSlot, final int hotbarButton,
                         final @NotNull String cursorMaterial, final boolean left,
                         final boolean right, final boolean shift) {
        this.type = type;
        this.action = action;
        this.slot = slot;
        this.rawSlot = rawSlot;
        this.hotbarButton = hotbarButton;
        this.cursorMaterial = cursorMaterial;
        this.left = left;
        this.right = right;
        this.shift = shift;
    }

    public @NotNull String type() {
        return type;
    }

    public @NotNull String action() {
        return action;
    }

    public int slot() {
        return slot;
    }

    public int rawSlot() {
        return rawSlot;
    }

    public int hotbarButton() {
        return hotbarButton;
    }

    public @NotNull String cursorMaterial() {
        return cursorMaterial;
    }

    public boolean left() {
        return left;
    }

    public boolean right() {
        return right;
    }

    public boolean shift() {
        return shift;
    }
}
