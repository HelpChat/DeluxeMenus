package com.extendedclip.deluxemenus.listener;

import com.extendedclip.deluxemenus.DeluxeMenus;
import org.jetbrains.annotations.NotNull;

public abstract class Listener implements org.bukkit.event.Listener {
    protected final DeluxeMenus plugin;

    public Listener(@NotNull final DeluxeMenus plugin) {
        this.plugin = plugin;
    }

    public void register() {
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }
}
