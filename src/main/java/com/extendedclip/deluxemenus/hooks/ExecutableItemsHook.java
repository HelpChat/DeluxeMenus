package com.extendedclip.deluxemenus.hooks;

import com.extendedclip.deluxemenus.cache.SimpleCache;
import com.ssomar.score.api.executableitems.ExecutableItemsAPI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.ssomar.score.api.executableitems.config.ExecutableItemInterface;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ExecutableItemsHook implements ItemHook, SimpleCache {

  private final Map<String, ItemStack> cache = new ConcurrentHashMap<>();

  @Override
  public ItemStack getItem(@NotNull String... arguments) {
    if (arguments.length == 0) {
      return new ItemStack(Material.STONE);
    }

    final ItemStack item = cache.computeIfAbsent(arguments[0], (id) -> {
      final ItemStack result = ExecutableItemsAPI.getExecutableItemsManager()
          .getExecutableItem(arguments[0])
          .map(x -> x.buildItem(1, Optional.empty()))
          .orElse(null);

      return (result == null) ? null : result.clone();
    });

    return (item == null) ? new ItemStack(Material.STONE) : item.clone();
  }

  @Override
  public boolean itemMatchesIdentifiers(@NotNull ItemStack item, @NotNull String... arguments) {
    if (arguments.length == 0) {
      return false;
    }
    ExecutableItemInterface fromId = ExecutableItemsAPI.getExecutableItemsManager().getExecutableItem(arguments[0]).orElse(null);
    ExecutableItemInterface fromItem = ExecutableItemsAPI.getExecutableItemsManager().getExecutableItem(item).orElse(null);
    return fromItem != null && fromItem.equals(fromId);
  }

  @Override
  public String getPrefix() {
    return "executableitems-";
  }

  @Override
  public void clearCache() {
    this.cache.clear();
  }

}
