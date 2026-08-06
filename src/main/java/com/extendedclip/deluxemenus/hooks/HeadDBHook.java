package com.extendedclip.deluxemenus.hooks;

import com.extendedclip.deluxemenus.DeluxeMenus;
import io.github.silentdevelopment.headdb.HeadDBService;
import io.github.silentdevelopment.headdb.model.Head;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

public final class HeadDBHook implements ItemHook {

    private final DeluxeMenus plugin;
    private final HeadDBService service;
    private final TextureHeadHook textureHook;

    public HeadDBHook(@NotNull final DeluxeMenus plugin, @NotNull final TextureHeadHook textureHook) {
        this.plugin = plugin;
        this.textureHook = textureHook;

        final RegisteredServiceProvider<HeadDBService> registration = Bukkit.getServicesManager().getRegistration(HeadDBService.class);
        this.service = registration == null ? null : registration.getProvider();
    }

    @Override
    public ItemStack getItem(@NotNull final String... arguments) {
        if (arguments.length == 0) {
            return plugin.getHead().clone();
        }

        if (service == null) {
            return plugin.getHead().clone();
        }

        final Head head = service.find(arguments[0]).orElse(null);

        if (head == null) {
            return plugin.getHead().clone();
        }

        return textureHook.getItem(head.texture().hash());
    }

    @Override
    public boolean itemMatchesIdentifiers(@NotNull final ItemStack item, @NotNull final String... arguments) {
        return false;
    }

    @Override
    public String getPrefix() {
        return "headdb-";
    }
}