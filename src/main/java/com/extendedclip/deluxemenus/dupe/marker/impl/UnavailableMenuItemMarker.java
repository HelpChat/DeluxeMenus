package com.extendedclip.deluxemenus.dupe.marker.impl;

import com.extendedclip.deluxemenus.dupe.marker.ItemMarker;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class UnavailableMenuItemMarker implements ItemMarker {
    @Override
    public @NotNull ItemStack mark(@NotNull ItemStack itemStack) {
        return itemStack;
    }

    @Override
    public @NotNull ItemStack unmark(@NotNull ItemStack itemStack) {
        return itemStack;
    }

    @Override
    public boolean isMarked(@NotNull ItemStack itemStack) {
        return false;
    }
}
