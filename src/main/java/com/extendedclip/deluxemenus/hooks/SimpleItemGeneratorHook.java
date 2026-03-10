package com.extendedclip.deluxemenus.hooks;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.cache.SimpleCache;
import com.extendedclip.deluxemenus.utils.DebugLevel;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ua.valeriishymchuk.simpleitemgenerator.api.SimpleItemGenerator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class SimpleItemGeneratorHook implements ItemHook, SimpleCache {


    private final Map<String, ItemStack> cache = new ConcurrentHashMap<>();
    private final DeluxeMenus plugin;

    public SimpleItemGeneratorHook(DeluxeMenus plugin) {
        this.plugin = plugin;
    }

    @Override
    public void clearCache() {
        cache.clear();
    }

    @Override
    public ItemStack getItem(@NotNull Player holder, @NotNull String... arguments) {
        if (arguments.length == 0) {
            return errorItem("Item arguments are absent.");
        }

        final ItemStack item = cache.computeIfAbsent(arguments[0], id -> {
            return SimpleItemGenerator.get().bakeItem(id, holder).orElse(null);
        });
        return item == null? errorItem("Item %s wasn't found by SimpleItemGenerator.", arguments[0]) : item.clone();
    }

    private ItemStack errorItem(String error, Object... args) {
        final ItemStack item = new ItemStack(Material.STONE, 1);
        plugin.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                String.format(error, args)
        );
        return item;
    }

    @Override
    public boolean itemMatchesIdentifiers(@NotNull ItemStack item, @NotNull String... arguments) {
        if (arguments.length == 0) {
            return false;
        }
        return SimpleItemGenerator.get().getCustomItemKey(item)
                .map(s -> arguments[0].equals(s))
                .orElse(false);
    }

    @Override
    public String getPrefix() {
        return "simpleitemgenerator-";
    }
}
