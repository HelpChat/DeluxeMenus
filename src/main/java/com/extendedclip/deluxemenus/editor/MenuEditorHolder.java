package com.extendedclip.deluxemenus.editor;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class MenuEditorHolder implements InventoryHolder {

    private final String menuName;
    private Inventory inventory;

    public MenuEditorHolder(final @NotNull String menuName) {
        this.menuName = menuName;
    }

    public @NotNull String menuName() {
        return this.menuName;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

    public void setInventory(final @NotNull Inventory inventory) {
        this.inventory = inventory;
    }
}
