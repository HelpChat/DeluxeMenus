package com.extendedclip.deluxemenus.listener;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.action.ClickHandler;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.menu.MenuHolder;
import com.extendedclip.deluxemenus.menu.MenuItem;
import com.extendedclip.deluxemenus.requirement.RequirementList;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerListener extends Listener {

    private final Cache<UUID, Long> clickCooldown = CacheBuilder.newBuilder()
        .expireAfterWrite(75, TimeUnit.MILLISECONDS)
        .build();

    // This is so dumb. Mojang fix your shit.
    private final Cache<UUID, Long> shiftClickCooldown = CacheBuilder.newBuilder()
        .expireAfterWrite(200, TimeUnit.MILLISECONDS)
        .build();

    public PlayerListener(@NotNull final DeluxeMenus plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCommandExecute(PlayerCommandPreprocessEvent event) {
        final String cmd = event.getMessage().substring(1).toLowerCase();
        final Menu menu = Menu.getMenuByCommand(cmd);
        
        if (menu == null || menu.options().registerCommands()) {
            return;
        }

        menu.openMenu(event.getPlayer());
        event.setCancelled(true);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        cleanupPlayer(event.getPlayer());
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        if (player.isSleeping()) {
            event.setCancelled(true);
        }

        if (Menu.isInMenu(player)) {
            Menu.closeMenu(plugin, player, true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        if (Menu.isInMenu(player)) {
            Menu.closeMenu(plugin, player, false);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Menu.cleanInventory(plugin, player);
                player.updateInventory();
            }, 3L);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        final MenuHolder holder = Menu.getMenuHolder(player);
        if (holder == null) {
            return;
        }

        if (holder.getMenu() == null) {
            Menu.closeMenu(plugin, player, true);
            return;
        }

        if (holder.isUpdating()) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        final int slot = event.getRawSlot();
        final MenuItem item = holder.getItem(slot);
        
        if (item == null) {
            return;
        }

        final UUID uuid = player.getUniqueId();
        if (isClickBlocked(uuid, event.getClick())) {
            return;
        }

        if (processClick(player, holder, item, event.getClick())) {
            clickCooldown.put(uuid, System.currentTimeMillis());
        }
    }

    private boolean isClickBlocked(UUID uuid, ClickType clickType) {
        if (clickType == ClickType.DOUBLE_CLICK) {
            return true;
        }
        
        if (clickType == ClickType.SHIFT_LEFT) {
            shiftClickCooldown.put(uuid, System.currentTimeMillis());
        }
        
        return clickCooldown.getIfPresent(uuid) != null 
            || shiftClickCooldown.getIfPresent(uuid) != null;
    }

    private boolean processClick(Player player, MenuHolder holder, MenuItem item, ClickType clickType) {
        if (handleClick(player, holder, 
            item.options().clickHandler(),
            item.options().clickRequirements())) {
            return true;
        }

        return switch (clickType) {
            case SHIFT_LEFT -> handleClick(player, holder,
                item.options().shiftLeftClickHandler(),
                item.options().shiftLeftClickRequirements());
                
            case SHIFT_RIGHT -> handleClick(player, holder,
                item.options().shiftRightClickHandler(),
                item.options().shiftRightClickRequirements());
                
            case LEFT -> handleClick(player, holder,
                item.options().leftClickHandler(),
                item.options().leftClickRequirements());
                
            case RIGHT -> handleClick(player, holder,
                item.options().rightClickHandler(),
                item.options().rightClickRequirements());
                
            case MIDDLE -> handleClick(player, holder,
                item.options().middleClickHandler(),
                item.options().middleClickRequirements());
                
            default -> false;
        };
    }

    /**
     * Handles menu click by player
     *
     * @param player       player who clicked
     * @param holder       menu holder
     * @param handler      click handler
     * @param requirements click requirements
     * @return true if click was handled successfully. will ever return false if no click handler was found
     */
    private boolean handleClick(
        final @NotNull Player player,
        final @NotNull MenuHolder holder,
        final @NotNull ClickHandler handler,
        final @NotNull RequirementList requirements
    ) {
        if (handler == null) {
            return false;
        }

        if (!requirements.evaluate(holder)) {
            final ClickHandler denyHandler = requirements.getDenyHandler();
            if (denyHandler != null) {
                denyHandler.onClick(holder);
            }
            return true;
        }

        handler.onClick(holder);
        return true;
    }

    private void cleanupPlayer(Player player) {
        if (Menu.isInMenu(player)) {
            Menu.closeMenu(plugin, player, false);
        }
    }
}
