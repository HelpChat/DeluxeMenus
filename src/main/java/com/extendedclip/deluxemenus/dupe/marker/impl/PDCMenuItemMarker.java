package com.extendedclip.deluxemenus.dupe.marker.impl;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.dupe.marker.ItemMarker;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class PDCMenuItemMarker implements ItemMarker {

    private final NamespacedKey mark;

    public PDCMenuItemMarker(@NotNull final DeluxeMenus plugin, @NotNull final String mark) {
        this.mark = new NamespacedKey(plugin, mark);
    }

    @Override
    public @NotNull ItemStack mark(@NotNull ItemStack itemStack) {
        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return itemStack;
        }

        final PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(
                mark,
                PersistentDataType.BYTE,
                (byte) 1
        );

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Override
    public @NotNull ItemStack unmark(@NotNull ItemStack itemStack) {
        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return itemStack;
        }

        final PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.remove(mark);

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Override
    public boolean isMarked(@NotNull ItemStack itemStack) {
        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return false;
        }

        final PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        return container.has(mark, PersistentDataType.BYTE);
    }
}
