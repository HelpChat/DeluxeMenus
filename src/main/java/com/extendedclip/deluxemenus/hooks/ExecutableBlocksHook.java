package com.extendedclip.deluxemenus.hooks;

import com.extendedclip.deluxemenus.cache.SimpleCache;
import com.ssomar.score.api.ExecutableBlocksAPI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ExecutableBlocksHook implements ItemHook, SimpleCache {

  private final Map<String, ItemStack> cache = new ConcurrentHashMap<>();

  @SuppressWarnings("deprecation")
  @Override
  public ItemStack getItem(@NotNull String... arguments) {
    if (arguments.length == 0) {
      return new ItemStack(Material.STONE);
    }

    final ItemStack item = cache.computeIfAbsent(arguments[0], (id) -> {
      final ItemStack result = ExecutableBlocksAPI.getExecutableBlock(arguments[0]);

      return (result == null) ? null : result.clone();
    });

    return (item == null) ? new ItemStack(Material.STONE) : item.clone();
  }

  @Override
  public String getPrefix() {
    return "executableblocks-";
  }

  @Override
  public void clearCache() {
    this.cache.clear();
  }

}
