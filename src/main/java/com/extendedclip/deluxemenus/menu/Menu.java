package com.extendedclip.deluxemenus.menu;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.events.DeluxeMenusOpenMenuEvent;
import com.extendedclip.deluxemenus.events.DeluxeMenusPreOpenMenuEvent;
import com.extendedclip.deluxemenus.menu.command.RegistrableMenuCommand;
import com.extendedclip.deluxemenus.menu.options.MenuOptions;
import com.extendedclip.deluxemenus.requirement.RequirementList;
import com.extendedclip.deluxemenus.scheduler.scheduling.schedulers.TaskScheduler;
import com.extendedclip.deluxemenus.utils.DebugLevel;
import com.extendedclip.deluxemenus.utils.StringUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Menu {

    private static final Map<String, Menu> menus = new ConcurrentHashMap<>();
    private static final Map<String, Menu> subMenus = new ConcurrentHashMap<>();
    private static final Set<MenuHolder> menuHolders = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Menu> lastOpenedMenus = new ConcurrentHashMap<>();

    private final DeluxeMenus plugin;
    private final TaskScheduler scheduler;
    private final MenuOptions options;
    private final Map<Integer, TreeMap<Integer, MenuItem>> items;
    // menu path starting from the plugin directory
    private final String path;

    private RegistrableMenuCommand command = null;

    public Menu(
            final @NotNull DeluxeMenus plugin,
            final @NotNull MenuOptions options,
            final @NotNull Map<Integer, TreeMap<Integer, MenuItem>> items,
            final @NotNull String path
    ) {
        this.plugin = plugin;
        this.scheduler = plugin.getScheduler();
        this.options = options;
        this.items = items;
        this.path = path;

        if (!this.options.subMenu() && this.options.registerCommands()) {
            this.command = new RegistrableMenuCommand(plugin, this);
            this.command.register();
        }

        if (this.options.subMenu()) {
            subMenus.put(this.options.name(), this);
        } else {
            menus.put(this.options.name(), this);
        }
    }

    public static void unload(final @NotNull DeluxeMenus plugin, final @NotNull String name) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isInMenu(p, name)) {
                closeMenu(plugin, p, true);
            }
        }

        Optional<Menu> optionalMenu = Menu.getMenuByName(name);
        if (optionalMenu.isEmpty()) {
            subMenus.remove(name);
            return;
        }

        optionalMenu.get().unregisterCommand();
        menus.remove(name);
    }

    public static void unload(final @NotNull DeluxeMenus plugin) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isInMenu(p)) {
                closeMenu(plugin, p, true);
            }
        }
        for (Menu menu : Menu.getAllMenus()) {
            menu.unregisterCommand();
        }
        for (Menu menu : Menu.getAllSubMenus()) {
            menu.unregisterCommand();
        }
        menus.clear();
        subMenus.clear();
        menuHolders.clear();
        lastOpenedMenus.clear();
    }

    private void unregisterCommand() {
        if (this.command != null) {
            this.command.unregister();
        }

        // WARNING! A reference to the command is stored by CraftBukkit for their `/help` command. There is currently
        // no way to remove this reference!
        this.command = null;
    }

    public static void unloadForShutdown(final @NotNull DeluxeMenus plugin) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isInMenu(player)) {
                closeMenuForShutdown(plugin, player);
            }
        }
        menus.clear();
        subMenus.clear();
    }

    public static int getLoadedMenuSize() {
        return menus.size();
    }

    public static int getLoadedSubMenuSize() {
        return subMenus.size();
    }

    public static @NotNull Set<String> getAllMenuNames() {
        return menus.keySet();
    }

    public static @NotNull Collection<Menu> getAllMenus() {
        return menus.values();
    }

    public static @NotNull Collection<Menu> getAllSubMenus() {
        return subMenus.values();
    }

    // Menus need to be stored in a list because config.yml can contain multiple menus.
    // This can be changed once we remove support for menus inside the config file.
    public static @NotNull TreeMap<String, List<Menu>> getPathSortedMenus() {
        return menus.values().stream().map(m -> Map.entry(m.path(), m)).collect(
                TreeMap::new, (tree, entry) -> {
                    final List<Menu> list = tree.computeIfAbsent(entry.getKey(), k -> new ArrayList<>());
                    list.add(entry.getValue());
                    tree.put(entry.getKey(), list);
                },
                (tree1, tree2) -> {
                    for (Entry<String, List<Menu>> entry : tree2.entrySet()) {
                        final List<Menu> list = tree1.computeIfAbsent(entry.getKey(), k -> new ArrayList<>());
                        list.addAll(entry.getValue());
                        tree1.put(entry.getKey(), list);
                    }
                }
        );
    }

    public static @NotNull Optional<Menu> getMenuByName(final @NotNull String name) {
        return menus.entrySet().stream().filter(e -> e.getKey().equalsIgnoreCase(name)).findFirst().map(Entry::getValue);
    }

    public static @NotNull Optional<Menu> getSubMenuByName(final @NotNull String name) {
        return subMenus.entrySet().stream().filter(e -> e.getKey().equalsIgnoreCase(name)).findFirst().map(Entry::getValue);
    }

    public static @NotNull Optional<Menu> getMenuByCommand(final @NotNull String command) {
        return menus.values().stream().filter(m -> m.getMenuCommandUsed(command).isPresent()).findFirst();
    }

    public static boolean isMenuCommand(final @NotNull String command) {
        return getMenuByCommand(command).isPresent();
    }

    public static boolean isInMenu(final @NotNull Player player) {
        return menuHolders.stream().anyMatch(h -> h.getViewerName().equals(player.getName()));
    }

    public static boolean isInMenu(final @NotNull Player player, final @NotNull String menu) {
        return menuHolders.stream().anyMatch(h -> h.getMenuName().equals(menu) && h.getViewerName().equals(player.getName()));
    }

    public static Optional<MenuHolder> getMenuHolder(final @NotNull Player player) {
        return menuHolders.stream().filter(h -> h.getViewerName().equals(player.getName())).findFirst();
    }

    public static Optional<Menu> getOpenMenu(final @NotNull Player player) {
        return getMenuHolder(player).flatMap(MenuHolder::getMenu);
    }

    public static Optional<Menu> getLastMenu(final @NotNull Player player) {
        return Optional.ofNullable(lastOpenedMenus.get(player.getUniqueId()));
    }

    public static void cleanInventory(final @NotNull DeluxeMenus plugin, final @NotNull Player player) {
        for (final ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack == null) continue;
            if (!plugin.getMenuItemMarker().isMarked(itemStack)) continue;

            plugin.debug(
                    DebugLevel.LOWEST,
                    Level.INFO,
                    "Found a DeluxeMenus item in a player's inventory. Removing it."
            );
            player.getInventory().remove(itemStack);
        }
        player.updateInventory();
    }

    public static void closeMenu(final @NotNull DeluxeMenus plugin, final @NotNull Player player, final boolean close, final boolean executeCloseActions) {
        Optional<MenuHolder> optionalHolder = getMenuHolder(player);
        if (optionalHolder.isEmpty()) {
            return;
        }

        MenuHolder holder = optionalHolder.get();

        holder.stopPlaceholderUpdate();
        holder.stopRefreshTask();

        if (executeCloseActions) {
            holder.getMenu().map(Menu::options).map(MenuOptions::closeHandler).flatMap(h -> h).ifPresent(h -> h.onClick(holder));
        }

        if (close) {
            plugin.getScheduler().runTask(player, () -> {
                holder.restorePlayerInventory();
                player.closeInventory();
                cleanInventory(plugin, player);
            });
        } else {
            holder.restorePlayerInventory();
        }
        menuHolders.remove(holder);
        lastOpenedMenus.put(player.getUniqueId(), holder.getMenu().orElse(null));
    }

    public static void closeMenuForShutdown(final @NotNull DeluxeMenus plugin, final @NotNull Player player) {
        getMenuHolder(player).ifPresent(holder -> {
            holder.stopPlaceholderUpdate();
            holder.restorePlayerInventory();
        });

        player.closeInventory();
        cleanInventory(plugin, player);
    }

    public static void closeMenu(final @NotNull DeluxeMenus plugin, final @NotNull Player player, final boolean close) {
        closeMenu(plugin, player, close, false);
    }

    private boolean hasOpenBypassPerm(final @NotNull Player viewer) {
        return viewer.hasPermission("deluxemenus.openrequirement.bypass." + this.options.name())
                || viewer.hasPermission("deluxemenus.openrequirement.bypass.*");
    }

    private boolean handleOpenRequirements(final @NotNull MenuHolder holder) {
        if (this.options.openRequirements().isEmpty()) {
            return true;
        }

        final RequirementList openRequirements = this.options.openRequirements().get();
        if (openRequirements.getRequirements() == null) {
            return true;
        }

        if (holder.getViewer() != null && (this.options.enableBypassPerm() && this.hasOpenBypassPerm(holder.getViewer()))) {
            return true;
        }

        if (!openRequirements.evaluate(holder)) {
            if (openRequirements.getDenyHandler() != null) {
                openRequirements.getDenyHandler().onClick(holder);
            }
            return false;
        }
        return true;
    }

    private boolean handleArgRequirements(final @NotNull MenuHolder holder) {
        for (RequirementList rl : this.options.argumentRequirements()) {
            if (rl.getRequirements() == null) {
                continue;
            }

            if (!rl.evaluate(holder)) {
                if (rl.getDenyHandler() != null) {
                    rl.getDenyHandler().onClick(holder);
                }
                return false;
            }
        }

        return true;
    }

    public void openMenu(final @NotNull Player viewer) {
        openMenu(viewer, null, null);
    }

    public void openMenu(final @NotNull Player viewer, final @Nullable Map<String, String> args, final @Nullable Player placeholderPlayer) {
        openMenu(viewer, args, placeholderPlayer, null);
    }

    public void openMenu(
            final @NotNull Player viewer,
            final @Nullable Map<String, String> args,
            final @Nullable Player placeholderPlayer,
            final @Nullable Menu playerInventoryMenu
    ) {
        if (this.options.subMenu()) {
            return;
        }

        if (items == null || items.isEmpty()) {
            return;
        }

        DeluxeMenusPreOpenMenuEvent preOpenEvent = new DeluxeMenusPreOpenMenuEvent(viewer);
        Bukkit.getPluginManager().callEvent(preOpenEvent);

        if (preOpenEvent.isCancelled()) {
            return;
        }

        final MenuHolder holder = new MenuHolder(plugin, viewer);
        if (placeholderPlayer != null) {
            holder.setPlaceholderPlayer(placeholderPlayer);
        }
        holder.setTypedArgs(args);
        holder.parsePlaceholdersInArguments(this.options.parsePlaceholdersInArguments());
        holder.parsePlaceholdersAfterArguments(this.options.parsePlaceholdersAfterArguments());
        if (playerInventoryMenu != null && this.options.playerInventoryMenu().isEmpty()) {
            holder.setPlayerInventoryMenu(playerInventoryMenu);
        }

        if (!this.handleArgRequirements(holder)) {
            return;
        }

        if (!this.handleOpenRequirements(holder)) {
            return;
        }

        scheduler.runTaskAsynchronously(() -> {

            Set<MenuItem> activeItems = getActiveItems(holder);

            if (activeItems.isEmpty()) {
                return;
            }

            holder.setMenuName(this.options.name());
            holder.setActiveItems(activeItems);

            this.options.openHandler().ifPresent(h -> h.onClick(holder));

            String title = StringUtils.color(holder.setPlaceholdersAndArguments(this.options.title()));

            Inventory inventory;

            if (this.options.type() != InventoryType.CHEST) {
                inventory = Bukkit.createInventory(holder, this.options.type(), title);
            } else {
                inventory = Bukkit.createInventory(holder, this.options.size(), title);
            }

            holder.setInventory(inventory);

            boolean update = false;
            final Map<Integer, ItemStack> playerInventoryItems = new HashMap<>();
            final boolean renderPlayerInventory = rendersPlayerInventory(holder);

            for (MenuItem item : activeItems) {

                ItemStack iStack = item.getItemStack(holder);

                if (iStack == null) {
                    continue;
                }

                iStack = plugin.getMenuItemMarker().mark(iStack);

                int slot = item.options().slot();

                if (slot >= this.options.size() + (renderPlayerInventory ? 36 : 0)) {
                    plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Item set to slot " + slot + " for menu: " + this.options.name() + " exceeds the inventory size!",
                            "This item will not be added to the menu!"
                    );
                    continue;
                }

                if (item.options().updatePlaceholders()) {
                    update = true;
                }

                if (slot < this.options.size()) {
                    inventory.setItem(item.options().slot(), iStack);
                } else {
                    playerInventoryItems.put(slot, iStack);
                }
            }

            final boolean updatePlaceholders = update;

            scheduler.runTask(viewer, () -> {
                if (options.refresh()) {
                    holder.startRefreshTask();
                }

                if (isInMenu(holder.getViewer())) {
                    closeMenu(plugin, holder.getViewer(), false);
                }

                if (renderPlayerInventory) {
                    holder.hidePlayerInventory();
                }

                final InventoryView view = viewer.openInventory(inventory);
                if (view != null && renderPlayerInventory) {
                    holder.applyPlayerInventoryItems(playerInventoryItems);
                }
                menuHolders.add(holder);

                if (updatePlaceholders) {
                    holder.startUpdatePlaceholdersTask();
                }
            });

            scheduler.runTask(viewer, () -> {
                DeluxeMenusOpenMenuEvent openEvent = new DeluxeMenusOpenMenuEvent(viewer, holder);
                Bukkit.getPluginManager().callEvent(openEvent);
            });
        });
    }

    public void refreshForAll() {
        menuHolders.stream().filter(menuHolder -> menuHolder.getMenuName().equalsIgnoreCase(options.name())).forEach(MenuHolder::refreshMenu);
    }

    public @NotNull Set<MenuItem> getActiveItems(final @NotNull MenuHolder holder) {
        final Set<MenuItem> activeItems = new HashSet<>(getActiveItems(holder, 0, this.options.size(), 0));
        final Optional<Menu> openPlayerInventoryMenu = holder.getPlayerInventoryMenu();

        if (!this.options.hidePlayerInventory()
                && openPlayerInventoryMenu.isEmpty()
                && this.options.playerInventoryMenu().isEmpty()) {
            return activeItems;
        }

        if (openPlayerInventoryMenu.isPresent()) {
            activeItems.addAll(openPlayerInventoryMenu.get().getActiveItems(holder, 0, 36, this.options.size()));
            return activeItems;
        }

        if (this.options.playerInventoryMenu().isPresent()) {
            final String playerInventoryMenuName = this.options.playerInventoryMenu().get();
            final Optional<Menu> bottomMenu = Menu.getSubMenuByName(playerInventoryMenuName);
            if (bottomMenu.isPresent()) {
                activeItems.addAll(bottomMenu.get().getActiveItems(holder, 0, 36, this.options.size()));
            } else {
                plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Player inventory menu " + playerInventoryMenuName + " for menu " + this.options.name() + " was not found."
                );
            }
            return activeItems;
        }

        return activeItems;
    }

    private boolean rendersPlayerInventory(final @NotNull MenuHolder holder) {
        return this.options.hidePlayerInventory()
                || holder.getPlayerInventoryMenu().isPresent()
                || this.options.playerInventoryMenu().flatMap(Menu::getSubMenuByName).isPresent();
    }

    private @NotNull Set<MenuItem> getActiveItems(final @NotNull MenuHolder holder, final int minimumSlot, final int maximumSlot, final int slotOffset) {
        final Set<MenuItem> activeItems = new HashSet<>();

        for (Entry<Integer, TreeMap<Integer, MenuItem>> entry : items.entrySet()) {
            final int configuredSlot = entry.getKey();

            if (configuredSlot < minimumSlot || configuredSlot >= maximumSlot) {
                continue;
            }

            for (MenuItem item : entry.getValue().values()) {
                if (item.options().viewRequirements().isPresent()) {
                    if (!item.options().viewRequirements().get().evaluate(holder)) {
                        continue;
                    }
                }

                final int renderedSlot = configuredSlot + slotOffset;
                activeItems.add(new MenuItem(plugin, item.options().asBuilder().slot(renderedSlot).build()));
                break;
            }
        }

        return activeItems;
    }

    public @NotNull Map<Integer, TreeMap<Integer, MenuItem>> getMenuItems() {
        return this.items;
    }

    public @NotNull Optional<String> getMenuCommandUsed(final @NotNull String command) {
        return this.options.commands().stream().filter(c -> c.equalsIgnoreCase(command)).findFirst();
    }

    public @NotNull MenuOptions options() {
        return this.options;
    }

    public @NotNull String path() {
        return this.path;
    }

    public int activeViewers() {
        return (int) menuHolders.stream().filter(holder -> holder.getMenuName().equalsIgnoreCase(options.name())).count();
    }

}
