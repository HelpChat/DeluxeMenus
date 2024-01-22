package com.extendedclip.deluxemenus;

import com.extendedclip.deluxemenus.cache.SimpleCache;
import com.extendedclip.deluxemenus.commands.DeluxeMenusCommands;
import com.extendedclip.deluxemenus.config.DeluxeMenusConfig;
import com.extendedclip.deluxemenus.dupe.DupeFixer;
import com.extendedclip.deluxemenus.dupe.MenuItemMarker;
import com.extendedclip.deluxemenus.hooks.*;
import com.extendedclip.deluxemenus.listener.PlayerListener;
import com.extendedclip.deluxemenus.menu.HeadType;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.metrics.Metrics;
import com.extendedclip.deluxemenus.nbt.NbtProvider;
import com.extendedclip.deluxemenus.persistentmeta.PersistentMetaHandler;
import com.extendedclip.deluxemenus.placeholder.Expansion;
import com.extendedclip.deluxemenus.updatechecker.UpdateChecker;
import com.extendedclip.deluxemenus.utils.DebugLevel;
import com.extendedclip.deluxemenus.utils.Messages;
import com.extendedclip.deluxemenus.utils.VersionHelper;
import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class DeluxeMenus extends JavaPlugin {

  public final static Map<String, Material> MATERIALS
          = Arrays.stream(Material.values()).collect(Collectors.toUnmodifiableMap(Enum::name, Function.identity()));
  private static DeluxeMenus instance;
  private static DebugLevel debugLevel = DebugLevel.LOWEST;
  private DeluxeMenusConfig menuConfig;
  private Map<String, ItemHook> itemHooks;
  private VaultHook vaultHook;
  private boolean checkUpdates;
  private ItemStack head;
  private PersistentMetaHandler persistentMetaHandler;
  private MenuItemMarker menuItemMarker;
  private DupeFixer dupeFixer;
  private BukkitAudiences adventure;
  private TaskScheduler universalScheduler;

  @Override
  public void onLoad() {
    instance = this;

    this.persistentMetaHandler = new PersistentMetaHandler();
    
    if (NbtProvider.isAvailable()) {
      DeluxeMenus.debug(
          DebugLevel.HIGHEST,
          Level.INFO,
          "NMS hook has been setup successfully!"
      );
      return;
    }

    debug(
        DebugLevel.HIGHEST,
        Level.WARNING,
        "Could not setup a NMS hook for your server version!"
    );
  }

  @SuppressWarnings("deprecation")
  @Override
  public void onEnable() {
    if (!hookPlaceholderAPI()) {
      debug(
          DebugLevel.HIGHEST,
          Level.SEVERE,
          "Could not hook into PlaceholderAPI!",
          "DeluxeMenus will now disable!"
      );
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    } else {
      debug(
          DebugLevel.HIGHEST,
          Level.INFO,
          "Successfully hooked into PlaceholderAPI!"
      );
    }
    this.universalScheduler = UniversalScheduler.getScheduler(this);
    menuItemMarker = new MenuItemMarker(this);
    dupeFixer = new DupeFixer(this, menuItemMarker);

    this.adventure = BukkitAudiences.create(this);

    setupItemHooks();

    if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
      vaultHook = new VaultHook();

      if (vaultHook.hooked()) {
        debug(
            DebugLevel.HIGHEST,
            Level.INFO,
            "Successfully hooked into Vault!"
        );
      }
    }

    if (!VersionHelper.IS_ITEM_LEGACY) {
      head = new ItemStack(Material.PLAYER_HEAD, 1);
    } else {
      head = new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) 3);
    }

    menuConfig = new DeluxeMenusConfig(this);
    if (menuConfig.loadDefConfig()) {
      debugLevel(menuConfig.debugLevel());
      checkUpdates = getConfig().getBoolean("check_updates");
      debug(
          DebugLevel.HIGHEST,
          Level.INFO,
          menuConfig.loadGUIMenus() + " GUI menus loaded!"
      );
    } else {
      debug(
          DebugLevel.HIGHEST,
          Level.WARNING,
          "Failed to load from config.yml. Use /dm reload after fixing your errors."
      );
    }

    new PlayerListener(this);
    new DeluxeMenusCommands(this);
    Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

    if (checkUpdates) {
      UpdateChecker updateChecker = new UpdateChecker(this);

      if (updateChecker.updateAvailable()) {
        debug(
            DebugLevel.HIGHEST,
            Level.INFO,
            "An update for DeluxeMenus (DeluxeMenus v" + updateChecker.getLatestVersion() + ")",
            "is available at https://www.spigotmc.org/resources/deluxemenus.11734/"
        );
      } else {
        debug(
            DebugLevel.HIGHEST,
            Level.INFO,
            "You are running the latest version of DeluxeMenus!"
        );
      }
    }

    startMetrics();

    new Expansion(this).register();
  }

  @Override
  public void onDisable() {
    Bukkit.getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");
    universalScheduler.cancelTasks(this);

    if (this.adventure != null) {
      this.adventure.close();
      this.adventure = null;
    }

    Menu.unloadForShutdown();

    itemHooks.clear();

    instance = null;
  }

  private void setupItemHooks() {
    itemHooks = new HashMap<>();

    if (PlaceholderAPIPlugin.getServerVersion().isSpigot()) {
      itemHooks.put(HeadType.NAMED.getHookName(), new NamedHeadHook(this));
      itemHooks.put(HeadType.BASE64.getHookName(), new BaseHeadHook());
      itemHooks.put(HeadType.TEXTURE.getHookName(), new TextureHeadHook());
    }

    if (Bukkit.getPluginManager().isPluginEnabled("HeadDatabase")) {
      try {
        Class.forName("me.arcaniax.hdb.api.HeadDatabaseAPI");
        itemHooks.put(HeadType.HDB.getHookName(), new HeadDatabaseHook());
      } catch (ClassNotFoundException ignored) {}
    }

    if (Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
      itemHooks.put("itemsadder", new ItemsAdderHook());
    }

    if (Bukkit.getPluginManager().isPluginEnabled("Oraxen")) {
      itemHooks.put("oraxen", new OraxenHook());
    }

    if (Bukkit.getPluginManager().isPluginEnabled("MMOItems")) {
      itemHooks.put("mmoitems", new MMOItemsHook());
    }

    if (Bukkit.getPluginManager().isPluginEnabled("ExecutableItems")) {
      itemHooks.put("executableitems", new ExecutableItemsHook());
    }

    if (Bukkit.getPluginManager().isPluginEnabled("ExecutableBlocks")) {
      itemHooks.put("executableblocks", new ExecutableBlocksHook());
    }

  }

  public Optional<ItemHook> getItemHook(String id) {
    return Optional.ofNullable(itemHooks.get(id));
  }

  public Map<String, ItemHook> getItemHooks() {
    return itemHooks;
  }

  public ItemStack getHead() {
    return head != null ? head : new ItemStack(Material.DIRT, 1);
  }

  public static DeluxeMenus getInstance() {
    return instance;
  }

  public static DebugLevel debugLevel() {
    return debugLevel;
  }

  public static void debugLevel(final DebugLevel level) {
    debugLevel = level;
  }

  public static DebugLevel printStacktraceLevel() {
    return DebugLevel.MEDIUM;
  }

  public static boolean shouldPrintStackTrace() {
    return debugLevel().getPriority() <= printStacktraceLevel().getPriority();
  }

  public static void printStacktrace(final String message, final Throwable throwable) {
    if (!shouldPrintStackTrace()) return;

    getInstance().getLogger().log(Level.SEVERE, message, throwable);
  }

  private void startMetrics() {
    Metrics metrics = new Metrics(this, 445);
    metrics.addCustomChart(new Metrics.SingleLineChart("menus", Menu::getLoadedMenuSize));
  }

  public void connect(Player p, String server) {
    ByteArrayDataOutput out = ByteStreams.newDataOutput();

    try {
      out.writeUTF("Connect");
      out.writeUTF(server);
    } catch (Exception e) {
      debug(
          DebugLevel.HIGHEST,
          Level.SEVERE,
          "There was a problem attempting to send " + p.getName() + " to server " + server + "!"
      );

      printStacktrace(
          "There was a problem attempting to send " + p.getName() + " to server " + server + "!",
          e
      );
    }

    p.sendPluginMessage(this, "BungeeCord", out.toByteArray());
  }

  public void sms(CommandSender s, Component msg) {
      adventure().sender(s).sendMessage(msg);
  }

  public void sms(CommandSender s, Messages msg) {
    adventure().sender(s).sendMessage(msg.message());
  }

  public static void debug(@NotNull final DebugLevel debugLevel, @NotNull final Level level, @NotNull final String... messages) {
    if (debugLevel().getPriority() > debugLevel.getPriority()) return;

    getInstance().getLogger().log(
        level,
        String.join(System.lineSeparator(), messages)
    );
  }

  private boolean hookPlaceholderAPI() {
    return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
  }

  public MenuItemMarker getMenuItemMarker() {
    return menuItemMarker;
  }

  public DeluxeMenusConfig getConfiguration() {
    return menuConfig;
  }

  public VaultHook getVault() {
    return vaultHook;
  }

  public PersistentMetaHandler getPersistentMetaHandler() {
    return persistentMetaHandler;
  }

  public BukkitAudiences adventure() {
    if (this.adventure == null) {
      throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
    }
    return this.adventure;
  }

  public TaskScheduler getUniversalScheduler() {
    return universalScheduler;
  }

  public void clearCaches() {
    itemHooks.values().stream()
            .filter(Objects::nonNull)
            .filter(hook -> hook instanceof SimpleCache)
            .map(hook -> (SimpleCache) hook)
            .forEach(SimpleCache::clearCache);
  }
}
