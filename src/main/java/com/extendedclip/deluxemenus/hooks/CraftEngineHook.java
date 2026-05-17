package com.extendedclip.deluxemenus.hooks;

import com.extendedclip.deluxemenus.cache.SimpleCache;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CraftEngineHook implements ItemHook, SimpleCache {
    private static final ItemStack EMPTY = new ItemStack(Material.STONE);
    private final Map<String, ItemStack> cache = new ConcurrentHashMap<>();

    @Override
    public void clearCache() {
        this.cache.clear();
    }

    @Override
    public ItemStack getItem(@NotNull String... arguments) {
        if (arguments.length == 0) return EMPTY.clone();
        String namespaceId = arguments[0];
        ItemStack cached = this.cache.get(namespaceId);
        if (cached != null) return cached.clone();
        BukkitItemDefinition definition = CraftEngineItems.byId(namespaceId);
        if (definition == null) return EMPTY.clone();
        ItemStack result = definition.buildBukkitItem();
        this.cache.put(namespaceId, result);
        return result.clone();
    }

    @Override
    public ItemStack getItem(@NotNull Player holder, @NotNull String... arguments) {
        if (arguments.length == 0) return EMPTY.clone();
        BukkitItemDefinition definition = CraftEngineItems.byId(arguments[0]);
        if (definition == null) return EMPTY.clone();
        return definition.buildBukkitItem(holder);
    }

    @Override
    public boolean itemMatchesIdentifiers(@NotNull ItemStack item, @NotNull String... arguments) {
        if (arguments.length == 0) return false;
        BukkitItemDefinition definition = CraftEngineItems.byItemStack(item);
        return definition != null && definition.id().asString().equals(arguments[0]);
    }

    @Override
    public String getPrefix() {
        return "craftengine-";
    }
}
