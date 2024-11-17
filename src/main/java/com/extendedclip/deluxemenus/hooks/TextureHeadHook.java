package com.extendedclip.deluxemenus.hooks;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.cache.SimpleCache;
import com.extendedclip.deluxemenus.utils.SkullUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TextureHeadHook implements ItemHook, SimpleCache {

  private final Map<String, ItemStack> cache = new ConcurrentHashMap<>();

  @Override
  public ItemStack getItem(@NotNull final String... arguments) {
    if (arguments.length == 0) {
      return DeluxeMenus.getInstance().getHead().clone();
    }

    try {
      return cache.computeIfAbsent(arguments[0], key -> SkullUtils.getSkullByBase64EncodedTextureUrl(SkullUtils.getEncoded(key))).clone();
    } catch (Exception exception) {
      DeluxeMenus.printStacktrace(
          "Something went wrong while trying to get texture head: " + arguments[0],
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
    return arguments[0].equals(SkullUtils.getTextureFromSkull(item));
  }

  @Override
  public String getPrefix() {
    return "texture-";
  }

  @Override
  public void clearCache() {
    cache.clear();
  }
}
