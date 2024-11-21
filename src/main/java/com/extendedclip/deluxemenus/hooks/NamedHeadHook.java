package com.extendedclip.deluxemenus.hooks;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.cache.SimpleCache;
import com.extendedclip.deluxemenus.utils.SkullUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.extendedclip.deluxemenus.utils.VersionHelper;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

public class NamedHeadHook implements ItemHook, Listener, SimpleCache {

    private final Map<String, ItemStack> cache = new ConcurrentHashMap<>();

    public NamedHeadHook(@NotNull final DeluxeMenus plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public ItemStack getItem(@NotNull final String... arguments) {
        if (arguments.length == 0) {
            return DeluxeMenus.getInstance().getHead().clone();
        }

        try {
            return cache.computeIfAbsent(arguments[0], SkullUtils::getSkullByName).clone();
        } catch (Exception exception) {
            DeluxeMenus.printStacktrace(
                    "Something went wrong while trying to get a head by name" +
                            ": " + arguments[0],
                    exception
            );
        }

        return DeluxeMenus.getInstance().getHead().clone();
    }

    @Override
    public boolean itemMatchesIdentifiers(@NotNull ItemStack item, @NotNull String... arguments) {
        if (arguments.length == 0) {
            return false;
        }
        return arguments[0].equalsIgnoreCase(SkullUtils.getSkullOwner(item));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        cache.remove(event.getPlayer().getName());
    }

    @Override
    public String getPrefix() {
        return "head-";
    }

    @Override
    public void clearCache() {
        cache.clear();
    }
}
