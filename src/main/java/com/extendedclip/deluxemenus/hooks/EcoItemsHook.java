package com.extendedclip.deluxemenus.hooks;

import com.extendedclip.deluxemenus.cache.SimpleCache;
import com.willfp.ecoitems.items.EcoItem;
import com.willfp.ecoitems.items.EcoItemFinder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EcoItemsHook implements ItemHook, SimpleCache {

    private final Map<String, ItemStack> cache = new ConcurrentHashMap<>();

    private final Object ecoItems;
    private final Method getByID;

    private final Method getItemStack;
    private final Method getID;

    private final Object ecoItemFinder;
    private final Method find;

    public EcoItemsHook() {
        try {
            // this is only way of doing it, because EcoItems is build lets say differently and DM cannot be compiled if accessed using EI as a dependency :|
            Class<?> ecoItemsClass = Class.forName("com.willfp.ecoitems.items.EcoItems");

            Class<?> ecoItemClass = Class.forName("com.willfp.ecoitems.items.EcoItem");

            Class<?> ecoItemFinderClass = Class.forName("com.willfp.ecoitems.items.EcoItemFinder");

            ecoItems = ecoItemsClass.getField("INSTANCE").get(null);
            getByID = ecoItemsClass.getMethod("getByID", String.class);

            getItemStack = ecoItemClass.getMethod("getItemStack");
            getID = ecoItemClass.getMethod("getID");

            ecoItemFinder = ecoItemFinderClass.getField("INSTANCE").get(null);
            find = ecoItemFinderClass.getMethod("find", ItemStack.class);

        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to hook into EcoItems", e);
        }
    }


    @Override
    public ItemStack getItem(@NotNull String... arguments) {
        if (arguments.length == 0) {
            return new ItemStack(Material.STONE);
        }

        ItemStack cached = cache.get(arguments[0]);
        if (cached != null) {
            return cached.clone();
        }

        try {
            Object ecoItem = getByID.invoke(ecoItems, arguments[0]);

            if (ecoItem == null) {
                return new ItemStack(Material.STONE);
            }

            ItemStack item = ((ItemStack) getItemStack.invoke(ecoItem)).clone();

            cache.put(arguments[0], item);

            return item.clone();

        } catch (ReflectiveOperationException e) {
            Bukkit.getLogger().warning("Failed to get EcoItems item '" + arguments[0] + "': " + e.getMessage());
            return new ItemStack(Material.STONE);
        }
    }

    @Override
    public boolean itemMatchesIdentifiers(@NotNull ItemStack item, @NotNull String... arguments) {
        if (arguments.length == 0) {
            return false;
        }
        try {
            List<?> items = (List<?>) find.invoke(ecoItemFinder, item);
            for (Object ecoItem : items) {
                String id = (String) getID.invoke(ecoItem);

                if (id.equalsIgnoreCase(arguments[0])) {
                    return true;
                }
            }
            return false;

        } catch (ReflectiveOperationException e) {
            Bukkit.getLogger().warning("Failed to find EcoItems item: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getPrefix() {
        return "ecoitems-";
    }

    @Override
    public void clearCache() {
        cache.clear();
    }
}
