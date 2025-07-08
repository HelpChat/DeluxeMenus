package com.extendedclip.deluxemenus.hooks;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ItemHook {

  default ItemStack getItem(@NotNull final String... arguments) {
    return new ItemStack(Material.STONE);
  }

  default ItemStack getItem(@NotNull final Player holder, @NotNull final String... arguments) {
    return getItem(arguments);
  }

  boolean itemMatchesIdentifiers(@NotNull ItemStack item, @NotNull final String... arguments);

  String getPrefix();

}
