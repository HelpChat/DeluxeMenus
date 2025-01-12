package com.extendedclip.deluxemenus.menu;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.command.RegistrableMenuCommand;
import com.extendedclip.deluxemenus.menu.options.MenuOptions;
import com.extendedclip.deluxemenus.requirement.RequirementList;
import com.extendedclip.deluxemenus.utils.DebugLevel;
import com.extendedclip.deluxemenus.utils.StringUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Menu {

    private static final Map<String, Menu> menus = new HashMap<>();
    private static final Set<MenuHolder> menuHolders = new HashSet<>();
    private static final Map<UUID, Menu> lastOpenedMenus = new HashMap<>();

    private final DeluxeMenus plugin;
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
        this.options = options;
        this.items = items;
        this.path = path;

        if (this.options.registerCommands()) {
            this.command = new RegistrableMenuCommand(plugin, this);
            this.command.register();
        }

        menus.put(this.options.name(), this);
    }

    public static void unload(final @NotNull DeluxeMenus plugin, final @NotNull String name) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isInMenu(p, name)) {
                closeMenu(plugin, p, true);
            }
        }

        Optional<Menu> optionalMenu = Menu.getMenuByName(name);
        if (optionalMenu.isEmpty()) {
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
        menus.clear();
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
    }

    public static int getLoadedMenuSize() {
        return menus.size();
    }

    public static @NotNull Collection<Menu> getAllMenus() {
        return menus.values();
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

        if (executeCloseActions) {
            holder.getMenu().map(Menu::options).map(MenuOptions::closeHandler).flatMap(h -> h).ifPresent(h -> h.onClick(holder));
        }

        if (close) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.closeInventory();
                cleanInventory(plugin, player);
            });
        }
        menuHolders.remove(holder);
        lastOpenedMenus.put(player.getUniqueId(), holder.getMenu().orElse(null));
    }

    public static void closeMenuForShutdown(final @NotNull DeluxeMenus plugin, final @NotNull Player player) {
        getMenuHolder(player).ifPresent(MenuHolder::stopPlaceholderUpdate);

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

        if (holder.getViewer() != null && this.hasOpenBypassPerm(holder.getViewer())) {
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
        if (items == null || items.isEmpty()) {
            return;
        }

        final MenuHolder holder = new MenuHolder(plugin, viewer);
        if (placeholderPlayer != null) {
            holder.setPlaceholderPlayer(placeholderPlayer);
        }
        holder.setTypedArgs(args);
        holder.parsePlaceholdersInArguments(this.options.parsePlaceholdersInArguments());
        holder.parsePlaceholdersAfterArguments(this.options.parsePlaceholdersAfterArguments());

        if (!this.handleArgRequirements(holder)) {
            return;
        }

        if (!this.handleOpenRequirements(holder)) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            Set<MenuItem> activeItems = new HashSet<>();

            for (Entry<Integer, TreeMap<Integer, MenuItem>> entry : items.entrySet()) {

                for (MenuItem item : entry.getValue().values()) {

                    int slot = item.options().slot();

                    if (slot >= this.options.size()) {
                        plugin.debug(
                                DebugLevel.HIGHEST,
                                Level.WARNING,
                                "Item set to slot " + slot + " for menu: " + this.options.name() + " exceeds the inventory size!",
                                "This item will not be added to the menu!"
                        );
                        continue;
                    }

                    if (item.options().viewRequirements().isPresent()) {

                        if (item.options().viewRequirements().get().evaluate(holder)) {

                            activeItems.add(item);
                            break;
                        }
                    } else {

                        activeItems.add(item);
                        break;
                    }
                }
            }

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

            for (MenuItem item : activeItems) {

                ItemStack iStack = item.getItemStack(holder);

                if (iStack == null) {
                    continue;
                }

                iStack = plugin.getMenuItemMarker().mark(iStack);

                int slot = item.options().slot();

                if (slot >= this.options.size()) {
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

                inventory.setItem(item.options().slot(), iStack);
            }

            final boolean updatePlaceholders = update;

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (isInMenu(holder.getViewer())) {
                    closeMenu(plugin, holder.getViewer(), false);
                }

                viewer.openInventory(inventory);
                menuHolders.add(holder);

                if (updatePlaceholders) {
                    holder.startUpdatePlaceholdersTask();
                }
            });
        });
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
}
