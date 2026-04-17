package com.extendedclip.deluxemenus.hooks;

import com.extendedclip.deluxemenus.cache.SimpleCache;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

final class ItemStackCache implements SimpleCache {

    private final Map<String, ItemStack> cache = new ConcurrentHashMap<>();

    @Nullable
    ItemStack get(@NotNull final String key) {
        return cache.get(key);
    }

    @Nullable
    ItemStack computeIfAbsent(@NotNull final String key, @NotNull final Function<String, ItemStack> loader) {
        return cache.computeIfAbsent(key, loader);
    }

    void put(@NotNull final String key, @NotNull final ItemStack value) {
        cache.put(key, value);
    }

    void remove(@NotNull final String key) {
        cache.remove(key);
    }

    @Override
    public void clearCache() {
        cache.clear();
    }
}
