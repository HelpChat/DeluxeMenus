package com.extendedclip.deluxemenus.dupe.marker.impl;

import com.extendedclip.deluxemenus.dupe.marker.ItemMarker;
import com.extendedclip.deluxemenus.nbt.NbtProvider;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class NMSMenuItemMarker implements ItemMarker {

    private final String mark;

    public NMSMenuItemMarker(@NotNull final String mark) {
        this.mark = mark;
    }

    @Override
    public @NotNull ItemStack mark(@NotNull ItemStack itemStack) {
        return NbtProvider.setBoolean(itemStack, mark, true);
    }

    @Override
    public @NotNull ItemStack unmark(@NotNull ItemStack itemStack) {
        return NbtProvider.removeKey(itemStack, mark);
    }

    @Override
    public boolean isMarked(@NotNull ItemStack itemStack) {
        return NbtProvider.hasKey(itemStack, mark);
    }
}
