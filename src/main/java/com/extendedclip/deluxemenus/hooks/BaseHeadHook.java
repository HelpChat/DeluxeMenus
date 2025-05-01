package com.extendedclip.deluxemenus.hooks;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.cache.SimpleCache;
import com.extendedclip.deluxemenus.utils.SkullUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BaseHeadHook implements ItemHook, SimpleCache {

  private final DeluxeMenus plugin;
  private final Map<String, ItemStack> cache = new ConcurrentHashMap<>();

    public BaseHeadHook(@NotNull final DeluxeMenus plugin) {
        this.plugin = plugin;
    }

  @Override
  public ItemStack getItem(@NotNull final String... arguments) {
    if (arguments.length == 0) {
      return plugin.getHead().clone();
    }

    try {
      return cache.computeIfAbsent(arguments[0], value -> SkullUtils.getSkullByBase64EncodedTextureUrl(plugin, value)).clone();
    } catch (Exception exception) {
      plugin.printStacktrace(
          "Something went wrong while trying to get base64 head: " + arguments[0],
          exception
      );
    }

    return plugin.getHead().clone();
  }

  @Override
  public boolean itemMatchesIdentifiers(@NotNull ItemStack item, @NotNull String... arguments) {
    if (arguments.length == 0) {
      return false;
    }
    String itemTexture = SkullUtils.getTextureFromSkull(plugin, item);
    String texture = SkullUtils.decodeSkinUrl(arguments[0]);
    if (itemTexture == null || texture == null) return false;

    texture = texture.substring("https://textures.minecraft.net/texture/".length()-1);
    return texture.equals(itemTexture);
  }

  @Override
  public String getPrefix() {
    return "basehead-";
  }

  @Override
  public void clearCache() {
    cache.clear();
  }
}
