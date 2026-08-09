package com.extendedclip.deluxemenus.placeholder.internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable snapshot of the menu item a {@link PlaceholderContext} was created for.
 * <p>
 * Config-side values ({@link #slot()}, {@link #priority()}, {@link #update()}) are known as soon as
 * the {@code MenuItem} is known. The built-stack values ({@link #material()}, {@link #amount()},
 * {@link #modelData()}, {@link #displayName()}) only exist once the {@code ItemStack} has been
 * built, so they are filled in later through {@link #withStack(String, int, String, String)} and are
 * null until then.
 */
public final class ItemSnapshot {

    private final int slot;
    private final int priority;
    private final boolean update;

    private final String material;
    private final Integer amount;
    private final String modelData;
    private final String displayName;

    public ItemSnapshot(final int slot, final int priority, final boolean update) {
        this(slot, priority, update, null, null, null, null);
    }

    private ItemSnapshot(final int slot, final int priority, final boolean update,
                         final @Nullable String material, final @Nullable Integer amount,
                         final @Nullable String modelData, final @Nullable String displayName) {
        this.slot = slot;
        this.priority = priority;
        this.update = update;
        this.material = material;
        this.amount = amount;
        this.modelData = modelData;
        this.displayName = displayName;
    }

    /**
     * Returns a copy of this snapshot with the built-stack values filled in.
     */
    public @NotNull ItemSnapshot withStack(final @Nullable String material, final int amount,
                                           final @Nullable String modelData,
                                           final @Nullable String displayName) {
        return new ItemSnapshot(slot, priority, update, material, amount, modelData, displayName);
    }

    public int slot() {
        return slot;
    }

    public int priority() {
        return priority;
    }

    public boolean update() {
        return update;
    }

    public @Nullable String material() {
        return material;
    }

    public @Nullable Integer amount() {
        return amount;
    }

    public @Nullable String modelData() {
        return modelData;
    }

    public @Nullable String displayName() {
        return displayName;
    }
}
