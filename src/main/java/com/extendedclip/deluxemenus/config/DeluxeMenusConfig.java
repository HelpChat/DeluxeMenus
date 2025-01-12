package com.extendedclip.deluxemenus.config;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.action.ActionType;
import com.extendedclip.deluxemenus.action.ClickAction;
import com.extendedclip.deluxemenus.action.ClickActionTask;
import com.extendedclip.deluxemenus.action.ClickHandler;
import com.extendedclip.deluxemenus.hooks.ItemHook;
import com.extendedclip.deluxemenus.menu.options.LoreAppendMode;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.menu.MenuHolder;
import com.extendedclip.deluxemenus.menu.MenuItem;
import com.extendedclip.deluxemenus.menu.options.MenuItemOptions;
import com.extendedclip.deluxemenus.menu.options.MenuOptions;
import com.extendedclip.deluxemenus.requirement.HasExpRequirement;
import com.extendedclip.deluxemenus.requirement.HasItemRequirement;
import com.extendedclip.deluxemenus.requirement.HasMetaRequirement;
import com.extendedclip.deluxemenus.requirement.HasMoneyRequirement;
import com.extendedclip.deluxemenus.requirement.HasPermissionRequirement;
import com.extendedclip.deluxemenus.requirement.HasPermissionsRequirement;
import com.extendedclip.deluxemenus.requirement.InputResultRequirement;
import com.extendedclip.deluxemenus.requirement.IsNearRequirement;
import com.extendedclip.deluxemenus.requirement.IsObjectRequirement;
import com.extendedclip.deluxemenus.requirement.JavascriptRequirement;
import com.extendedclip.deluxemenus.requirement.RegexMatchesRequirement;
import com.extendedclip.deluxemenus.requirement.Requirement;
import com.extendedclip.deluxemenus.requirement.RequirementList;
import com.extendedclip.deluxemenus.requirement.RequirementType;
import com.extendedclip.deluxemenus.requirement.StringLengthRequirement;
import com.extendedclip.deluxemenus.requirement.wrappers.ItemWrapper;
import com.extendedclip.deluxemenus.utils.DebugLevel;
import com.extendedclip.deluxemenus.utils.ItemUtils;
import com.extendedclip.deluxemenus.utils.LocationUtils;
import com.extendedclip.deluxemenus.utils.VersionHelper;
import com.google.common.base.Enums;
import com.google.common.primitives.Ints;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.extendedclip.deluxemenus.utils.Constants.PLACEHOLDER_PREFIX;
import static com.extendedclip.deluxemenus.utils.Constants.PLAYER_ITEMS;
import static com.extendedclip.deluxemenus.utils.Constants.WATER_BOTTLE;

public class DeluxeMenusConfig {

    public static final List<String> VALID_MATERIALS = new ArrayList<>();
    public static final List<String> VALID_MATERIAL_PREFIXES = new ArrayList<>();
    public static final Pattern DELAY_MATCHER = Pattern.compile("<delay=([^<>]+)>", Pattern.CASE_INSENSITIVE);
    public static final Pattern CHANCE_MATCHER = Pattern.compile("<chance=([^<>]+)>", Pattern.CASE_INSENSITIVE);
    public static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%((?<identifier>[a-zA-Z0-9]+)_)(?<parameters>[^%]+)%");
    private static final List<InventoryType> VALID_INVENTORY_TYPES = VersionHelper.getValidInventoryTypes();

    static {
        VALID_MATERIALS.addAll(PLAYER_ITEMS);
        VALID_MATERIALS.add(WATER_BOTTLE);

        VALID_MATERIAL_PREFIXES.add(PLACEHOLDER_PREFIX);
    }

    private final String separator = File.separator;
    private final File menuDirectory;
    private final DeluxeMenus plugin;
    private final List<String> exampleMenus = Arrays.asList("basics_menu", "advanced_menu", "requirements_menu"
            // more example menus here
    );

    public DeluxeMenusConfig(@NotNull final DeluxeMenus plugin) {
        VALID_MATERIAL_PREFIXES.addAll(plugin.getItemHooks().values().stream().map(ItemHook::getPrefix).collect(Collectors.toList()));

        this.plugin = plugin;
        menuDirectory = new File(this.plugin.getDataFolder() + separator + "gui_menus");
        try {
            if (menuDirectory.mkdirs()) {
                plugin.debug(DebugLevel.HIGH, Level.INFO, "Individual menus directory did not exist.", "Created directory: plugins" + separator + "DeluxeMenus" + separator + "gui_menus");
            }
        } catch (SecurityException e) {
            plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Something went wrong while creating directory: plugins" + separator + "DeluxeMenus" + separator + "gui_menus");
        }
    }

    private static boolean isValidMaterial(final @NotNull String material) {
        final String lowercaseMaterial = material.toLowerCase(Locale.ROOT);
        if (VALID_MATERIALS.contains(lowercaseMaterial)) {
            return true;
        }

        if (Material.getMaterial(material.toUpperCase(Locale.ROOT)) != null) {
            return true;
        }

        for (final String prefix : VALID_MATERIAL_PREFIXES) {
            if (lowercaseMaterial.startsWith(prefix)) {
                final String[] split = lowercaseMaterial.split("-");
                return split.length >= 2;
            }
        }

        return false;
    }

    public static boolean containsPlaceholders(String text) {
        return PLACEHOLDER_PATTERN.matcher(text).find();
    }

    private List<String> getStringListFromConfig(FileConfiguration config, String path) {
        if (!config.contains(path)) {
            return Collections.emptyList();
        }

        if (config.isList(path)) {
            return config.getStringList(path);
        } else {
            return Collections.singletonList(config.getString(path, ""));
        }
    }

    public boolean loadDefConfig() {
        if (checkConfig(null, "config.yml", true) == null) {
            return false;
        }

        FileConfiguration c = plugin.getConfig();

        c.options().header(
                "DeluxeMenus " + plugin.getDescription().getVersion() + " main configuration file" +
                "\n" +
                "\nA full wiki on how to use this plugin can be found at:" +
                "\nhttps://wiki.helpch.at/helpchat-plugins/deluxemenus" +
                "\n"
        );
        c.addDefault("debug", "LOW");
        c.addDefault("check_updates", true);
        c.addDefault("use_admin_commands_in_menus_list", false);
        c.addDefault("menus_list_page_size", 10);
        c.options().copyDefaults(true);

        if (!c.contains("gui_menus")) {
            createMenuExamples(c);
        } else {
            plugin.saveConfig();
            plugin.reloadConfig();
        }

        return true;
    }

    private void createMenuExamples(FileConfiguration c) {
        for (String name : exampleMenus) {
            File menuFile = new File(menuDirectory.getPath(), name + ".yml");
            try {
                menuFile.createNewFile();
            } catch (IOException e) {
                plugin.printStacktrace("Failed to create example menus!", e);
                continue;
            }
            saveResourceToFile(name + ".yml", menuFile);
            c.set("gui_menus." + name + ".file", menuFile.getName());
        }
        plugin.saveConfig();
        plugin.reloadConfig();
    }

    private boolean saveResourceToFile(String resource, File file) {
        try {
            InputStream is = plugin.getResource(resource);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            OutputStream os = new FileOutputStream(file);
            os.write(buffer);
            return true;
        } catch (NullPointerException | IOException ex) {
            plugin.printStacktrace("Failed to update file: " + resource, ex);
            plugin.debug(DebugLevel.HIGHEST, Level.SEVERE, "Failed to save default settings for:" + file.getName() + " from resource:" + resource);
        }
        return false;
    }

    public FileConfiguration checkConfig(String folder, String fileName, boolean create) {
        File directory;

        if (folder != null) {
            directory = new File(plugin.getDataFolder() + separator + folder + separator);
        } else {
            directory = new File(plugin.getDataFolder() + separator);
        }

        try {
            if (!directory.exists()) {
                return null;
            }
        } catch (SecurityException e) {
            return null;
        }

        File configFile = new File(directory.getPath(), fileName);
        if (create) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                plugin.printStacktrace("Failed to create file: " + fileName, e);
                return null;
            }
        }

        if (!configFile.exists()) {
            return null;
        }

        return checkConfig(configFile);
    }

    private FileConfiguration checkConfig(File f) {
        FileConfiguration config = new YamlConfiguration();

        try {
            config.load(f);
            return config;
        } catch (IOException e) {
            plugin.debug(DebugLevel.HIGHEST, Level.SEVERE, "Could not read file: " + f.getName());

            plugin.printStacktrace("Could not read file: " + f.getName(), e);
            return null;
        } catch (InvalidConfigurationException e) {
            plugin.debug(DebugLevel.HIGHEST, Level.SEVERE, "Detected invalid configuration in file: " + f.getName());

            plugin.printStacktrace("Detected invalid configuration in file: " + f.getName(), e);
            return null;
        }
    }

    public boolean loadGUIMenu(String menu) {
        if (checkConfig(null, "config.yml", false) == null) {
            return false;
        }

        FileConfiguration c = plugin.getConfig();

        if (!c.contains("gui_menus")) {
            return false;
        }

        if (!c.isConfigurationSection("gui_menus")) {
            return false;
        }

        Set<String> keys = c.getConfigurationSection("gui_menus").getKeys(false);

        if (keys.isEmpty()) {
            return false;
        }

        if (!keys.contains(menu)) {
            return false;
        }

        if (c.contains("gui_menus." + menu + ".file")) {
            loadMenuFromFile(menu);
        } else {
            loadMenu(c, menu, true, "config");
        }

        return true;
    }

    public int loadGUIMenus() {

        if (checkConfig(null, "config.yml", false) == null) {
            return 0;
        }

        FileConfiguration c = plugin.getConfig();

        if (!c.contains("gui_menus")) {
            return 0;
        }

        if (!c.isConfigurationSection("gui_menus")) {
            return 0;
        }

        Set<String> keys = c.getConfigurationSection("gui_menus").getKeys(false);

        if (keys.isEmpty()) {
            return 0;
        }

        for (String key : keys) {

            if (c.contains("gui_menus." + key + ".file")) {

                loadMenuFromFile(key);

            } else {
                loadMenu(c, key, true, "config");
            }
        }
        return Menu.getLoadedMenuSize();
    }

    public boolean loadMenuFromFile(String menuName) {

        String fileName = plugin.getConfig().getString("gui_menus." + menuName + ".file");

        if (!fileName.endsWith(".yml")) {
            plugin.debug(DebugLevel.HIGHEST, Level.SEVERE, "Filename specified for menu: " + menuName + " is not a .yml file!", "Make sure that the file name to load this menu from is specified as a .yml file!", "Skipping loading of menu: " + menuName);
            return false;
        }

        File f = new File(menuDirectory.getPath(), fileName);

        if (!f.exists()) {
            plugin.debug(DebugLevel.HIGHEST, Level.INFO, f.getName() + " does not exist!");

            try {
                File folder = f.getParentFile();
                if (!folder.exists()) folder.mkdirs();

                f.createNewFile();
                if (!saveResourceToFile("default_menu.yml", f)) {
                    plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Failed to create a default menu file for menu: " + menuName, "Skipping loading menu: " + menuName);
                    return false;
                }
                plugin.debug(DebugLevel.HIGHEST, Level.INFO, f.getName() + " created! Add your menu options to this file and use /dm reload to load it!");
            } catch (IOException e) {
                plugin.debug(DebugLevel.HIGHEST, Level.SEVERE, "Could not create menu file: plugins" + separator + "DeluxeMenus" + separator + "gui_menus" + separator + fileName);
                return false;
            }
        }

        FileConfiguration cfg = checkConfig(f);

        if (cfg == null) {
            plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Menu: " + menuName + " in file: " + fileName + " not loaded.");
            return false;
        }

        if (cfg.getKeys(false).isEmpty()) {
            plugin.debug(DebugLevel.HIGH, Level.INFO, "Menu config: " + f.getName() + " is empty! Creating default config example...");
            saveResourceToFile("default_menu.yml", f);
            return false;
        }

        final Path guiMenusPath = menuDirectory.toPath();
        final Path menuPath = f.toPath();
        final Path relativePath = guiMenusPath.relativize(menuPath);

        loadMenu(cfg, menuName, false, relativePath.toString());
        return Menu.getMenuByName(menuName).isPresent();
    }

    public void loadMenu(FileConfiguration c, String key, boolean mainConfig, final @NotNull String path) {
        if (mainConfig) {
            plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Menu: " + key + " does not have a file specified in config.yml! Creating menus in the " + "config.yml file is deprecated and will be removed in a future version! Please migrate your " + "menus to individual files in the gui_menus directory! For more information see: " + "https://wiki.helpch.at/clips-plugins/deluxemenus/external-menus");
        }

        String pre = "gui_menus." + key + ".";

        if (!mainConfig) {
            pre = "";
        }

        if (!c.contains(pre + "menu_title")) {
            plugin.debug(DebugLevel.HIGHEST, Level.SEVERE, "Menu title for menu: " + key + " is not present!", "Skipping menu: " + key);
            return;
        }

        String title = null;

        if (c.isString(pre + "menu_title")) {
            title = c.getString(pre + "menu_title");
        } else if (c.isList(pre + "menu_title")) {
            title = c.getStringList(pre + "menu_title").get(0);
        }

        if (title == null || title.isEmpty()) {
            plugin.debug(DebugLevel.HIGHEST, Level.SEVERE, "Menu title for menu: " + key + " is invalid!", "Skipping menu: " + key);
            return;
        }

        final MenuOptions.MenuOptionsBuilder builder = MenuOptions.builder(key, title);

        InventoryType type = InventoryType.CHEST;

        if (c.contains(pre + "inventory_type")) {
            try {
                final InventoryType inventoryType = InventoryType.valueOf(c.getString(pre + "inventory_type").toUpperCase());
                type = !VALID_INVENTORY_TYPES.contains(inventoryType) ? InventoryType.CHEST : inventoryType;
            } catch (Exception ex) {
                plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Inventory type for menu: " + key + " is invalid!", "Valid Inventory types: " + Arrays.toString(VALID_INVENTORY_TYPES.toArray()), "Defaulting to CHEST inventory type.");
            }
        }

        builder.type(type);

        final List<String> openCommands = new ArrayList<>();

        if (c.contains(pre + "open_command")) {
            if (c.isString(pre + "open_command") && !c.getString(pre + "open_command").isEmpty()) {

                String cmd = c.getString(pre + "open_command");

                if (cmd == null) {
                    plugin.debug(DebugLevel.HIGHEST, Level.SEVERE, "open_command specified for menu: " + key + " is null!", "Skipping menu: " + key);
                    return;
                }

                if (Menu.isMenuCommand(cmd)) {
                    plugin.debug(DebugLevel.HIGHEST, Level.SEVERE, "open_command specified for menu: " + key + " already exists for another menu!", "Skipping menu: " + key);
                    return;
                }

                openCommands.add(cmd.toLowerCase());

            } else if (c.isList(pre + "open_command") && !c.getStringList(pre + "open_command").isEmpty()) {

                List<String> cmds = c.getStringList(pre + "open_command");

                for (String cmd : cmds) {
                    if (Menu.isMenuCommand(cmd)) {
                        plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "command: " + cmd + " specified for menu: " + key + " already exists for another menu!", "Skipping command: " + cmd + " in menu: " + key);
                    } else {
                        openCommands.add(cmd.toLowerCase());
                    }
                }
            }
        }

        if (!openCommands.isEmpty()) {
            builder.commands(openCommands);
            builder.registerCommands(c.getBoolean(pre + "register_command", false));
        }

        List<String> argumentNames = new ArrayList<>();
        List<RequirementList> argumentRequirements = new ArrayList<>();

        if (c.contains(pre + "args")) {
            // New requirements parsing
            if (c.isConfigurationSection(pre + "args")) {
                Set<String> mapList = c.getConfigurationSection(pre + "args").getKeys(false);
                debug("found args");
                for (String arg : mapList) {
                    debug("arg: " + arg);
                    // If it has requirements, add them
                    if (c.contains(pre + "args." + arg + ".requirements")) {
                        debug("arg has requirements: " + arg);
                        argumentRequirements.add(this.getRequirements(c, pre + "args." + arg));
                    }
                    // Always add the arg itself
                    argumentNames.add(arg);
                }
                // Old list parsing
            } else if (c.isList(pre + "args")) {
                argumentNames.addAll(c.getStringList(pre + "args"));
                // Old singular item parsing
            } else if (c.isString(pre + "args")) {
                argumentNames.add(c.getString(pre + "args"));
            }
        }

        builder.arguments(argumentNames);
        builder.argumentRequirements(argumentRequirements);
        builder.argumentsUsageMessage(c.getString(pre + "args_usage_message", null));

        int size = 54;
        if (type == InventoryType.CHEST) {
            if (!c.contains(pre + "size")) {
                plugin.debug(DebugLevel.HIGHEST, Level.INFO, "Menu size for menu: " + key + " is not present!", "Using default size of 54");
            } else {
                size = c.getInt(pre + "size");

                if ((size + 1) % 9 == 0) size++;

                if ((size - 1) % 9 == 0) size--;

                if (size < 9) {
                    plugin.debug(DebugLevel.HIGHEST, Level.INFO, "Menu size for menu: " + key + " is lower than 9", "Defaulting to 9.");
                    size = 9;
                }

                if (size > 54) {
                    plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Menu size for menu: " + key + " is higher than 54", "Defaulting to 54.");
                    size = 54;
                }

                if (size % 9 != 0) {
                    plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Menu size for menu: " + key + " is not a multiple of 9", "Defaulting to 54.");
                    size = 54;
                }
            }
        } else {
            size = type.getDefaultSize();
            plugin.debug(DebugLevel.LOWEST, Level.INFO, "TYPE IS: " + type + ". Setting size to:" + type.getDefaultSize());
        }

        builder.size(size);

        if (c.contains(pre + "open_requirement")) {
            final RequirementList requirements = getRequirements(c, pre + "open_requirement");
            if (requirements != null && requirements.getRequirements() != null) {
                builder.openRequirements(requirements);
            }
        }

        if (c.contains(pre + "open_commands")) {
            builder.openHandler(getClickHandler(c, pre + "open_commands"));
        }

        if (c.contains(pre + "close_commands")) {
            builder.closeHandler(getClickHandler(c, pre + "close_commands"));
        }

        final int updateInterval = c.getInt(pre + "update_interval", 10);
        builder.updateInterval(updateInterval > 0 ? updateInterval : 10);

        Map<Integer, TreeMap<Integer, MenuItem>> items = loadMenuItems(c, key, mainConfig);

        if (items == null || items.isEmpty()) {
            plugin.debug(DebugLevel.HIGHEST, Level.SEVERE, "Failed to load menu items for menu: " + key, "Skipping menu: " + key);
            return;
        }

        builder.parsePlaceholdersInArguments(c.getBoolean(pre + "arguments_support_placeholders", false));
        builder.parsePlaceholdersAfterArguments(c.getBoolean(pre + "parse_placeholders_after_arguments", false));

        // Don't need to register the menu since it's done in the constructor
        new Menu(plugin, builder.build(), items, path);
    }

    private Map<Integer, TreeMap<Integer, MenuItem>> loadMenuItems(FileConfiguration c, String name, boolean mainConfig) {
        String itemsPath = "gui_menus." + name + ".items";

        if (!mainConfig) {
            itemsPath = "items";
        }

        if (!c.contains(itemsPath) || !c.isConfigurationSection(itemsPath)) {
            return null;
        }

        Set<String> itemKeys = c.getConfigurationSection(itemsPath).getKeys(false);

        if (itemKeys.isEmpty()) {
            return null;
        }

        Map<Integer, TreeMap<Integer, MenuItem>> menuItems = new HashMap<>();

        for (String key : itemKeys) {

            String currentPath = itemsPath + "." + key + ".";

            if (!c.contains(currentPath + "material")) {
                plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Material for item: " + key + " in menu: " + name + " is not present!", "Skipping item: " + key);
                continue;
            }

            final String material = c.getString(currentPath + "material");
            final String lowercaseMaterial = material.toLowerCase(Locale.ROOT);
            if (!isValidMaterial(lowercaseMaterial)) {
                plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Material for item: " + key + " in menu: " + name + " is not valid!", "Skipping item: " + key);
                continue;
            }

            checkForDeprecatedItemOptions(c.getConfigurationSection(currentPath), name);

            MenuItemOptions.MenuItemOptionsBuilder builder = MenuItemOptions.builder()
                    .material(material)
                    .baseColor(Optional.ofNullable(c.getString(currentPath + "base_color"))
                            .map(String::toUpperCase)
                            .map(DyeColor::valueOf)
                            .orElse(null))
                    .slot(c.getInt(currentPath + "slot", 0))
                    .amount(c.getInt(currentPath + "amount", -1))
                    .dynamicAmount(c.getString(currentPath + "dynamic_amount", null))
                    .customModelData(c.getString(currentPath + "model_data", null))
                    .lightLevel(c.getString(currentPath + "light_level", null))
                    .displayName(c.getString(currentPath + "display_name"))
                    .lore(c.getStringList(currentPath + "lore"))
                    .hasLore(c.contains(currentPath + "lore"))
                    .rgb(c.getString(currentPath + "rgb", null))
                    .unbreakable(c.getBoolean(currentPath + "unbreakable", false))
                    .updatePlaceholders(c.getBoolean(currentPath + "update", false))
                    .hideAttributes(c.getBoolean(currentPath + "hide_attributes", false))
                    .hideUnbreakable(c.getBoolean(currentPath + "hide_unbreakable", false))
                    .hideEnchants(c.getBoolean(currentPath + "hide_enchantments", false))
                    .nbtString(c.getString(currentPath + "nbt_string", null))
                    .nbtByte(c.getString(currentPath + "nbt_byte", null))
                    .nbtShort(c.getString(currentPath + "nbt_short", null))
                    .nbtInt(c.getString(currentPath + "nbt_int", null))
                    .nbtStrings(c.getStringList(currentPath + "nbt_strings"))
                    .nbtBytes(c.getStringList(currentPath + "nbt_bytes"))
                    .nbtShorts(c.getStringList(currentPath + "nbt_shorts"))
                    .nbtInts(c.getStringList(currentPath + "nbt_ints"))
                    .priority(c.getInt(currentPath + "priority", 1))
                    .hideTooltip(c.getString(currentPath + "hide_tooltip", null))
                    .enchantmentGlintOverride(c.getString(currentPath + "enchantment_glint_override", null))
                    .rarity(c.getString(currentPath + "rarity", null))
                    .tooltipStyle(c.getString(currentPath + "tooltip_style", null))
                    .itemModel(c.getString(currentPath + "item_model", null));

            // Lore Append Mode
            if (c.contains(currentPath + "lore_append_mode")) {
                String loreAppendMode = c.getString(currentPath + "lore_append_mode", "OVERRIDE").toUpperCase();
                try {
                    builder.loreAppendMode(LoreAppendMode.valueOf(loreAppendMode));
                } catch (IllegalArgumentException | NullPointerException ignored) {
                    builder.loreAppendMode(LoreAppendMode.OVERRIDE); // Defaults to override in case of invalid append mode
                    plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Lore append mode: " + loreAppendMode + " for item: " + key + " in menu: " + name + " is not a valid lore append mode!");
                }
            }

            // item flags
            if (c.contains(currentPath + "item_flags")) {
                List<ItemFlag> itemFlags = new ArrayList<>();

                for (String flagAsString : getStringListFromConfig(c, currentPath + "item_flags")) {
                    ItemFlag flag = Enums.getIfPresent(ItemFlag.class, flagAsString.toUpperCase()).orNull();

                    if (flag == null) {
                        plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Item flag: " + flagAsString + " for item: " + key + " in menu: " + name + " is not a valid item flag!");
                        continue;
                    }

                    itemFlags.add(flag);
                }

                builder.itemFlags(itemFlags);
            }

            addDamageOptionToBuilder(c, currentPath, key, name, builder);

            if (VersionHelper.HAS_ARMOR_TRIMS) {
                builder.trimMaterial(c.getString(currentPath + "trim_material", null));
                builder.trimPattern(c.getString(currentPath + "trim_pattern", null));
            }

            if (c.contains(currentPath + "banner_meta") && c.isList(currentPath + "banner_meta")) {

                List<org.bukkit.block.banner.Pattern> bannerMeta = new ArrayList<>();

                for (String e : c.getStringList(currentPath + "banner_meta")) {
                    if (!e.contains(";")) {
                        plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Banner Meta for item: " + key + ", meta entry: " + e + " is invalid! Skipping this entry!");
                        continue;
                    }

                    String[] metaParts = e.split(";", 2);

                    if (metaParts.length != 2) {
                        plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Banner Meta for item: " + key + ", meta entry: " + e + " is invalid! Skipping this entry!");
                        continue;
                    }

                    final DyeColor color;
                    final PatternType type;

                    try {
                        color = DyeColor.valueOf(metaParts[0].toUpperCase());
                        type = PatternType.valueOf(metaParts[1].toUpperCase());
                    } catch (IllegalArgumentException exception) {
                        plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Banner Meta for item: " + key + ", meta entry: " + e + " is invalid! Skipping this entry!");

                        plugin.printStacktrace("Banner Meta for item: " + key + ", meta entry: " + e + " is invalid! Skipping this entry!", exception);
                        continue;
                    }

                    bannerMeta.add(new org.bukkit.block.banner.Pattern(color, type));
                }

                if (!bannerMeta.isEmpty()) {
                    builder.bannerMeta(bannerMeta);
                }
            }

            if (c.contains(currentPath + "potion_effects") && c.isList(currentPath + "potion_effects")) {
                List<PotionEffect> potionEffects = new ArrayList<>();
                for (String e : c.getStringList(currentPath + "potion_effects")) {
                    try {

                        if (!e.contains(";")) {
                            plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Potion Meta for item: " + key + ", meta entry: " + e + " is invalid! Skipping this entry!");
                            continue;
                        }

                        String[] metaParts = e.split(";", 3);

                        if (metaParts.length != 3) {
                            plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Potion Meta for item: " + key + ", meta entry: " + e + " is invalid! Skipping this entry!");
                            continue;
                        }

                        PotionEffectType type = PotionEffectType.getByName(metaParts[0]);
                        int duration = Integer.parseInt(metaParts[1]);
                        int amplifier = Integer.parseInt(metaParts[2]);

                        if (type == null) {
                            plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Potion Meta for item: " + key + ", meta entry: " + e + " is invalid! Skipping this entry!");
                            continue;
                        }

                        potionEffects.add(type.createEffect(duration, amplifier));

                    } catch (Exception ex) {
                        plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Potion Meta for item: " + key + ", meta entry: " + e + " is invalid! Skipping this entry!");
                    }
                }
                if (!potionEffects.isEmpty()) {
                    builder.potionEffects(potionEffects);
                }
            }

            addEnchantmentsOptionToBuilder(c, currentPath, key, name, builder);

            if (c.contains(currentPath + "view_requirement")) {
                builder.viewRequirements(this.getRequirements(c, currentPath + "view_requirement"));
            }

            if (c.contains(currentPath + "click_commands")) {
                builder.clickHandler(getClickHandler(c, currentPath + "click_commands"));
                if (c.contains(currentPath + "click_requirement")) {
                    builder.clickRequirements(this.getRequirements(c, currentPath + "click_requirement"));
                }
            }

            if (c.contains(currentPath + "left_click_commands")) {
                builder.leftClickHandler(getClickHandler(c, currentPath + "left_click_commands"));
                if (c.contains(currentPath + "left_click_requirement")) {
                    builder.leftClickRequirements(this.getRequirements(c, currentPath + "left_click_requirement"));
                }
            }

            if (c.contains(currentPath + "right_click_commands")) {
                builder.rightClickHandler(getClickHandler(c, currentPath + "right_click_commands"));
                if (c.contains(currentPath + "right_click_requirement")) {
                    builder.rightClickRequirements(this.getRequirements(c, currentPath + "right_click_requirement"));
                }
            }

            if (c.contains(currentPath + "shift_left_click_commands")) {
                builder.shiftLeftClickHandler(getClickHandler(c, currentPath + "shift_left_click_commands"));
                if (c.contains(currentPath + "shift_left_click_requirement")) {
                    builder.shiftLeftClickRequirements(this.getRequirements(c, currentPath + "shift_left_click_requirement"));
                }
            }

            if (c.contains(currentPath + "shift_right_click_commands")) {
                builder.shiftRightClickHandler(getClickHandler(c, currentPath + "shift_right_click_commands"));
                if (c.contains(currentPath + "shift_right_click_requirement")) {
                    builder.shiftRightClickRequirements(this.getRequirements(c, currentPath + "shift_right_click_requirement"));
                }
            }

            if (c.contains(currentPath + "middle_click_commands")) {
                builder.middleClickHandler(getClickHandler(c, currentPath + "middle_click_commands"));
                if (c.contains(currentPath + "middle_click_requirement")) {
                    builder.middleClickRequirements(this.getRequirements(c, currentPath + "middle_click_requirement"));
                }
            }

            List<Integer> slots = new ArrayList<>();

            if (c.contains(currentPath + "slots") && c.isList(currentPath + "slots")) {
                List<String> confSlots = c.getStringList(currentPath + "slots");
                for (String slot : confSlots) {
                    String[] values = slot.split("-", 2);
                    if (values.length == 2) {
                        for (int i = Integer.parseInt(values[0]); i <= Integer.parseInt(values[1]); i++) {
                            slots.add(i);
                        }
                    } else {
                        slots.add(Integer.parseInt(slot));
                    }
                }
            } else {
                slots.add(c.getInt(currentPath + "slot", 0));
            }

            final MenuItem menuItem = new MenuItem(plugin, builder.build());

            for (int slot : slots) {
                TreeMap<Integer, MenuItem> slotPriorityMap;
                if ((!menuItems.containsKey(slot)) || menuItems.get(slot) == null) {
                    slotPriorityMap = new TreeMap<>();
                    menuItems.put(slot, slotPriorityMap);
                } else {
                    slotPriorityMap = menuItems.get(slot);
                }
                slotPriorityMap.put(menuItem.options().priority(), new MenuItem(plugin, menuItem.options().asBuilder().slot(slot).build()));
            }
        }
        return menuItems;
    }

    private RequirementList getRequirements(FileConfiguration c, String path) {

        debug("requirement path: " + path);

        List<Requirement> requirements = new ArrayList<>();

        if (!c.contains(path + ".requirements")) {
            debug("requirements list was not found");
            return null;
        }

        debug("found requirements list");

        for (String key : c.getConfigurationSection(path + ".requirements").getKeys(false)) {
            debug("requirement: " + key + " from requirements list");
            String rPath = path + ".requirements." + key;
            if (!c.contains(rPath + ".type")) {
                plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "No type set for requirement: " + key + " for path: " + rPath);
                continue;
            }

            String stringType = c.getString(rPath + ".type");
            RequirementType type = RequirementType.getType(stringType);

            if (type == null) {
                plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Requirement type '" + stringType + "' at path '" + rPath + "' is not valid!");
                continue;
            }

            debug("Requirement type: " + type.name());

            Requirement req = null;

            boolean invert;
            switch (type) {
                case HAS_ITEM:
                case DOES_NOT_HAVE_ITEM:
                    ItemWrapper wrapper = new ItemWrapper();
                    if (c.contains(rPath + ".material")) {
                        String materialName = c.getString(rPath + ".material");
                        try {
                            if (!containsPlaceholders(materialName) && plugin.getItemHooks().values()
                                    .stream()
                                    .filter(x -> materialName.toLowerCase().startsWith(x.getPrefix()))
                                    .findFirst()
                                    .orElse(null) == null)
                                Material.valueOf(materialName.toUpperCase());
                        } catch (Exception ex) {
                            plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "has item requirement at path: " + rPath + " does not specify a valid Material name!");
                            break;
                        }
                        wrapper.setMaterial(materialName);
                    } else {
                        plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "has item requirement at path: " + rPath + " does not contain a material: entry!");
                        break;
                    }
                    wrapper.setAmount(c.getInt(rPath + ".amount", 1));
                    wrapper.hasData(c.contains(rPath + ".data") && c.isInt(rPath + ".data"));
                    wrapper.setData((short) c.getInt(rPath + ".data", 0));

                    if (c.isString(rPath + ".name")) {
                        wrapper.setName(c.getString(rPath + ".name"));
                    }
                    if (c.isString(rPath + ".lore")) {
                        wrapper.setLore(c.getString(rPath + ".lore"));
                    }
                    if (c.isList(rPath + ".lore")) {
                        wrapper.setLoreList(c.getStringList(rPath + ".lore"));
                    }

                    wrapper.setStrict(c.getBoolean(rPath + ".strict", false));

                    wrapper.setArmor(c.getBoolean(rPath + ".armor", false));
                    wrapper.setOffhand(c.getBoolean(rPath + ".offhand", false));

                    // TODO: Remove support for the old options in v1.14.0
                    if (c.contains(rPath + ".model_data") && c.isInt(rPath + ".model_data")) {
                        wrapper.setCustomData(c.getInt(rPath + ".model_data"));
                    } else {
                        wrapper.setCustomData(c.getInt(rPath + ".modeldata", 0));
                    }

                    if (c.contains(rPath + ".name_contains")) {
                        wrapper.setNameContains(c.getBoolean(rPath + ".name_contains"));
                    } else {
                        wrapper.setNameContains(c.getBoolean(rPath + ".name-contains", false));
                    }

                    if (c.contains(rPath + ".name_ignorecase")) {
                        wrapper.setNameContains(c.getBoolean(rPath + ".name_ignorecase"));
                    } else {
                        wrapper.setNameContains(c.getBoolean(rPath + ".name-ignorecase", false));
                    }

                    if (c.contains(rPath + ".lore_contains")) {
                        wrapper.setLoreContains(c.getBoolean(rPath + ".lore_contains"));
                    } else {
                        wrapper.setLoreContains(c.getBoolean(rPath + ".lore-contains", false));
                    }

                    if (c.contains(rPath + ".lore_ignorecase")) {
                        wrapper.setLoreContains(c.getBoolean(rPath + ".lore_ignorecase"));
                    } else {
                        wrapper.setLoreContains(c.getBoolean(rPath + ".lore-ignorecase", false));
                    }

                    invert = type == RequirementType.DOES_NOT_HAVE_ITEM;
                    req = new HasItemRequirement(plugin, wrapper, invert);
                    break;
                case HAS_PERMISSION:
                case DOES_NOT_HAVE_PERMISSION:
                    if (c.contains(rPath + ".permission")) {
                        invert = type == RequirementType.DOES_NOT_HAVE_PERMISSION;
                        req = new HasPermissionRequirement(c.getString(rPath + ".permission"), invert);
                    } else {
                        plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Has Permission requirement at path: " + rPath + " does not contain a permission: entry");
                    }
                    break;
                case HAS_PERMISSIONS:
                case DOES_NOT_HAVE_PERMISSIONS:
                    if (c.contains(rPath + ".permissions")) {
                        invert = type == RequirementType.DOES_NOT_HAVE_PERMISSIONS;
                        int minimum = -1;
                        if (c.contains(rPath + ".minimum") && (minimum = c.getInt(rPath + ".minimum")) < 1) {
                            plugin.debug(
                                    DebugLevel.HIGHEST,
                                    Level.WARNING,
                                    "Has Permissions requirement at path: " + rPath + " has a minimum lower than 1. All permissions will be checked"
                            );
                            minimum = -1;
                        }
                        List<String> permissions = c.getStringList(rPath + ".permissions");
                        if (permissions.isEmpty()) {
                            plugin.debug(
                                    DebugLevel.HIGHEST,
                                    Level.WARNING,
                                    "Has Permissions requirement at path: " + rPath + " has no permissions to check. Ignoring..."
                            );
                            break;
                        } else if (minimum > permissions.size()) {
                            plugin.debug(
                                    DebugLevel.HIGHEST,
                                    Level.WARNING,
                                    "Has Permissions requirement at path: " + rPath + " has a minimum higher than the amount of permissions. Using "+permissions.size()+" instead"
                            );
                            minimum = permissions.size();
                        }
                        req = new HasPermissionsRequirement(permissions, minimum, invert);
                    } else {
                        plugin.debug(
                                DebugLevel.HIGHEST,
                                Level.WARNING,
                                "Has Permissions requirement at path: " + rPath + " does not contain permissions: entry"
                        );
                    }
                    break;
                case JAVASCRIPT:
                    if (c.contains(rPath + ".expression")) {
                        req = new JavascriptRequirement(plugin, c.getString(rPath + ".expression"));
                    } else {
                        plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Javascript requirement at path: " + rPath + " does not contain an expression: entry");
                    }
                    break;
                case EQUAL_TO:
                case NOT_EQUAL_TO:
                case GREATER_THAN:
                case GREATER_THAN_EQUAL_TO:
                case LESS_THAN:
                case LESS_THAN_EQUAL_TO:
                case STRING_CONTAINS:
                case STRING_EQUALS:
                case STRING_EQUALS_IGNORECASE:
                case STRING_DOES_NOT_CONTAIN:
                case STRING_DOES_NOT_EQUAL:
                case STRING_DOES_NOT_EQUAL_IGNORECASE:
                    if (c.contains(rPath + ".input") && c.contains(rPath + ".output")) {
                        req = new InputResultRequirement(type, c.getString(rPath + ".input"), c.getString(rPath + ".output"));
                    } else {
                        plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Requirement at path: " + rPath + " does not contain the input: and/or the output: entries");
                    }
                    break;
                case HAS_MONEY:
                case DOES_NOT_HAVE_MONEY:
                    if (c.contains(rPath + ".amount") || c.contains(rPath + ".placeholder")) {
                        invert = type == RequirementType.DOES_NOT_HAVE_MONEY;
                        req = new HasMoneyRequirement(plugin, c.getDouble(rPath + ".amount"), invert, c.getString(rPath + ".placeholder", null));
                    } else {
                        plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Has Money requirement at path: " + rPath + " does not contain an amount: entry");
                    }
                    break;
                case HAS_EXP:
                case DOES_NOT_HAVE_EXP:
                    if (c.contains(rPath + ".amount")) {
                        if (!containsPlaceholders(c.getString(rPath + ".amount")) && !c.isInt(rPath + ".amount")) {
                            plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Value at path: " + rPath + ".amount is not a placeholder or a number");
                            break;
                        }
                        invert = type == RequirementType.DOES_NOT_HAVE_EXP;
                        req = new HasExpRequirement(plugin, c.getString(rPath + ".amount"), invert, c.getBoolean(rPath + ".level"));
                    } else {
                        plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Has Exp requirement at path: " + rPath + " does not contain an amount: entry");
                    }
                    break;
                case REGEX_MATCHES:
                case REGEX_DOES_NOT_MATCH:
                    if (c.contains(rPath + ".input") && c.contains(rPath + ".regex")) {
                        Pattern p = Pattern.compile(c.getString(rPath + ".regex"));
                        invert = type == RequirementType.REGEX_DOES_NOT_MATCH;
                        req = new RegexMatchesRequirement(p, c.getString(rPath + ".input"), invert);
                    } else {
                        plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Regex requirement at path: " + rPath + " does not contain a input: or regex: entry");
                    }
                    break;
                case IS_NEAR:
                case IS_NOT_NEAR:
                    if (c.contains(rPath + ".location") && c.contains(rPath + ".distance")) {
                        invert = type == RequirementType.IS_NOT_NEAR;
                        Location loc = LocationUtils.deserializeLocation(c.getString(rPath + ".location"));
                        if (loc == null) {
                            plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "requirement at path: " + rPath + " has an invalid location. Valid Format is: <world>,<x>,<y>,<z>");
                        }
                        req = new IsNearRequirement(loc, c.getInt(rPath + ".distance"), invert);
                    } else {
                        plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Is Near requirement at path: " + rPath + " does not contain a location: or distance: entry");
                    }
                    break;
                case HAS_META:
                case DOES_NOT_HAVE_META:
                    if (!VersionHelper.IS_PDC_VERSION) {
                        plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Has Meta requirement is not available for your server version!");
                        break;
                    }
                    if (c.contains(rPath + ".key") && c.contains(rPath + ".meta_type") && c.contains(rPath + ".value")) {
                        String metaKey = c.getString(rPath + ".key");
                        invert = type == RequirementType.DOES_NOT_HAVE_META;
                        req = new HasMetaRequirement(plugin, metaKey, c.getString(rPath + ".meta_type").toUpperCase(), c.getString(rPath + ".value"), invert);
                    } else {
                        plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Has Meta requirement at path: " + rPath + " does not contain the key:, meta_type: and/or value: entries!");
                    }
                    break;
                case STRING_LENGTH:
                    if (c.contains(rPath + ".input") && (c.contains(rPath + ".min") || c.contains(rPath + ".max"))) {
                        int min = c.getInt(rPath + ".min", 0);
                        Integer max = null;
                        if (c.contains(rPath + ".max")) {
                            max = c.getInt(rPath + ".max");
                        }
                        req = new StringLengthRequirement(c.getString(rPath + ".input"), min, max);
                    } else {
                        plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "String length requirement at path: " + rPath + " does not contain an input: or one of (min: or max:)");
                    }
                    break;
                case IS_OBJECT:
                    if (c.contains(rPath + ".input") && c.contains(rPath + ".object")) {
                        req = new IsObjectRequirement(c.getString(rPath + ".input"), c.getString(rPath + ".object"));
                    } else {
                        plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "String length requirement at path: " + rPath + " does not contain an input: or object:");
                    }
                    break;
                default:
                    break;
            }

            if (req != null) {
                ClickHandler requirementSuccessHandler = null;
                ClickHandler requirementDenyHandler = null;

                if (c.contains(rPath + ".success_commands")) {
                    debug("Requirement has success commands");
                    requirementSuccessHandler = getClickHandler(c, rPath + ".success_commands");
                }

                if (c.contains(rPath + ".deny_commands")) {
                    debug("Requirement has deny commands");
                    requirementDenyHandler = getClickHandler(c, rPath + ".deny_commands");
                }

                req.setOptional(c.getBoolean(rPath + ".optional", false));
                req.setSuccessHandler(requirementSuccessHandler);
                req.setDenyHandler(requirementDenyHandler);
                requirements.add(req);
            }
        }

        if (requirements.isEmpty()) {
            return null;
        }

        RequirementList list = new RequirementList(requirements);

        if (c.contains(path + ".deny_commands")) {
            debug("global deny handler found");
            list.setDenyHandler(getClickHandler(c, path + ".deny_commands"));
        }

        list.setStopAtSuccess(c.getBoolean(path + ".stop_at_success", false));

        if (c.contains(path + ".minimum_requirements")) {
            list.setMinimumRequirements(c.getInt(path + ".minimum_requirements"));
        } else {
            int required = 0;
            for (Requirement req : requirements) {
                if (!req.isOptional()) {
                    required = required++;
                }
            }
            list.setMinimumRequirements(required);
        }

        return list;
    }

    private ClickHandler getClickHandler(FileConfiguration c, String configPath) {

        List<String> commands = c.getStringList(configPath);

        if (commands.isEmpty()) {
            return null;
        }

        final List<ClickAction> actions = new ArrayList<>();

        for (String command : commands) {

            if (command == null || command.isEmpty()) {
                continue;
            }

            ActionType type = ActionType.getByStart(command);

            if (type == null) {
                continue;
            } else {
                command = command.replaceFirst(Pattern.quote(type.getIdentifier()), "");
            }

            if (command.startsWith(" ")) {
                command = command.trim();
            }

            ClickAction action = new ClickAction(type, command);

            Matcher d = DELAY_MATCHER.matcher(command);

            if (d.find()) {
                action.setDelay(d.group(1));
                command = command.replaceFirst(Pattern.quote(d.group()), "");
            }

            Matcher ch = CHANCE_MATCHER.matcher(command);

            if (ch.find()) {
                action.setChance(ch.group(1));
                command = command.replaceFirst(Pattern.quote(ch.group()), "");
            }

            action.setExecutable(command);
            actions.add(action);
        }

        ClickHandler handler = null;

        if (!actions.isEmpty()) {

            handler = new ClickHandler() {

                @Override
                public void onClick(@NotNull final MenuHolder holder) {

                    for (ClickAction action : actions) {

                        if (!action.checkChance(holder)) {
                            continue;
                        }

                        final ClickActionTask actionTask = new ClickActionTask(plugin, holder.getViewer().getUniqueId(), action.getType(), action.getExecutable(), holder.getTypedArgs(), holder.parsePlaceholdersInArguments(), holder.parsePlaceholdersAfterArguments());

                        if (action.hasDelay()) {
                            actionTask.runTaskLater(plugin, action.getDelay(holder));
                            continue;
                        }

                        actionTask.runTask(plugin);
                    }
                }
            };
        }

        return handler;
    }

    private void checkForDeprecatedItemOptions(ConfigurationSection config, String menuName) {
        final BiConsumer<String, ItemFlag> oldItemFlagOptionCheck = (option, itemFlag) -> {
            if (!config.contains(option)) {
                return;
            }

            plugin.debug(DebugLevel.HIGHEST, Level.WARNING, String.format("Option '%s' of item '%s' in menu '%s' is deprecated and will be removed in the future. Replace it with item_flags: [%s].", option, config.getName(), menuName, itemFlag));
        };

        oldItemFlagOptionCheck.accept("hide_attributes", ItemFlag.HIDE_ATTRIBUTES);
        oldItemFlagOptionCheck.accept("hide_enchantments", ItemFlag.HIDE_ENCHANTS);
        oldItemFlagOptionCheck.accept("hide_unbreakable", ItemFlag.HIDE_UNBREAKABLE);
    }

    public void debug(String... messages) {
        plugin.debug(DebugLevel.LOWEST, Level.INFO, messages);
    }

    public @NotNull DebugLevel debugLevel() {
        String stringLevel = plugin.getConfig().getString("debug", "HIGHEST");

        if (stringLevel.equalsIgnoreCase("true")) {
            stringLevel = "LOWEST";
            plugin.getConfig().set("debug", "LOWEST");
        } else if (stringLevel.equalsIgnoreCase("false")) {
            stringLevel = "HIGHEST";
            plugin.getConfig().set("debug", "HIGHEST");
        }

        final DebugLevel debugLevel = DebugLevel.getByName(stringLevel);
        return debugLevel == null ? DebugLevel.LOW : debugLevel;
    }

    public File getMenuDirector() {
        return menuDirectory;
    }
    public void addEnchantmentsOptionToBuilder(final FileConfiguration c, final String currentPath,
                                               final String itemKey, final String menuName,
                                               final MenuItemOptions.MenuItemOptionsBuilder builder) {
        if (!c.contains(currentPath + "enchantments")) {
            return;
        }

        final List<String> configEnchantments = c.getStringList(currentPath + "enchantments");
        final Map<Enchantment, Integer> parsedEnchantments = new HashMap<>();

        for (final String configEnchantment : configEnchantments) {
            if (configEnchantment == null || !configEnchantment.contains(";")) {
                plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Enchantment format '" + configEnchantment + "' is incorrect for item " + itemKey +
                                " in GUI " + menuName + "!",
                        "Correct format: - '<Enchantment name>;<level>"
                );
                continue;
            }

            String[] parts = configEnchantment.split(";", 2);
            if (parts.length != 2 || parts[0] == null || parts[1] == null) {
                plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Enchantment format '" + configEnchantment + "' is incorrect for item " + itemKey +
                                " in GUI " + menuName + "!",
                        "Correct format: - '<Enchantment name>;<level>"
                );
                continue;
            }

            final Enchantment enchantment = Enchantment.getByName(parts[0].strip().toUpperCase());
            if (enchantment == null) {
                plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Enchantment '" + parts[0].strip() + "' for item " + itemKey +
                                " in menu " + menuName + " is not a valid enchantment name!"
                );
            }

            Integer level = Ints.tryParse(parts[1].strip());
            if (level == null) {
                level = 1;
                plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Enchantment level '" + parts[1].strip() + "' is incorrect for item " + itemKey +
                                " in menu " + menuName + "!"
                );
            }
            parsedEnchantments.put(enchantment, level);
        }
        builder.enchantments(parsedEnchantments);
    }

    public void addDamageOptionToBuilder(final FileConfiguration c, final String currentPath, final String itemKey,
                                         final String menuName, final MenuItemOptions.MenuItemOptionsBuilder builder) {
        boolean damageOptionIsPresent = false;
        String damageValue = null;

        String key = "damage";
        if (c.contains(currentPath + key)) {
            damageOptionIsPresent = true;
            damageValue = c.getString(currentPath + key, "");
        }

        key = "data";
        if (c.contains(currentPath + key)) {
            if (!damageOptionIsPresent) {
                plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Found 'data' option for item: " + itemKey + " in menu: " + menuName + ". This option " + "is deprecated and will be removed soon. Please use 'damage' instead.");
                damageValue = c.getString(currentPath + key, "");
            } else {
                plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Found 'data' and 'damage' options for item: " + itemKey + " in menu: " + menuName + ". 'data' option is deprecated and will be ignored. Using 'damage' instead.");
            }
        }

        if (damageValue == null) {
            return;
        }

        if (damageOptionIsPresent) {
            key = "damage";
        }

        if (!ItemUtils.isPlaceholderOption(damageValue) && Ints.tryParse(damageValue) == null) {
            plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Found invalid value for '" + key + "' option for item: " + itemKey + " in menu: " + menuName + ".", "The correct formats for '" + key + "' are:", "  -> <number>", "  -> placeholder-<placeholder>", "Ignoring the invalid value.");
            return;
        }

        final String[] parts = damageValue.split("-", 2);
        if (parts.length >= 2 && !containsPlaceholders(parts[1])) {
            plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Could not find placeholder for '" + key + "' option for item: " + itemKey + " in menu: " + menuName + ".", "Ignoring the invalid value.");
            return;
        }

        builder.damage(parts.length == 1 ? parts[0] : parts[1]);
    }
}
