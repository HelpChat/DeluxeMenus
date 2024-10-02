package com.extendedclip.deluxemenus.hooks;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.cache.SimpleCache;
import com.extendedclip.deluxemenus.utils.DebugLevel;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import io.github.projectunified.minelib.scheduler.global.GlobalScheduler;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
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
            CompletableFuture<ItemStack> future = new CompletableFuture<>();
            GlobalScheduler.get(DeluxeMenus.getInstance()).run(() -> {
                ItemStack item = MMOItems.plugin.getItem(itemType, splitArgs[1]);

                if (item == null) {
                    future.complete(new ItemStack(Material.STONE, 1));
                    return;
                }

                cache.put(arguments[0], item);
                future.complete(item);
            });
            mmoItem = future.get();
        } catch (InterruptedException | ExecutionException e) {
            DeluxeMenus.debug(DebugLevel.HIGHEST, Level.SEVERE, "Error getting MMOItem synchronously.");
        }

        return mmoItem == null ? new ItemStack(Material.STONE, 1) : mmoItem;
    }

    @Override
    public String getPrefix() {
        return "mmoitems-";
    }

    @Override
    public void clearCache() {
        cache.clear();
    }
}
