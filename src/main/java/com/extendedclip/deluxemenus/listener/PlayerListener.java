package com.extendedclip.deluxemenus.listener;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.action.ClickHandler;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.menu.MenuHolder;
import com.extendedclip.deluxemenus.menu.MenuItem;
import com.extendedclip.deluxemenus.requirement.RequirementList;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerListener implements Listener {

  private final DeluxeMenus plugin;
  private final Cache<UUID, Long> cache = CacheBuilder.newBuilder()
      .expireAfterWrite(75, TimeUnit.MILLISECONDS).build();

  // This is so dumb. Mojang fix your shit.
  private final Cache<UUID, Long> shiftCache = CacheBuilder.newBuilder()
      .expireAfterWrite(200, TimeUnit.MILLISECONDS).build();

  public PlayerListener(DeluxeMenus plugin) {
    this.plugin = plugin;
    Bukkit.getPluginManager().registerEvents(this, this.plugin);
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onCommandExecute(PlayerCommandPreprocessEvent event) {

    String cmd = event.getMessage().substring(1);
    Menu menu = Menu.getMenuByCommand(cmd.toLowerCase());

    if (menu == null) {
      return;
    }

    if (menu.registersCommand()) {
      return;
    }

    Player player = event.getPlayer();
    menu.openMenu(player);
    event.setCancelled(true);
  }

  @EventHandler
  public void onLeave(PlayerQuitEvent event) {
    Player player = event.getPlayer();

    if (Menu.inMenu(player)) {
      Menu.closeMenu(player, false);
    }
  }

  @EventHandler
  public void onOpen(InventoryOpenEvent event) {
    if (!(event.getPlayer() instanceof Player)) {
      return;
    }

    final Player player = (Player) event.getPlayer();

    if (player.isSleeping()) {
      event.setCancelled(true);
    }

    if (Menu.inMenu(player)) {
      Menu.closeMenu(player, true);
    }
  }

  @EventHandler
  public void onClose(InventoryCloseEvent event) {

    if (!(event.getPlayer() instanceof Player)) {
      return;
    }

    final Player player = (Player) event.getPlayer();

    if (Menu.inMenu(player)) {
      Menu.closeMenu(player, false);
      plugin.getUniversalScheduler().runTaskLater(plugin, () -> {
        Menu.cleanInventory(player, plugin.getMenuItemMarker());
        player.updateInventory();
      }, 3L);
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onClick(InventoryClickEvent event) {

    if (!(event.getWhoClicked() instanceof Player)) {
      return;
    }

    final Player player = (Player) event.getWhoClicked();

    MenuHolder holder = Menu.getMenuHolder(player);

    if (holder == null) {
      return;
    }

    if (holder.getMenu() == null) {
      Menu.closeMenu(player, true);
    }

    if (holder.isUpdating()) {
      event.setCancelled(true);
      return;
    }

    event.setCancelled(true);

    int slot = event.getRawSlot();

    MenuItem item = holder.getItem(slot);

    if (item == null) {
      return;
    }

    if (this.cache.getIfPresent(player.getUniqueId()) != null) {
      return;
    }

    if (this.shiftCache.getIfPresent(player.getUniqueId()) != null) {
      return;
    }

    if (event.getClick() == ClickType.DOUBLE_CLICK) {
      return;
    }

    if (event.getClick() == ClickType.SHIFT_LEFT) {
      this.shiftCache.put(player.getUniqueId(), System.currentTimeMillis());
    }

    if (handleClick(player, holder, item.options().clickHandler(),
            item.options().clickRequirements())) {
      return;
    }

    if (event.isShiftClick() && event.isLeftClick()) {
      if (handleClick(player, holder, item.options().shiftLeftClickHandler(),
              item.options().shiftLeftClickRequirements())) {
        return;
      }
    }

    if (event.isShiftClick() && event.isRightClick()) {
      if (handleClick(player, holder, item.options().shiftRightClickHandler(),
              item.options().shiftRightClickRequirements())) {
        return;
      }
    }

    if (event.getClick() == ClickType.LEFT) {
      if (handleClick(player, holder, item.options().leftClickHandler(),
              item.options().leftClickRequirements())) {
        return;
      }
    }

    if (event.getClick() == ClickType.RIGHT) {
      if (handleClick(player, holder, item.options().rightClickHandler(),
              item.options().rightClickRequirements())) {
        return;
      }
    }

    if (event.getClick() == ClickType.MIDDLE) {
      if (handleClick(player, holder, item.options().middleClickHandler(),
              item.options().middleClickRequirements())) {
        return;
      }
    }
  }

  /**
   * Handles menu click by player
   * @param player player who clicked
   * @param holder menu holder
   * @param handler click handler
   * @param requirements click requirements
   * @return true if click was handled successfully. will ever return false if no click handler was found
   */
  private boolean handleClick(final @NotNull Player player, final @NotNull MenuHolder holder,
                              final @NotNull Optional<ClickHandler> handler,
                              final @NotNull Optional<RequirementList> requirements) {
    if (handler.isEmpty()) {
      return false;
    }

    if (requirements.isPresent()) {
      final ClickHandler denyHandler = requirements.get().getDenyHandler();

      if (!requirements.get().evaluate(holder)) {
        if (denyHandler == null) {
          return true;
        }

        denyHandler.onClick(holder);
        return true;
      }
    }

    this.cache.put(player.getUniqueId(), System.currentTimeMillis());
    handler.get().onClick(holder);

    return true;
  }
}
