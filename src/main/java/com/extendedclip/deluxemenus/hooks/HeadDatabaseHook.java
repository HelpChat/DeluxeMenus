package com.extendedclip.deluxemenus.hooks;

import com.extendedclip.deluxemenus.DeluxeMenus;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class HeadDatabaseHook implements ItemHook {

  private final HeadDatabaseAPI api;

  public HeadDatabaseHook() {
    api = new HeadDatabaseAPI();
  }

  @Override
  public ItemStack getItem(@NotNull final String... arguments) {
    if (arguments.length == 0) {
      return DeluxeMenus.getInstance().getHead().clone();
    }

    try {
      final ItemStack item = api.getItemHead(arguments[0]);
      return item != null ? item : DeluxeMenus.getInstance().getHead().clone();
    } catch (NullPointerException exception) {
      DeluxeMenus.printStacktrace(
          "Something went wrong while trying to get head database head: " + arguments[0],
          exception
      );
    }

    return DeluxeMenus.getInstance().getHead().clone();
  }

  @Override
  public boolean itemMatchesIdentifiers(@NotNull ItemStack item, @NotNull String... arguments) {
    if (arguments.length == 0) {
      return false;
    }
    return arguments[0].equalsIgnoreCase(api.getItemID(item));
  }

  @Override
  public String getPrefix() {
    return "hdb-";
  }
}
