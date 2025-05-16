package com.extendedclip.deluxemenus.menu;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.options.MenuOptions;
import com.extendedclip.deluxemenus.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

public class MenuHolder implements InventoryHolder {

    private final DeluxeMenus plugin;
    private final Player viewer;

    private Player placeholderPlayer;
    private String menuName;
    private Set<MenuItem> activeItems;
    private BukkitTask updateTask = null;
    private Inventory inventory;
    private boolean updating;
    private boolean parsePlaceholdersInArguments;
    private boolean parsePlaceholdersAfterArguments;
    private Map<String, String> typedArgs;

    public MenuHolder(final @NotNull DeluxeMenus plugin, final @NotNull Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;
    }

    public MenuHolder(final @NotNull DeluxeMenus plugin, final @NotNull Player viewer, final @NotNull String menuName,
                      final @NotNull Set<@NotNull MenuItem> activeItems, final @NotNull Inventory inventory) {
        this.plugin = plugin;
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

    public Optional<Menu> getMenu() {
        return Menu.getMenuByName(menuName);
    }

    public @NotNull String setPlaceholdersAndArguments(final @NotNull String string) {
        if (parsePlaceholdersAfterArguments) {
            return setPlaceholders(setArguments(string));
        }
        return setArguments(setPlaceholders(string));
    }

    public @NotNull String setPlaceholders(final @NotNull String string) {
        final Player player = this.placeholderPlayer != null ? this.placeholderPlayer : this.getViewer();
        if (player == null) {
            return string;
        }

        return StringUtils.replacePlaceholders(string, player);
    }

    public @NotNull String setArguments(final @NotNull String string) {
        final Player player = this.placeholderPlayer != null ? this.placeholderPlayer : this.getViewer();

        return StringUtils.replaceArguments(
                string,
                this.typedArgs,
                player,
                this.parsePlaceholdersInArguments
        );
    }

    public void refreshMenu() {

        Optional<Menu> optionalMenu = getMenu();
        if (optionalMenu.isEmpty()) {
            return;
        }

        Menu menu = optionalMenu.get();

        if (menu.getMenuItems().isEmpty()) {
            return;
        }

        setUpdating(true);

        stopPlaceholderUpdate();

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {

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
                Menu.closeMenu(plugin, getViewer(), true);
            }

            Bukkit.getScheduler().runTask(plugin, () -> {

                boolean update = false;

                for (MenuItem item : active) {

                    ItemStack iStack = item.getItemStack(this);

                    int slot = item.options().slot();

                    if (slot >= menu.options().size()) {
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
                                amt = Integer.parseInt(setPlaceholdersAndArguments(item.options().dynamicAmount().get()));
                                if (amt <= 0) {
                                    amt = 1;
                                }
                            } catch (Exception exception) {
                                plugin.printStacktrace(
                                        "Something went wrong while updating item in slot " + item.options().slot() +
                                                ". Invalid dynamic amount: " + setPlaceholdersAndArguments(item.options().dynamicAmount().get()),
                                        exception
                                );
                            }
                        }

                        ItemMeta meta = i.getItemMeta();

                        if (item.options().displayNameHasPlaceholders() && item.options().displayName().isPresent()) {
                            meta.setDisplayName(StringUtils.color(setPlaceholdersAndArguments(item.options().displayName().get())));
                        }

                        if (item.options().loreHasPlaceholders()) {
                            meta.setLore(item.getMenuItemLore(getHolder(), item.options().lore()));
                        }

                        i.setItemMeta(meta);
                        i.setAmount(amt);
                    }
                }
            }

        }.runTaskTimerAsynchronously(plugin, 20L,
                20L * Menu.getMenuByName(menuName)
                        .map(Menu::options)
                        .map(MenuOptions::updateInterval)
                        .orElse(10));
    }

    public boolean isUpdating() {
        return updating;
    }

    public void setUpdating(boolean updating) {
        this.updating = updating;
    }

    @Override
    public @NotNull Inventory getInventory() {
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

    public void parsePlaceholdersInArguments(final boolean parsePlaceholdersInArguments) {
        this.parsePlaceholdersInArguments = parsePlaceholdersInArguments;
    }

    public void parsePlaceholdersAfterArguments(final boolean parsePlaceholdersAfterArguments) {
        this.parsePlaceholdersAfterArguments = parsePlaceholdersAfterArguments;
    }

    public boolean parsePlaceholdersInArguments() {
        return parsePlaceholdersInArguments;
    }

    public boolean parsePlaceholdersAfterArguments() {
        return parsePlaceholdersAfterArguments;
    }

    public void setPlaceholderPlayer(Player placeholderPlayer) {
        this.placeholderPlayer = placeholderPlayer;
    }

    public Player getPlaceholderPlayer() {
        return placeholderPlayer;
    }

    public @NotNull DeluxeMenus getPlugin() {
        return plugin;
    }
}
