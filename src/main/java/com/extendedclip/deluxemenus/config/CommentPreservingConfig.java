package com.extendedclip.deluxemenus.config;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.utils.DebugLevel;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.simpleyaml.configuration.file.YamlFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Handles YAML configuration operations with full comment preservation.
 * This class uses Simple-YAML library to ensure comments keep their exact formatting.
 */
public class CommentPreservingConfig {
    private final DeluxeMenus plugin;

    public CommentPreservingConfig(@NotNull final DeluxeMenus plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads a YAML file with comment preservation
     *
     * @param file The file to load
     * @return The loaded YamlFile, or null if an error occurred
     */
    public YamlFile load(@NotNull final File file) {
        YamlFile yamlFile = new YamlFile(file);
        try {
            yamlFile.loadWithComments();
            return yamlFile;
        } catch (IOException e) {
            plugin.debug(DebugLevel.HIGHEST, Level.SEVERE, "Could not read file: " + file.getName());
            plugin.printStacktrace("Could not read file: " + file.getName(), e);
            return null;
        } catch (InvalidConfigurationException e) {
            plugin.debug(DebugLevel.HIGHEST, Level.SEVERE, "Detected invalid configuration in file: " + file.getName());
            plugin.printStacktrace("Detected invalid configuration in file: " + file.getName(), e);
            return null;
        }
    }

    /**
     * Saves a YamlFile with preserved comments
     *
     * @param yamlFile The YamlFile to save
     * @return true if the file was saved successfully, false otherwise
     */
    public boolean save(@NotNull final YamlFile yamlFile) {
        try {
            yamlFile.saveWithComments();
            return true;
        } catch (IOException e) {
            plugin.debug(DebugLevel.HIGHEST, Level.SEVERE, "Could not save file: " + yamlFile.getFilePath());
            plugin.printStacktrace("Could not save file: " + yamlFile.getFilePath(), e);
            return false;
        }
    }
    
    /**
     * Copies values from a ConfigurationSection to a YamlFile
     *
     * @param section The source ConfigurationSection
     * @param yamlFile The target YamlFile
     * @param parentPath The parent path for nested sections
     */
    public void copyValues(@NotNull final ConfigurationSection section, @NotNull final YamlFile yamlFile, @NotNull final String parentPath) {
        for (String key : section.getKeys(false)) {
            String path = parentPath.isEmpty() ? key : parentPath + "." + key;
            
            if (section.isConfigurationSection(key)) {
                copyValues(section.getConfigurationSection(key), yamlFile, path);
            } else {
                yamlFile.set(path, section.get(key));
            }
        }
    }
}
