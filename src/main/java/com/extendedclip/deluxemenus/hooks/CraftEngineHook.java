package com.extendedclip.deluxemenus.hooks;

import com.extendedclip.deluxemenus.cache.SimpleCache;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptors;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.item.BuildableItem;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CraftEngineHook implements ItemHook, SimpleCache {
    private static final ItemStack EMPTY = new ItemStack(Material.STONE);
    private final Map<String, ItemStack> cache = new ConcurrentHashMap<>();

    @Override
    public void clearCache() {
        cache.clear();
    }

    @Override
    public ItemStack getItem(@NotNull String... arguments) {
        if (arguments.length == 0) return EMPTY.clone();
        return cache.computeIfAbsent(arguments[0], namespaceId ->
                Optional.ofNullable(CraftEngineItems.byId(Key.of(namespaceId)))
                        .map(BuildableItem::buildItemStack)
                        .orElse(EMPTY)
        ).clone();
    }

    @Override
    public ItemStack getItem(@NotNull Player holder, @NotNull String... arguments) {
        if (arguments.length == 0) return EMPTY.clone();
        Key id = Key.of(arguments[0]);
        return Optional.ofNullable(CraftEngineItems.byId(id))
                .map(item -> item.buildItemStack(BukkitAdaptors.adapt(holder)))
                .orElse(EMPTY.clone());
    }

    @Override
    public boolean itemMatchesIdentifiers(@NotNull ItemStack item, @NotNull String... arguments) {
        if (arguments.length == 0) return false;
        CustomItem<ItemStack> customItem = CraftEngineItems.byItemStack(item);
        return customItem != null && customItem.id().equals(Key.of(arguments[0]));
    }

    @Override
    public String getPrefix() {
        return "craftengine-";
    }
}
