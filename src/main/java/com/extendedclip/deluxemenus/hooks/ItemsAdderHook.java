package com.extendedclip.deluxemenus.hooks;

import com.extendedclip.deluxemenus.cache.SimpleCache;
import dev.lone.itemsadder.api.CustomStack;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemsAdderHook implements ItemHook, SimpleCache {

    private final Map<String, ItemStack> cache = new ConcurrentHashMap<>();

    @Override
    public ItemStack getItem(@NotNull final String... arguments) {
        if (arguments.length == 0) {
            return new ItemStack(Material.STONE, 1);
        }

        final ItemStack cached = cache.get(arguments[0]);
        if (cached != null) {
            return cached.clone();
        }

        final CustomStack customStack = CustomStack.getInstance(arguments[0]);

        if (customStack == null) {
            return new ItemStack(Material.STONE, 1);
        }

        final ItemStack item = customStack.getItemStack().clone();
        cache.put(arguments[0], item);

        return item.clone();
    }

    @Override
    public boolean itemMatchesIdentifiers(@NotNull ItemStack item, @NotNull String... arguments) {
        if (arguments.length == 0) {
            return false;
        }
        CustomStack stack = CustomStack.byItemStack(item);
        return stack != null && stack.getId().equalsIgnoreCase(arguments[0]);
    }

    @Override
    public String getPrefix() {
        return "itemsadder-";
    }

    @Override
    public void clearCache() {
        cache.clear();
    }
}
