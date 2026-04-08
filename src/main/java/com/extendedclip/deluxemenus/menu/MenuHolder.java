package com.extendedclip.deluxemenus.menu;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.action.ClickHandler;
import com.extendedclip.deluxemenus.config.DeluxeMenusConfig;
import com.extendedclip.deluxemenus.menu.options.MenuItemOptions;
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
    private BukkitTask refreshTask = null;
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

            if (active.isEmpty() && menu.getItemTemplates().isEmpty()) {
                Menu.closeMenu(plugin, getViewer(), true);
            }

            // Expand item_templates (placeholder resolution happens here, in the async phase)
            final Set<MenuItem> templateItems = expandTemplateItems(menu, active);

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

                // Place template-expanded items into the inventory
                for (MenuItem templateItem : templateItems) {
                    ItemStack iStack = templateItem.getItemStack(this);
                    if (iStack != null) {
                        iStack = plugin.getMenuItemMarker().mark(iStack);
                    }
                    int slot = templateItem.options().slot();
                    if (slot < menu.options().size()) {
                        getInventory().setItem(slot, iStack);
                    }
                    if (templateItem.options().updatePlaceholders()) {
                        update = true;
                    }
                }

                // Merge template items into the active set
                active.addAll(templateItems);
                setActiveItems(active);

                if (update && updateTask == null) {
                    startUpdatePlaceholdersTask();
                } else if(!update && updateTask != null) {
                    stopPlaceholderUpdate();
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

    public void stopRefreshTask() {
        if(refreshTask != null) {
            try {
                refreshTask.cancel();
            } catch (Exception ignored) {
            }
            refreshTask = null;
        }
    }

    public void startRefreshTask() {
        if(refreshTask != null) {
            stopRefreshTask();
        }

        refreshTask = new BukkitRunnable() {
            @Override
            public void run() {
                refreshMenu();
            }
        }.runTaskTimerAsynchronously(plugin, 20L,
                20L * Menu.getMenuByName(menuName)
                        .map(Menu::options)
                        .map(MenuOptions::refreshInterval)
                        .orElse(10));
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

    /**
     * Expands all {@link MenuItemTemplate}s for the given menu into concrete {@link MenuItem}s.
     * <p>
     * For each template, the {@code based-on} placeholder is resolved for the viewing player.
     * The result is expected to be a comma-separated list of values. Each value produces one
     * {@link MenuItem} placed at the corresponding slot from the template's slot list.
     * All occurrences of {@code <template:name>} in display_name, lore, and command strings
     * are replaced with the current iteration value.
     * <p>
     * If a regular item already occupies a slot with equal or higher priority, the template
     * item for that slot is skipped.
     *
     * @param menu            the menu whose templates should be expanded
     * @param existingActive  the set of already-active regular items (may be mutated if a
     *                        template overwrites a lower-priority regular item)
     * @return the set of newly created template {@link MenuItem}s
     */
    public Set<MenuItem> expandTemplateItems(final @NotNull Menu menu, final @NotNull Set<MenuItem> existingActive) {
        Set<MenuItem> expandedItems = new HashSet<>();

        for (MenuItemTemplate template : menu.getItemTemplates()) {
            String raw = setPlaceholders(template.getBasedOnPlaceholder());

            // If the placeholder didn't resolve or is empty, skip this template entirely
            if (raw.isEmpty() || raw.equals(template.getBasedOnPlaceholder())) {
                continue;
            }

            // Comma is the delimiter for the based-on placeholder return value
            String[] splitValues = raw.split(",");
            List<String> values = new ArrayList<>();
            for (String v : splitValues) {
                String trimmed = v.trim();
                if (!trimmed.isEmpty()) {
                    values.add(trimmed);
                }
            }

            if (values.isEmpty()) {
                continue;
            }

            List<Integer> slots = template.getSlots();
            int count = Math.min(values.size(), slots.size());

            for (int i = 0; i < count; i++) {
                String currentValue = values.get(i);
                int slot = slots.get(i);

                if (slot >= menu.options().size()) {
                    continue;
                }

                // Check priority against any existing item at this slot
                final int slotToCheck = slot;
                MenuItem existingItem = existingActive.stream()
                        .filter(item -> item.options().slot() == slotToCheck)
                        .findFirst()
                        .orElse(null);

                if (existingItem != null && existingItem.options().priority() >= template.getBaseOptions().priority()) {
                    continue;
                }

                // Evaluate view requirement if present
                if (template.getBaseOptions().viewRequirements().isPresent()) {
                    if (!template.getBaseOptions().viewRequirements().get().evaluate(this)) {
                        continue;
                    }
                }

                // Clone the base options with the slot set and <template:name> replaced
                MenuItemOptions.MenuItemOptionsBuilder builder = template.getBaseOptions().asBuilder()
                        .slot(slot);

                // Replace <template:name> in display_name
                if (template.getBaseOptions().displayName().isPresent()) {
                    builder.displayName(
                            template.getBaseOptions().displayName().get().replace("<template:name>", currentValue)
                    );
                }

                // Replace <template:name> in each lore line
                if (!template.getBaseOptions().lore().isEmpty()) {
                    List<String> newLore = new ArrayList<>();
                    for (String line : template.getBaseOptions().lore()) {
                        newLore.add(line.replace("<template:name>", currentValue));
                    }
                    builder.lore(newLore);
                }

                // Replace <template:name> in raw command strings and parse into ClickHandlers
                builder.clickHandler(buildTemplateClickHandler(template.getClickCommands(), currentValue));
                builder.leftClickHandler(buildTemplateClickHandler(template.getLeftClickCommands(), currentValue));
                builder.rightClickHandler(buildTemplateClickHandler(template.getRightClickCommands(), currentValue));
                builder.shiftLeftClickHandler(buildTemplateClickHandler(template.getShiftLeftClickCommands(), currentValue));
                builder.shiftRightClickHandler(buildTemplateClickHandler(template.getShiftRightClickCommands(), currentValue));
                builder.middleClickHandler(buildTemplateClickHandler(template.getMiddleClickCommands(), currentValue));

                MenuItem expandedItem = new MenuItem(plugin, builder.build());
                expandedItems.add(expandedItem);

                // If overwriting a lower-priority regular item, remove it from the existing active set
                if (existingItem != null) {
                    existingActive.remove(existingItem);
                }
            }
        }

        return expandedItems;
    }

    private ClickHandler buildTemplateClickHandler(final @NotNull List<String> rawCommands, final @NotNull String templateValue) {
        if (rawCommands.isEmpty()) {
            return null;
        }
        List<String> replaced = new ArrayList<>(rawCommands.size());
        for (String cmd : rawCommands) {
            replaced.add(cmd.replace("<template:name>", templateValue));
        }
        return DeluxeMenusConfig.parseClickHandler(plugin, replaced);
    }
}
