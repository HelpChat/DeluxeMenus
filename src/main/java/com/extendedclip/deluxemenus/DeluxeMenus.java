package com.extendedclip.deluxemenus;

import com.extendedclip.deluxemenus.cache.SimpleCache;
import com.extendedclip.deluxemenus.command.DeluxeMenusCommand;
import com.extendedclip.deluxemenus.config.DeluxeMenusConfig;
import com.extendedclip.deluxemenus.config.GeneralConfig;
import com.extendedclip.deluxemenus.dupe.DupeFixer;
import com.extendedclip.deluxemenus.dupe.MenuItemMarker;
import com.extendedclip.deluxemenus.hooks.*;
import com.extendedclip.deluxemenus.listener.PlayerListener;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.menu.options.HeadType;
import com.extendedclip.deluxemenus.metrics.Metrics;
import com.extendedclip.deluxemenus.nbt.NbtProvider;
import com.extendedclip.deluxemenus.persistentmeta.PersistentMetaHandler;
import com.extendedclip.deluxemenus.placeholder.Expansion;
import com.extendedclip.deluxemenus.updatechecker.UpdateChecker;
import com.extendedclip.deluxemenus.utils.DebugLevel;
import com.extendedclip.deluxemenus.utils.Messages;
import com.extendedclip.deluxemenus.utils.VersionHelper;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
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

    public static final Map<String, Material> MATERIALS = Arrays.stream(Material.values()).collect(Collectors.toUnmodifiableMap(Enum::name, Function.identity()));

    private static final DebugLevel STACKTRACE_PRINT_LEVEL = DebugLevel.MEDIUM;

    private PersistentMetaHandler persistentMetaHandler;
    private MenuItemMarker menuItemMarker;

    private BukkitAudiences audiences;

    private VaultHook vaultHook;

    private ItemStack head;
    private Map<String, ItemHook> itemHooks;

    private final GeneralConfig generalConfig = new GeneralConfig(this);
    private DeluxeMenusConfig menuConfig;

    @Override
    public void onLoad() {
        if (NbtProvider.isAvailable()) {
            this.debug(DebugLevel.HIGHEST, Level.INFO, "NMS hook has been setup successfully!");
            return;
        }

        this.debug(DebugLevel.HIGHEST, Level.WARNING, "Could not setup a NMS hook for your server version!");
    }

    @Override
    public void onEnable() {
        this.generalConfig.load();

        if (!hookIntoPlaceholderAPI()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.persistentMetaHandler = new PersistentMetaHandler(this);
        this.menuItemMarker = new MenuItemMarker(this);
        new DupeFixer(this, this.menuItemMarker).register();

        this.audiences = BukkitAudiences.create(this);

        hookIntoVault();
        setUpItemHooks();

        this.menuConfig = new DeluxeMenusConfig(this);
        if (this.menuConfig.loadDefConfig()) {
            debug(DebugLevel.HIGHEST, Level.INFO, menuConfig.loadGUIMenus() + " GUI menus loaded!");
        } else {
            debug(DebugLevel.HIGHEST, Level.WARNING, "Failed to load from config.yml. Use /dm reload after fixing your errors.");
        }

        new PlayerListener(this).register();
        if (!new DeluxeMenusCommand(this).register()) {
            debug(DebugLevel.HIGHEST, Level.SEVERE, "Could not register the DeluxeMenus command!");
        }
        new Expansion(this).register();

        setUpBungeeCordMessaging();
        setUpUpdateChecker();
        setUpMetrics();
    }

    @Override
    public void onDisable() {
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");

        Bukkit.getScheduler().cancelTasks(this);

        if (this.audiences != null) {
            this.audiences.close();
            this.audiences = null;
        }

        Menu.unloadForShutdown(this);

        itemHooks.clear();

        HandlerList.unregisterAll(this);
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

    public boolean shouldPrintStackTrace() {
        return generalConfig.debugLevel().getPriority() <= STACKTRACE_PRINT_LEVEL.getPriority();
    }

    public void printStacktrace(final String message, final Throwable throwable) {
        if (!shouldPrintStackTrace()) return;

        this.getLogger().log(Level.SEVERE, message, throwable);
    }

    public void connect(Player p, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        try {
            out.writeUTF("Connect");
            out.writeUTF(server);
        } catch (Exception e) {
            debug(DebugLevel.HIGHEST, Level.SEVERE, "There was a problem attempting to send " + p.getName() + " to server " + server + "!");

            printStacktrace("There was a problem attempting to send " + p.getName() + " to server " + server + "!", e);
        }

        p.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }

    public void sms(CommandSender s, Component msg) {
        audiences().sender(s).sendMessage(msg);
    }

    public void sms(CommandSender s, Messages msg) {
        audiences().sender(s).sendMessage(msg.message());
    }

    public void debug(@NotNull final DebugLevel messageDebugLevel, @NotNull final Level level, @NotNull final String... messages) {
        this.debug(generalConfig.debugLevel(), messageDebugLevel, level, messages);
    }

    public void debug(@NotNull final DebugLevel generalDebugLevel, @NotNull final DebugLevel messageDebugLevel, @NotNull final Level level, @NotNull final String... messages) {
        if (generalDebugLevel.getPriority() > messageDebugLevel.getPriority()) return;

        this.getLogger().log(level, String.join(System.lineSeparator(), messages));
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

    public BukkitAudiences audiences() {
        if (this.audiences == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.audiences;
    }

    public void clearCaches() {
        itemHooks.values().stream().filter(Objects::nonNull).filter(hook -> hook instanceof SimpleCache).map(hook -> (SimpleCache) hook).forEach(SimpleCache::clearCache);
    }

    public void reload() {
        this.generalConfig.reload();
    }

    public GeneralConfig getGeneralConfig() {
        return generalConfig;
    }

    private boolean hookIntoPlaceholderAPI() {
        final boolean canHook = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        if (!canHook) {
            this.debug(DebugLevel.HIGHEST, Level.SEVERE, "Could not hook into PlaceholderAPI!", "DeluxeMenus will now disable!");
            return false;
        }

        this.debug(DebugLevel.HIGHEST, Level.INFO, "Successfully hooked into PlaceholderAPI!");
        return true;
    }

    private void hookIntoVault() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            return;
        }
        this.vaultHook = new VaultHook();

        if (this.vaultHook.hooked()) {
            this.debug(DebugLevel.HIGHEST, Level.INFO, "Successfully hooked into Vault!");
            return;
        }

        this.debug(DebugLevel.HIGHEST, Level.WARNING, "Could not hook into Vault!",
                "DeluxeMenus will continue to work but some features (such as the 'has money' requirement) may not be available.");
    }

    @SuppressWarnings("deprecation")
    private void setUpItemHooks() {
        if (!VersionHelper.IS_ITEM_LEGACY) {
            this.head = new ItemStack(Material.PLAYER_HEAD, 1);
        } else {
            this.head = new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) 3);
        }

        this.itemHooks = new HashMap<>();

        final NamedHeadHook namedHeadHook = new NamedHeadHook(this);
        namedHeadHook.register();
        this.itemHooks.put(HeadType.NAMED.getHookName(), namedHeadHook);
        this.itemHooks.put(HeadType.BASE64.getHookName(), new BaseHeadHook(this));
        this.itemHooks.put(HeadType.TEXTURE.getHookName(), new TextureHeadHook(this));

        if (Bukkit.getPluginManager().isPluginEnabled("HeadDatabase")) {
            try {
                Class.forName("me.arcaniax.hdb.api.HeadDatabaseAPI");
                this.itemHooks.put(HeadType.HDB.getHookName(), new HeadDatabaseHook(this));
            } catch (ClassNotFoundException ignored) {
                // We are looking for this specific class because we've had issues with other plugins being named HeadDatabase
                // in the past
            }
        }

        if (Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
            this.itemHooks.put("itemsadder", new ItemsAdderHook());
        }

        if (Bukkit.getPluginManager().isPluginEnabled("Oraxen")) {
            this.itemHooks.put("oraxen", new OraxenHook());
        }

        if (Bukkit.getPluginManager().isPluginEnabled("MMOItems")) {
            this.itemHooks.put("mmoitems", new MMOItemsHook(this));
        }

        if (Bukkit.getPluginManager().isPluginEnabled("ExecutableItems")) {
            this.itemHooks.put("executableitems", new ExecutableItemsHook());
        }

        if (Bukkit.getPluginManager().isPluginEnabled("ExecutableBlocks")) {
            this.itemHooks.put("executableblocks", new ExecutableBlocksHook());
        }

        if (Bukkit.getPluginManager().isPluginEnabled("SimpleItemGenerator")) {
            this.itemHooks.put("simpleitemgenerator", new SimpleItemGeneratorHook(this));
        }
    }

    private void setUpBungeeCordMessaging() {
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    private void setUpUpdateChecker() {
        if (!this.generalConfig.checkForUpdates()) {
            return;
        }

        final UpdateChecker updateChecker = new UpdateChecker(this);
        updateChecker.register();

        if (updateChecker.updateAvailable()) {
            this.debug(DebugLevel.HIGHEST, Level.INFO, "An update for DeluxeMenus (DeluxeMenus v" + updateChecker.getLatestVersion() + ")", "is available at https://www.spigotmc.org/resources/deluxemenus.11734/");
            return;
        }

        this.debug(DebugLevel.HIGHEST, Level.INFO, "You are running the latest version of DeluxeMenus!");
    }

    private void setUpMetrics() {
        final Metrics metrics = new Metrics(this, 445);
    }
}
