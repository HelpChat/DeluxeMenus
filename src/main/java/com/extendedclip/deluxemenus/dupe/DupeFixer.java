package com.extendedclip.deluxemenus.dupe;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.utils.DebugLevel;
import io.github.projectunified.minelib.scheduler.global.GlobalScheduler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class DupeFixer implements Listener {

    private final DeluxeMenus plugin;
    private final MenuItemMarker marker;

    public DupeFixer(@NotNull final DeluxeMenus plugin, @NotNull final MenuItemMarker marker) {
        this.plugin = plugin;
        this.marker = marker;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onPickup(@NotNull final EntityPickupItemEvent event) {
        if (!marker.isMarked(event.getItem().getItemStack())) {
            return;
        }

        DeluxeMenus.debug(
                DebugLevel.LOWEST,
                Level.INFO,
                "Someone picked up a DeluxeMenus item. Removing it."
        );
        event.getItem().remove();
    }

    @EventHandler
    private void onDrop(@NotNull final PlayerDropItemEvent event) {
        if (!marker.isMarked(event.getItemDrop().getItemStack())) {
            return;
        }

        DeluxeMenus.debug(
                DebugLevel.LOWEST,
                Level.INFO,
                "A DeluxeMenus item was dropped in the world. Removing it."
        );
        event.getItemDrop().remove();
    }

    @EventHandler
    private void onLogin(@NotNull final PlayerLoginEvent event) {
        GlobalScheduler.get(plugin).runLater(
                () -> {
                    for (final ItemStack itemStack : event.getPlayer().getInventory().getContents()) {
                        if (itemStack == null) continue;
                        if (!marker.isMarked(itemStack)) continue;

                        DeluxeMenus.debug(
                                DebugLevel.LOWEST,
                                Level.INFO,
                                "Player logged in with a DeluxeMenus item in their inventory. Removing it."
                        );
                        event.getPlayer().getInventory().remove(itemStack);
                    }},
                10L
        );
    }
}