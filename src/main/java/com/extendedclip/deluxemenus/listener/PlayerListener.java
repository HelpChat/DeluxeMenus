package com.extendedclip.deluxemenus.listener;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.menu.MenuHolder;
import com.extendedclip.deluxemenus.menu.MenuItem;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
      Bukkit.getScheduler().runTaskLater(plugin, () -> {
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

    if (item.getClickHandler() != null) {
      if (item.getClickRequirements() != null) {
        if (!item.getClickRequirements().evaluate(holder)) {
          if (item.getClickRequirements().getDenyHandler() != null) {
            item.getClickRequirements().getDenyHandler().onClick(holder);
          }
          return;
        }
      }
      this.cache.put(player.getUniqueId(), System.currentTimeMillis());
      item.getClickHandler().onClick(holder);
      return;
    }

    if (event.isShiftClick() && event.isLeftClick() && item.getShiftLeftClickHandler() != null) {
      if (item.getShiftLeftClickRequirements() != null) {
        if (!item.getShiftLeftClickRequirements().evaluate(holder)) {
          if (item.getShiftLeftClickRequirements().getDenyHandler() != null) {
            item.getShiftLeftClickRequirements().getDenyHandler().onClick(holder);
          }
          return;
        }
      }
      this.cache.put(player.getUniqueId(), System.currentTimeMillis());
      item.getShiftLeftClickHandler().onClick(holder);
    } else if (event.isShiftClick() && event.isRightClick()
        && item.getShiftRightClickHandler() != null) {
      if (item.getShiftRightClickRequirements() != null) {
        if (!item.getShiftRightClickRequirements().evaluate(holder)) {
          if (item.getShiftRightClickRequirements().getDenyHandler() != null) {
            item.getShiftRightClickRequirements().getDenyHandler().onClick(holder);
          }
          return;
        }
      }
      this.cache.put(player.getUniqueId(), System.currentTimeMillis());
      item.getShiftRightClickHandler().onClick(holder);
    } else if (event.getClick() == ClickType.LEFT && item.getLeftClickHandler() != null) {
      if (item.getLeftClickRequirements() != null) {
        if (!item.getLeftClickRequirements().evaluate(holder)) {
          if (item.getLeftClickRequirements().getDenyHandler() != null) {
            item.getLeftClickRequirements().getDenyHandler().onClick(holder);
          }
          return;
        }
      }
      this.cache.put(player.getUniqueId(), System.currentTimeMillis());
      item.getLeftClickHandler().onClick(holder);
    } else if (event.getClick() == ClickType.RIGHT && item.getRightClickHandler() != null) {
      if (item.getRightClickRequirements() != null) {
        if (!item.getRightClickRequirements().evaluate(holder)) {
          if (item.getRightClickRequirements().getDenyHandler() != null) {
            item.getRightClickRequirements().getDenyHandler().onClick(holder);
          }
          return;
        }
      }
      this.cache.put(player.getUniqueId(), System.currentTimeMillis());
      item.getRightClickHandler().onClick(holder);
    } else if (event.getClick() == ClickType.MIDDLE && item.getMiddleClickHandler() != null) {
      if (item.getMiddleClickRequirements() != null) {
        if (!item.getMiddleClickRequirements().evaluate(holder)) {
          if (item.getMiddleClickRequirements().getDenyHandler() != null) {
            item.getMiddleClickRequirements().getDenyHandler().onClick(holder);
          }
          return;
        }
      }
      this.cache.put(player.getUniqueId(), System.currentTimeMillis());
      item.getMiddleClickHandler().onClick(holder);
    }
  }
}
