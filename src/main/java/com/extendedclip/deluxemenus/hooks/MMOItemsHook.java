package com.extendedclip.deluxemenus.hooks;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.cache.SimpleCache;
import com.extendedclip.deluxemenus.utils.DebugLevel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MMOItemsHook implements ItemHook, SimpleCache {

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

        String[] splitArgs = arguments[0].split(":");
        if (splitArgs.length != 2) {
            return new ItemStack(Material.STONE, 1);
        }

        final Type itemType = MMOItems.plugin.getTypes().get(splitArgs[0]);
        if (itemType == null) {
            return new ItemStack(Material.STONE, 1);
        }

        ItemStack mmoItem = null;
        try {
            mmoItem = Bukkit.getScheduler().callSyncMethod(DeluxeMenus.getInstance(), () -> {
                ItemStack item = MMOItems.plugin.getItem(itemType, splitArgs[1]);

                if (item == null) {
                    return new ItemStack(Material.STONE, 1);
                }

                cache.put(arguments[0], item);

                return item;
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            DeluxeMenus.debug(DebugLevel.HIGHEST, Level.SEVERE, "Error getting MMOItem synchronously.");
        }

        if (mmoItem == null) {
            return new ItemStack(Material.STONE, 1);
        } else {
            return mmoItem;
        }
    }

    @Override
    public void clearCache() {
        cache.clear();
    }
}
