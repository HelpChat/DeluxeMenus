package com.extendedclip.deluxemenus.editor;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class MenuEditorHolder implements InventoryHolder {

    public enum View {
        PREVIEW,
        SLOT
    }

    private final String menuName;
    private final View view;
    private final int slot;
    private Inventory inventory;

    public MenuEditorHolder(final @NotNull String menuName) {
        this(menuName, View.PREVIEW, -1);
    }

    public MenuEditorHolder(final @NotNull String menuName, final @NotNull View view, final int slot) {
        this.menuName = menuName;
        this.view = view;
        this.slot = slot;
    }

    public @NotNull String menuName() {
        return this.menuName;
    }

    public @NotNull View view() {
        return this.view;
    }

    public int slot() {
        return this.slot;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

    public void setInventory(final @NotNull Inventory inventory) {
        this.inventory = inventory;
    }
}
