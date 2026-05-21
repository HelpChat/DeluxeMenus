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
        } else {
            config.set(itemPath + "." + option, value);
        }

        save(menu, config);
        return true;
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

        return Optional.ofNullable(config.getString(itemPath + "." + option));
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
            config.save(optionalFile.get());
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
            if (config.getInt(itemPath + ".slot", 0) != slot) {
                continue;
            }

            return itemPath;
        }

        return null;
    }

    private @NotNull String getRoot(final @NotNull Menu menu) {
        return resolveFile(menu).isPresent() ? "" : "gui_menus." + menu.options().name() + ".";
    }

    private boolean isListOption(final @NotNull String option) {
        return option.endsWith("_commands") || "lore".equals(option);
    }

    private @NotNull List<String> parseList(final @NotNull String value) {
        if (value.isBlank()) {
            return List.of();
        }
        return Arrays.asList(value.split("\\R"));
    }
}
