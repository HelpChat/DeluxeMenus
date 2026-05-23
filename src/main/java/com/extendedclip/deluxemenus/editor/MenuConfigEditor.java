package com.extendedclip.deluxemenus.editor;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.Menu;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MenuConfigEditor {

    private final DeluxeMenus plugin;

    public MenuConfigEditor(final @NotNull DeluxeMenus plugin) {
        this.plugin = plugin;
    }

    public @NotNull Optional<File> resolveFile(final @NotNull Menu menu) {
        if ("config".equalsIgnoreCase(menu.path())) {
            return Optional.empty();
        }

        final File directory = menu.options().subMenu()
                ? plugin.getConfiguration().getSubMenuDirectory()
                : plugin.getConfiguration().getMenuDirector();
        return Optional.of(new File(directory, menu.path()));
    }

    public @NotNull String readRaw(final @NotNull Menu menu) throws IOException {
        final Optional<File> optionalFile = resolveFile(menu);
        if (optionalFile.isPresent()) {
            return Files.readString(optionalFile.get().toPath(), StandardCharsets.UTF_8);
        }

        return plugin.getConfig().saveToString();
    }

    public @NotNull String describeTarget(final @NotNull Menu menu) {
        return resolveFile(menu).map(File::getPath).orElse("config.yml");
    }

    public void saveRaw(final @NotNull Menu menu, final @NotNull String raw) throws IOException {
        final Optional<File> optionalFile = resolveFile(menu);
        if (optionalFile.isPresent()) {
            Files.writeString(optionalFile.get().toPath(), raw, StandardCharsets.UTF_8);
            return;
        }

        try {
            plugin.getConfig().loadFromString(raw);
            plugin.saveConfig();
        } catch (final InvalidConfigurationException exception) {
            throw new IOException("Invalid YAML", exception);
        }
    }

    public boolean setMaterial(final @NotNull Menu menu, final int slot, final @NotNull String material) throws IOException {
        return setItemValue(menu, slot, "material", material);
    }

    public boolean setItemValue(final @NotNull Menu menu, final int slot, final @NotNull String option, final @NotNull String value) throws IOException {
        final YamlConfiguration config = load(menu);
        final String itemPath = findOrCreateItemPath(config, menu, slot);
        if (itemPath == null) {
            return false;
        }

        if (isListOption(option)) {
            config.set(itemPath + "." + option, parseList(value));
        } else if (isIntegerOption(option)) {
            config.set(itemPath + "." + option, parseInteger(value));
        } else if (isBooleanOption(option)) {
            config.set(itemPath + "." + option, Boolean.parseBoolean(value));
        } else if (value.isBlank() && isOptionalOption(option)) {
            config.set(itemPath + "." + option, null);
        } else {
            config.set(itemPath + "." + option, value);
        }

        save(menu, config);
        return true;
    }

    public boolean deleteItem(final @NotNull Menu menu, final int slot) throws IOException {
        final YamlConfiguration config = load(menu);
        final String itemPath = findItemPath(config, menu, slot);
        if (itemPath == null) {
            return false;
        }

        config.set(itemPath, null);
        save(menu, config);
        return true;
    }

    public boolean setMenuValue(final @NotNull Menu menu, final @NotNull String option, final @NotNull String value) throws IOException {
        final YamlConfiguration config = load(menu);
        final String root = getRoot(menu);

        if ("size".equals(option)) {
            config.set(root + option, parseInteger(value));
        } else if (value.isBlank()) {
            config.set(root + option, null);
        } else {
            config.set(root + option, value);
        }

        save(menu, config);
        return true;
    }

    public @NotNull Optional<String> getMenuString(final @NotNull Menu menu, final @NotNull String option) {
        final YamlConfiguration config = load(menu);
        final String root = getRoot(menu);
        final Object value = config.get(root + option);
        return Optional.ofNullable(value).map(String::valueOf);
    }

    public @NotNull Optional<String> getItemString(final @NotNull Menu menu, final int slot, final @NotNull String option) {
        final YamlConfiguration config = load(menu);
        final String itemPath = findItemPath(config, menu, slot);
        if (itemPath == null) {
            return Optional.empty();
        }

        if (config.isList(itemPath + "." + option)) {
            return Optional.of(String.join("\n", config.getStringList(itemPath + "." + option)));
        }

        return Optional.ofNullable(config.get(itemPath + "." + option)).map(String::valueOf);
    }

    public void reload(final @NotNull Menu menu) {
        final String menuName = menu.options().name();
        final boolean subMenu = menu.options().subMenu();
        final boolean mainConfigMenu = "config".equalsIgnoreCase(menu.path());

        Menu.unload(plugin, menuName);
        plugin.reloadConfig();
        plugin.reload();
        if (subMenu) {
            if (mainConfigMenu) {
                plugin.getConfiguration().loadSubMenus();
                return;
            }

            plugin.getConfiguration().loadSubMenuFromFile(menuName);
            return;
        }

        plugin.getConfiguration().loadGUIMenu(menuName);
    }

    private @NotNull YamlConfiguration load(final @NotNull Menu menu) {
        final Optional<File> optionalFile = resolveFile(menu);
        return optionalFile
                .map(YamlConfiguration::loadConfiguration)
                .orElseGet(() -> {
                    final YamlConfiguration yaml = new YamlConfiguration();
                    for (final String key : plugin.getConfig().getKeys(false)) {
                        yaml.set(key, plugin.getConfig().get(key));
                    }
                    return yaml;
                });
    }

    private void save(final @NotNull Menu menu, final @NotNull YamlConfiguration config) throws IOException {
        final Optional<File> optionalFile = resolveFile(menu);
        if (optionalFile.isPresent()) {
            final File file = optionalFile.get();
            final File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            config.save(file);
            return;
        }

        for (final String configKey : config.getKeys(false)) {
            plugin.getConfig().set(configKey, config.get(configKey));
        }
        plugin.saveConfig();
    }

    private @Nullable String findOrCreateItemPath(final @NotNull YamlConfiguration config, final @NotNull Menu menu, final int slot) {
        final String existingPath = findItemPath(config, menu, slot);
        if (existingPath != null) {
            return existingPath;
        }

        final String root = getRoot(menu);
        final String itemKey = "slot_" + slot;
        final String itemPath = root + "items." + itemKey;
        config.set(itemPath + ".slot", slot);
        config.set(itemPath + ".material", "STONE");
        return itemPath;
    }

    private @Nullable String findItemPath(final @NotNull YamlConfiguration config, final @NotNull Menu menu, final int slot) {
        final String root = getRoot(menu);
        final ConfigurationSection items = config.getConfigurationSection(root + "items");
        if (items == null) {
            return null;
        }

        for (final String key : items.getKeys(false)) {
            final String itemPath = root + "items." + key;
            if (!matchesSlot(config, itemPath, slot)) {
                continue;
            }

            return itemPath;
        }

        return null;
    }

    private @NotNull String getRoot(final @NotNull Menu menu) {
        if (resolveFile(menu).isPresent()) {
            return "";
        }

        final String section = menu.options().subMenu() ? "sub_menus" : "gui_menus";
        return section + "." + menu.options().name() + ".";
    }

    private boolean isListOption(final @NotNull String option) {
        return option.endsWith("_commands") || "lore".equals(option) || "item_flags".equals(option);
    }

    private boolean isIntegerOption(final @NotNull String option) {
        return "amount".equals(option) || "priority".equals(option) || "slot".equals(option);
    }

    private boolean isBooleanOption(final @NotNull String option) {
        return "update".equals(option);
    }

    private boolean isOptionalOption(final @NotNull String option) {
        return !"material".equals(option);
    }

    private int parseInteger(final @NotNull String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (final NumberFormatException exception) {
            return 0;
        }
    }

    private @NotNull List<String> parseList(final @NotNull String value) {
        if (value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split("\\R|\\s\\|\\s|\\|"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());
    }

    private boolean matchesSlot(final @NotNull YamlConfiguration config, final @NotNull String itemPath, final int slot) {
        if (config.getInt(itemPath + ".slot", Integer.MIN_VALUE) == slot) {
            return true;
        }

        final List<?> configuredSlots = config.getList(itemPath + ".slots");
        if (configuredSlots == null) {
            return false;
        }

        for (final Object configuredSlot : configuredSlots) {
            if (configuredSlot != null && configuredSlotMatches(String.valueOf(configuredSlot), slot)) {
                return true;
            }
        }

        return false;
    }

    private boolean configuredSlotMatches(final @NotNull String configuredSlot, final int slot) {
        final String[] range = configuredSlot.split("-", 2);
        try {
            if (range.length == 2) {
                return slot >= Integer.parseInt(range[0].trim()) && slot <= Integer.parseInt(range[1].trim());
            }

            return slot == Integer.parseInt(configuredSlot.trim());
        } catch (final NumberFormatException exception) {
            return false;
        }
    }
}
