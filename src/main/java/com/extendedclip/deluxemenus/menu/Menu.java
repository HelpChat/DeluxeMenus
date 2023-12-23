package com.extendedclip.deluxemenus.menu;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.action.ClickHandler;
import com.extendedclip.deluxemenus.dupe.MenuItemMarker;
import com.extendedclip.deluxemenus.requirement.RequirementList;
import com.extendedclip.deluxemenus.utils.DebugLevel;
import com.extendedclip.deluxemenus.utils.StringUtils;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import me.clip.placeholderapi.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class Menu extends Command {

  private static final Map<String, Menu> menus = new HashMap<>();
  private static final Set<MenuHolder> holders = new HashSet<>();
  private static CommandMap commandMap = null;
  private final String menuName;
  private final String menuTitle;
  private final int size;
  private final Map<Integer, TreeMap<Integer, MenuItem>> items;
  private InventoryType type;
  private List<String> menuCommands;
  private int updateInterval;
  private RequirementList openRequirements;
  private ClickHandler openHandler, closeHandler;
  private boolean registersCommand;
  // args
  private List<String> args;
  private String argUsageMessage;

  public Menu(String menuName, String menuTitle, Map<Integer, TreeMap<Integer, MenuItem>> items,
      int size, List<String> menuCommands, boolean registerCommand, List<String> args) {
    super(menuCommands.get(0));
    this.menuName = menuName;
    this.menuTitle = StringUtils.color(menuTitle);
    this.items = items;
    this.size = size;
    this.menuCommands = menuCommands;
    this.registersCommand = registerCommand;
    this.args = args;
    if (registerCommand) {
      if (menuCommands.size() > 1) {
        this.setAliases(menuCommands.subList(1, menuCommands.size()));
      }
      addCommand();
    }
    menus.put(this.menuName, this);
  }

  public Menu(String menuName, String menuTitle, Map<Integer, TreeMap<Integer, MenuItem>> items,
      int size) {
    super(menuName);
    this.menuName = menuName;
    this.menuTitle = StringUtils.color(menuTitle);
    this.items = items;
    this.size = size;
    menus.put(this.menuName, this);
  }

  public static void unload(String menu) {
    for (Player p : Bukkit.getOnlinePlayers()) {
      if (inMenu(p, menu)) {
        closeMenu(p, true);
      }
    }
    Menu m = Menu.getMenu(menu);
    if (m == null) {
      return;
    }

    m.removeCommand();
    menus.remove(menu);
  }

  public static void unload() {
    for (Player p : Bukkit.getOnlinePlayers()) {
      if (inMenu(p)) {
        closeMenu(p, true);
      }
    }
    if (Menu.getAllMenus() != null) {
      for (Menu menu : Menu.getAllMenus()) {
        menu.removeCommand();
      }
    }
    menus.clear();
    holders.clear();
  }

  public static void unloadForShutdown() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      if (inMenu(player)) {
        closeMenuForShutdown(player);
      }
    }
    menus.clear();
  }

  public static int getLoadedMenuSize() {
    return menus.size();
  }

  public static Menu getMenu(Player p) {
    return getOpenMenu(p);
  }

  public static Collection<Menu> getAllMenus() {
    return !menus.isEmpty() ? menus.values() : null;
  }

  public static Menu getMenu(String menuName) {
    for (Entry<String, Menu> e : menus.entrySet()) {
      if (e.getKey().equalsIgnoreCase(menuName)) {
        return e.getValue();
      }
    }
    return null;
  }

  public static Menu getMenuByCommand(String command) {
    for (Menu m : menus.values()) {
      if (m.getMenuCommandUsed(command) != null) {
        return m;
      }
    }
    return null;
  }

  public static boolean isMenuCommand(String command) {
    return getMenuByCommand(command) != null;
  }

  public static boolean inMenu(Player p) {
    return holders.stream().anyMatch(h -> h.getViewerName().equals(p.getName()));
  }

  public static boolean inMenu(Player p, String menu) {
    return holders.stream().anyMatch(h -> h.getMenuName().equals(menu) && h.getViewerName().equals(p.getName()));
  }

  public static MenuHolder getMenuHolder(Player p) {
    return holders.stream().filter(h -> h.getViewerName().equals(p.getName())).findFirst()
        .orElse(null);
  }

  public static Menu getOpenMenu(Player p) {
    MenuHolder h = getMenuHolder(p);
    return h == null ? null : h.getMenu();
  }

  public static void cleanInventory(Player player, @NotNull final MenuItemMarker marker) {
    if (player == null) {
      return;
    }
    for (final ItemStack itemStack : player.getInventory().getContents()) {
      if (itemStack == null) continue;
      if (!marker.isMarked(itemStack)) continue;

      DeluxeMenus.debug(
              DebugLevel.LOWEST,
              Level.INFO,
                "Found a DeluxeMenus item in a player's inventory. Removing it."
      );
      player.getInventory().remove(itemStack);
    }
    player.updateInventory();
  }

  public static void closeMenu(final Player p, boolean close, boolean executeCloseActions) {

    MenuHolder holder = getMenuHolder(p);
    if (holder == null) {
      return;
    }

    holder.stopPlaceholderUpdate();

    if (executeCloseActions) {
      if (holder.getMenu().getCloseHandler() != null) {
        holder.getMenu().getCloseHandler().onClick(holder);
      }
    }

    if (close) {
      Bukkit.getScheduler().runTask(DeluxeMenus.getInstance(), () -> {
        p.closeInventory();
        cleanInventory(p, DeluxeMenus.getInstance().getMenuItemMarker());
      });
    }
    holders.remove(holder);
  }

  public static void closeMenuForShutdown(final Player p) {
    MenuHolder holder = getMenuHolder(p);
    if (holder == null) {
      return;
    }

    holder.stopPlaceholderUpdate();

    p.closeInventory();
    cleanInventory(p, DeluxeMenus.getInstance().getMenuItemMarker());
  }

  public static void closeMenu(final Player p, boolean close) {
    closeMenu(p, close, false);
  }

  private void addCommand() {
    if (commandMap == null) {
      try {
        final Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        f.setAccessible(true);
        commandMap = (CommandMap) f.get(Bukkit.getServer());
      } catch (Exception e) {
        e.printStackTrace();
        return;
      }
    }
    boolean registered = commandMap.register("DeluxeMenus", this);
    if (registered) {
      DeluxeMenus.debug(
          DebugLevel.LOW,
          Level.INFO,
          "Registered command: " + this.getName() + " for menu: " + this.getMenuName()
      );
    }
  }

  private void removeCommand() {
    if (commandMap != null && this.registersCommand()) {
      Field cMap;
      Field knownCommands;
      try {
        cMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        cMap.setAccessible(true);
        knownCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");
        knownCommands.setAccessible(true);
        ((Map<String, Command>) knownCommands.get((SimpleCommandMap) cMap.get(Bukkit.getServer())))
            .remove(this.getName());
        boolean unregistered = this.unregister((CommandMap) cMap.get(Bukkit.getServer()));
        this.unregister(commandMap);
        if (unregistered) {
          DeluxeMenus.debug(
              DebugLevel.HIGH,
              Level.INFO,
              "Successfully unregistered command: " + this.getName()
          );
        } else {
          DeluxeMenus.debug(
              DebugLevel.HIGHEST,
              Level.WARNING,
              "Failed to unregister command: " + this.getName()
          );
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  @Override
  public boolean execute(CommandSender sender, String commandLabel, String[] typedArgs) {
    if (!(sender instanceof Player)) {
      Msg.msg(sender, "Menus can only be opened by players!");
      return true;
    }

    Map<String, String> argMap = null;

    if (this.args != null) {
      if (typedArgs.length < this.args.size()) {
        if (this.argUsageMessage != null) {
          Msg.msg(sender, this.argUsageMessage);
        }
        return true;
      }
      argMap = new HashMap<>();
      int index = 0;
      for (String arg : this.args) {
        if (index + 1 == this.args.size()) {
          String last = String.join(" ", Arrays.asList(typedArgs).subList(index, typedArgs.length));
          argMap.put(arg, last);
        } else {
          argMap.put(arg, typedArgs[index]);
        }
        index++;
      }
    }

    Player player = (Player) sender;
    openMenu(player, argMap, null);
    return true;
  }

  private boolean hasOpenBypassPerm(Player viewer) {
    return viewer.hasPermission("deluxemenus.openrequirement.bypass." + menuName)
        || viewer.hasPermission("deluxemenus.openrequirement.bypass.*");
  }

  private boolean handleOpenRequirements(MenuHolder holder) {
    if (openRequirements == null || openRequirements.getRequirements() == null) {
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

  public void openMenu(final Player viewer) {
    openMenu(viewer, null, null);
  }

  public void openMenu(final Player viewer, final Map<String, String> args, final Player placeholderPlayer) {
    if (menuTitle == null || items == null || items.size() <= 0) {
      return;
    }

    final MenuHolder holder = new MenuHolder(viewer);
    if (placeholderPlayer != null) holder.setPlaceholderPlayer(placeholderPlayer);
    holder.setTypedArgs(args);

    if (!this.handleOpenRequirements(holder)) {
      return;
    }

    Bukkit.getScheduler().runTaskAsynchronously(DeluxeMenus.getInstance(), () -> {

      Set<MenuItem> activeItems = new HashSet<>();

      for (Entry<Integer, TreeMap<Integer, MenuItem>> entry : items.entrySet()) {

        for (MenuItem item : entry.getValue().values()) {

          int slot = item.getSlot();

          if (slot >= size) {
            DeluxeMenus.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "Item set to slot " + slot + " for menu: " + menuName + " exceeds the inventory size!",
                "This item will not be added to the menu!"
            );
            continue;
          }

          if (item.hasViewRequirement()) {

            if (item.getViewRequirements().evaluate(holder)) {

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

      holder.setMenuName(menuName);
      holder.setActiveItems(activeItems);

      if (this.openHandler != null) {
        this.openHandler.onClick(holder);
      }

      String title = StringUtils.color(holder.setPlaceholders(this.menuTitle));

      Inventory inventory;

      if (type != null) {
        inventory = Bukkit.createInventory(holder, type, title);
      } else {
        inventory = Bukkit.createInventory(holder, size, title);
      }

      holder.setInventory(inventory);

      boolean update = false;

      for (MenuItem item : activeItems) {

        ItemStack iStack = item.getItemStack(holder);

        if (iStack == null) {
          continue;
        }

        iStack = DeluxeMenus.getInstance().getMenuItemMarker().mark(iStack);

        int slot = item.getSlot();

        if (slot >= size) {
          DeluxeMenus.debug(
              DebugLevel.HIGHEST,
              Level.WARNING,
              "Item set to slot " + slot + " for menu: " + menuName + " exceeds the inventory size!",
              "This item will not be added to the menu!"
          );
          continue;
        }

        if (item.updatePlaceholders()) {
          update = true;
        }

        inventory.setItem(item.getSlot(), iStack);
      }

      final boolean updatePlaceholders = update;

      Bukkit.getScheduler().runTask(DeluxeMenus.getInstance(), () -> {
        if (inMenu(holder.getViewer())) {
          closeMenu(holder.getViewer(), false);
        }

        viewer.openInventory(inventory);
        holders.add(holder);

        if (updatePlaceholders) {
          holder.startUpdatePlaceholdersTask();
        }
      });
    });
  }

  public String getMenuTitle() {
    return menuTitle;
  }

  public String getMenuName() {
    return this.menuName;
  }

  public List<String> getMenuCommands() {
    return this.menuCommands;
  }

  public Map<Integer, TreeMap<Integer, MenuItem>> getMenuItems() {
    return this.items;
  }

  public int getSize() {
    return this.size;
  }

  public int getUpdateInterval() {
    return updateInterval >= 1 ? updateInterval : 10;
  }

  public void setUpdateInterval(int updateInterval) {
    this.updateInterval = updateInterval;
  }

  public String getMenuCommandUsed(String command) {
    if (getMenuCommands() == null) {
      return null;
    }
    for (String c : getMenuCommands()) {
      if (command.equalsIgnoreCase(c)) {
        return c;
      }
    }
    return null;
  }

  public RequirementList getOpenRequirements() {
    return openRequirements;
  }

  public void setOpenRequirements(RequirementList openRequirements) {
    this.openRequirements = openRequirements;
  }

  public InventoryType getInventoryType() {
    return type;
  }

  public void setInventoryType(InventoryType type) {
    this.type = type;
  }

  public ClickHandler getOpenHandler() {
    return openHandler;
  }

  public void setOpenHandler(ClickHandler openHandler) {
    this.openHandler = openHandler;
  }

  public ClickHandler getCloseHandler() {
    return closeHandler;
  }

  public void setCloseHandler(ClickHandler closeHandler) {
    this.closeHandler = closeHandler;
  }

  public boolean registersCommand() {
    return registersCommand;
  }

  public String getArgUsageMessage() {
    return argUsageMessage;
  }

  public void setArgUsageMessage(String argUsageMessage) {
    this.argUsageMessage = argUsageMessage;
  }
}
