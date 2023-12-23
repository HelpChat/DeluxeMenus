package com.extendedclip.deluxemenus.dupe.marker;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ItemMarker {
    @NotNull ItemStack mark(@NotNull ItemStack itemStack);
    @NotNull ItemStack unmark(@NotNull ItemStack itemStack);
    boolean isMarked(@NotNull ItemStack itemStack);
}
