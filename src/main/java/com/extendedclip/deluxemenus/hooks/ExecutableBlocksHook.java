package com.extendedclip.deluxemenus.hooks;

import com.extendedclip.deluxemenus.cache.SimpleCache;
import com.ssomar.score.api.executableblocks.ExecutableBlocksAPI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.ssomar.score.api.executableblocks.config.ExecutableBlockInterface;
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
      final Optional<ExecutableBlockInterface> result = ExecutableBlocksAPI.getExecutableBlocksManager().getExecutableBlock(arguments[0]);

      return result.map(executableBlockInterface -> executableBlockInterface.buildItem(1, Optional.empty())).orElse(null);
    });

    return (item == null) ? new ItemStack(Material.STONE) : item.clone();
  }

  @Override
  public boolean itemMatchesIdentifiers(@NotNull ItemStack item, @NotNull String... arguments) {
    return false;
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
