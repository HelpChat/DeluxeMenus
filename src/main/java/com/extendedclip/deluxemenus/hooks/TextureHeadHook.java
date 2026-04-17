package com.extendedclip.deluxemenus.hooks;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.cache.SimpleCache;
import com.extendedclip.deluxemenus.utils.SkullUtils;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TextureHeadHook implements ItemHook, SimpleCache {

    private final DeluxeMenus plugin;
    private final Map<String, ItemStack> cache = new ConcurrentHashMap<>();

    public TextureHeadHook(@NotNull final DeluxeMenus plugin) {
        this.plugin = plugin;
    }

    @Override
    public ItemStack getItem(@NotNull final String... arguments) {
        if (arguments.length == 0) {
            return plugin.getHead().clone();
        }

        try {
            return cache.computeIfAbsent(arguments[0], key -> SkullUtils.getSkullByBase64EncodedTextureUrl(plugin, SkullUtils.getEncoded(key))).clone();
        } catch (Exception exception) {
            plugin.printStacktrace("Something went wrong while trying to get texture head: " + arguments[0], exception);
        }

        return plugin.getHead().clone();
    }

  @Override
  public boolean itemMatchesIdentifiers(@NotNull ItemStack item, @NotNull String... arguments) {
    if (arguments.length == 0) {
      return false;
    }
    return arguments[0].equals(SkullUtils.getTextureFromSkull(plugin, item));
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
