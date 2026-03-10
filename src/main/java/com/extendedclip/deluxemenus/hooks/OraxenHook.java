package com.extendedclip.deluxemenus.hooks;

import com.extendedclip.deluxemenus.cache.SimpleCache;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OraxenHook implements ItemHook, SimpleCache {

    private final Map<String, ItemStack> cache = new ConcurrentHashMap<>();

    @Override
    public ItemStack getItem(@NotNull String... arguments) {
        if (arguments.length == 0) {
            return new ItemStack(Material.STONE);
        }

        final ItemStack item = cache.computeIfAbsent(arguments[0], (id) -> {
            final ItemBuilder builder = OraxenItems.getItemById(id);
            return (builder == null) ? null : builder.build().clone();
        });

        return (item == null) ? new ItemStack(Material.STONE) : item.clone();
    }

    @Override
    public boolean itemMatchesIdentifiers(@NotNull ItemStack item, @NotNull String... arguments) {
        if (arguments.length == 0) {
            return false;
        }
        return arguments[0].equalsIgnoreCase(OraxenItems.getIdByItem(item));
    }

    @Override
    public String getPrefix() {
        return "oraxen-";
    }

    @Override
    public void clearCache() {
        cache.clear();
    }
}
