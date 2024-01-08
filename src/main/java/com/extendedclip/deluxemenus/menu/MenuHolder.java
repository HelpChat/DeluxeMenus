package com.extendedclip.deluxemenus.menu;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.utils.StringUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class MenuHolder implements InventoryHolder {

  private final Player viewer;
  private Player placeholderPlayer;
  private String menuName;
  private Set<MenuItem> activeItems;
  private BukkitTask updateTask = null;
  private Inventory inventory;
  private boolean updating;
  private Map<String, String> typedArgs;

  public MenuHolder(Player viewer) {
    this.viewer = viewer;
  }

  public MenuHolder(Player viewer, String menuName,
      Set<MenuItem> activeItems, Inventory inventory) {
    this.viewer = viewer;
    this.menuName = menuName;
    this.activeItems = activeItems;
    this.inventory = inventory;
  }

  public String getViewerName() {
    return viewer.getName();
  }

  public BukkitTask getUpdateTask() {
    return updateTask;
  }

  public Player getViewer() {
    return viewer;
  }

  public String getMenuName() {
    return menuName;
  }

  public void setMenuName(String menuName) {
    this.menuName = menuName;
  }

  public Set<MenuItem> getActiveItems() {
    return activeItems;
  }

  public void setActiveItems(Set<MenuItem> items) {
    this.activeItems = items;
  }

  public MenuHolder getHolder() {
    return this;
  }

  public MenuItem getItem(int slot) {
    for (MenuItem item : activeItems) {
      if (item.options().slot() == slot) {
        return item;
      }
    }
    return null;
  }

  public Menu getMenu() {
    return Menu.getMenu(menuName);
  }

  public String setPlaceholders(String string) {
    // Set argument placeholders first
    if (this.typedArgs != null && !this.typedArgs.isEmpty()) {
      for (Entry<String, String> entry : typedArgs.entrySet()) {
        string = string.replace("{" + entry.getKey() + "}", entry.getValue());
      }
    }

    // Then set actual PAPI placeholders
    if (placeholderPlayer != null) {
      return PlaceholderAPI.setPlaceholders((OfflinePlayer) placeholderPlayer, string);
    } else {
      return this.getViewer() == null ? string
              : PlaceholderAPI.setPlaceholders((OfflinePlayer) this.getViewer(), string);
    }
  }

  public String setArguments(String string) {
    if (this.typedArgs == null || this.typedArgs.isEmpty()) {
      return string;
    }
    for (Entry<String, String> entry : this.typedArgs.entrySet()) {
      string = string.replace("{" + entry.getKey() + "}", entry.getValue());
    }
    return string;
  }

  public void refreshMenu() {

    Menu menu = getMenu();

    if (menu == null || menu.getMenuItems() == null || menu.getMenuItems().size() <= 0) {
      return;
    }

    setUpdating(true);

    stopPlaceholderUpdate();

    Bukkit.getScheduler().runTaskAsynchronously(DeluxeMenus.getInstance(), () -> {

      final Set<MenuItem> active = new HashSet<>();

      for (int i = 0; i < getInventory().getSize(); i++) {
        TreeMap<Integer, MenuItem> e = menu.getMenuItems().get(i);

        if (e == null) {
          getInventory().setItem(i, null);
          continue;
        }

        boolean m = false;
        for (MenuItem item : e.values()) {

          if (item.options().viewRequirements().isPresent()) {

            if (item.options().viewRequirements().get().evaluate(this)) {
              m = true;
              active.add(item);
              break;
            }
          } else {
            m = true;
            active.add(item);
            break;
          }
        }

        if (!m) {
          getInventory().setItem(i, null);
        }
      }

      if (active.isEmpty()) {
        Menu.closeMenu(getViewer(), true);
      }

      Bukkit.getScheduler().runTask(DeluxeMenus.getInstance(), () -> {

        boolean update = false;

        for (MenuItem item : active) {

          ItemStack iStack = item.getItemStack(this);

          int slot = item.options().slot();

          if (slot >= menu.getSize()) {
            continue;
          }

          if (item.options().updatePlaceholders()) {
            update = true;
          }

          getInventory().setItem(item.options().slot(), iStack);
        }

        setActiveItems(active);

        if (update) {
          startUpdatePlaceholdersTask();
        }

        setUpdating(false);
      });
    });
  }

  public void stopPlaceholderUpdate() {
    if (updateTask != null) {
      try {
        updateTask.cancel();
      } catch (Exception ignored) {
      }
      updateTask = null;
    }
  }

  public void startUpdatePlaceholdersTask() {

    if (updateTask != null) {
      stopPlaceholderUpdate();
    }

    updateTask = new BukkitRunnable() {

      @Override
      public void run() {

        if (updating) {
          return;
        }

        Set<MenuItem> items = getActiveItems();

        if (items == null) {
          return;
        }

        for (MenuItem item : items) {

          if (item.options().updatePlaceholders()) {

            ItemStack i = inventory.getItem(item.options().slot());

            if (i == null) {
              continue;
            }

            int amt = i.getAmount();

            if (item.options().dynamicAmount().isPresent()) {
              try {
               amt = Integer.parseInt(setPlaceholders(item.options().dynamicAmount().get()));
                if (amt <= 0) {
                  amt = 1;
                }
              } catch (Exception exception) {
                DeluxeMenus.printStacktrace(
                    "Something went wrong while updating item in slot " + item.options().slot() +
                        ". Invalid dynamic amount: " + setPlaceholders(item.options().dynamicAmount().get()),
                    exception
                );
              }
            }

            ItemMeta meta = i.getItemMeta();

            if (item.options().displayNameHasPlaceholders() && item.options().displayName().isPresent()) {
              meta.setDisplayName(StringUtils.color(setPlaceholders(item.options().displayName().get())));
            }

            if (item.options().loreHasPlaceholders()) {

              List<String> updated = new ArrayList<>();

              for (String line : item.options().lore()) {
                updated.add(StringUtils
                    .color(setPlaceholders(line)));
              }
              meta.setLore(updated);
            }

            i.setItemMeta(meta);
            i.setAmount(amt);
          }
        }
      }

    }.runTaskTimerAsynchronously(DeluxeMenus.getInstance(), 20L,
        20L * Menu.getMenu(menuName).getUpdateInterval());
  }

  public boolean isUpdating() {
    return updating;
  }

  public void setUpdating(boolean updating) {
    this.updating = updating;
  }

  @Override
  public Inventory getInventory() {
    return this.inventory;
  }

  public void setInventory(Inventory i) {
    this.inventory = i;
  }

  public Map<String, String> getTypedArgs() {
    return typedArgs;
  }

  public void setTypedArgs(Map<String, String> typedArgs) {
    this.typedArgs = typedArgs;
  }

  public void setPlaceholderPlayer(Player placeholderPlayer) {
    this.placeholderPlayer = placeholderPlayer;
  }

  public Player getPlaceholderPlayer() {
    return placeholderPlayer;
  }
}
