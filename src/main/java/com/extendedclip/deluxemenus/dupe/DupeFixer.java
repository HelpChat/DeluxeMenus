package com.extendedclip.deluxemenus.dupe;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.listener.Listener;
import com.extendedclip.deluxemenus.nbt.NbtProvider;
import com.extendedclip.deluxemenus.scheduler.scheduling.schedulers.TaskScheduler;
import com.extendedclip.deluxemenus.utils.DebugLevel;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * Prevents duplication of items created by DeluxeMenus. Items created by DeluxeMenus are marked and removed if found
 * outside the inventory they were created in.
 */
public class DupeFixer extends Listener {

    private final TaskScheduler scheduler;
    private final MenuItemMarker marker;

    public DupeFixer(@NotNull final DeluxeMenus plugin, @NotNull final MenuItemMarker marker) {
        super(plugin);
        this.scheduler = plugin.getScheduler();
        this.marker = marker;
    }

    @EventHandler
    private void onPickup(@NotNull final EntityPickupItemEvent event) {
        ItemStack stack = event.getItem().getItemStack();
        if (!marker.isMarked(stack) && isDupeProtectionFlagged(stack)) {
            return;
        }

        plugin.debug(
                DebugLevel.LOWEST,
                Level.INFO,
                "Someone picked up a DeluxeMenus item. Removing it."
        );
        event.getItem().remove();
    }

    @EventHandler
    private void onDrop(@NotNull final PlayerDropItemEvent event) {
        ItemStack stack = event.getItemDrop().getItemStack();
        if (!marker.isMarked(stack) && isDupeProtectionFlagged(stack)) {
            return;
        }

        plugin.debug(
                DebugLevel.LOWEST,
                Level.INFO,
                "A DeluxeMenus item was dropped in the world. Removing it."
        );
        event.getItemDrop().remove();
    }

    @EventHandler
    private void onLogin(@NotNull final PlayerJoinEvent event) {
        scheduler.runTaskLater(() -> {
                for (final ItemStack itemStack : event.getPlayer().getInventory().getContents()) {
                    if (itemStack == null) continue;
                    if (!marker.isMarked(itemStack) && isDupeProtectionFlagged(itemStack)) continue;

                    plugin.debug(
                            DebugLevel.LOWEST,
                            Level.INFO,
                            "Player logged in with a DeluxeMenus item in their inventory. Removing it."
                    );
                    event.getPlayer().getInventory().remove(itemStack);
                }
            }, 10L
        );
    }

    private boolean isDupeProtectionFlagged(ItemStack itemStack) {
        if (NbtProvider.isAvailable()) {
            String value = NbtProvider.getString(itemStack, "deluxemenus.item.dupeprotection");
            return !"true".equals(value);
        }

        return true;
    }
}
