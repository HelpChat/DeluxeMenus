package com.extendedclip.deluxemenus.commandpanels;

import com.extendedclip.deluxemenus.DeluxeMenus;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CommandPanelsConverter {

    private static final String DEFAULT_OUTPUT_FOLDER = "commandpanels";

    private final DeluxeMenus plugin;

    public CommandPanelsConverter(final @NotNull DeluxeMenus plugin) {
        this.plugin = plugin;
    }

    public @NotNull CommandPanelsConversionResult convertDefault(final @Nullable String outputFolder) throws IOException {
        return convert(resolveDefaultSource(), outputFolder);
    }

    public @NotNull CommandPanelsConversionResult convert(final @NotNull File source, final @Nullable String outputFolder) throws IOException {
        final File panelsFolder = resolvePanelsFolder(source);
        final File[] files = panelsFolder.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".yml"));
        if (files == null || files.length == 0) {
            throw new IOException("No .yml panel files were found in " + panelsFolder.getPath());
        }

        final String targetFolder = sanitizeFolder(outputFolder == null || outputFolder.isBlank() ? DEFAULT_OUTPUT_FOLDER : outputFolder);
        final File outputDirectory = new File(plugin.getConfiguration().getMenuDirector(), targetFolder);
        if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
            throw new IOException("Could not create output folder " + outputDirectory.getPath());
        }

        final CommandPanelsConversionResult result = new CommandPanelsConversionResult();
        final YamlConfiguration deluxeConfig = (YamlConfiguration) plugin.getConfig();
        final List<File> sortedFiles = new ArrayList<>(List.of(files));
        sortedFiles.sort(Comparator.comparing(File::getName));

        for (final File file : sortedFiles) {
            result.fileRead();
            final YamlConfiguration sourceConfig = YamlConfiguration.loadConfiguration(file);
            convertFile(sourceConfig, file, outputDirectory, targetFolder, deluxeConfig, result);
        }

        plugin.saveConfig();
        return result;
    }

    private void convertFile(
            final @NotNull YamlConfiguration sourceConfig,
            final @NotNull File sourceFile,
            final @NotNull File outputDirectory,
            final @NotNull String outputFolder,
            final @NotNull YamlConfiguration deluxeConfig,
            final @NotNull CommandPanelsConversionResult result
    ) throws IOException {
        final ConfigurationSection legacyPanels = sourceConfig.getConfigurationSection("panels");
        if (legacyPanels != null) {
            for (final String panelName : legacyPanels.getKeys(false)) {
                final ConfigurationSection panel = legacyPanels.getConfigurationSection(panelName);
                if (panel == null) {
                    result.menuSkipped();
                    continue;
                }
                try {
                    saveConvertedPanel(panelName, panel, outputDirectory, outputFolder, deluxeConfig, result);
                } catch (final RuntimeException exception) {
                    result.menuSkipped();
                    result.warn("Skipped " + panelName + " in " + sourceFile.getName() + ": " + exception.getMessage());
                }
            }
            return;
        }

        final String panelName = sourceConfig.getString("panel", stripExtension(sourceFile.getName()));
        try {
            saveConvertedPanel(panelName, sourceConfig, outputDirectory, outputFolder, deluxeConfig, result);
        } catch (final RuntimeException exception) {
            result.menuSkipped();
            result.warn("Skipped " + panelName + " in " + sourceFile.getName() + ": " + exception.getMessage());
        }
    }

    private void saveConvertedPanel(
            final @NotNull String rawPanelName,
            final @NotNull ConfigurationSection panel,
            final @NotNull File outputDirectory,
            final @NotNull String outputFolder,
            final @NotNull YamlConfiguration deluxeConfig,
            final @NotNull CommandPanelsConversionResult result
    ) throws IOException {
        final String menuName = sanitizeMenuName(rawPanelName);
        final YamlConfiguration converted = convertPanel(menuName, panel, result);
        if (converted.getConfigurationSection("items") == null) {
            result.menuSkipped();
            result.warn("Skipped " + rawPanelName + " because it did not contain convertible items.");
            return;
        }

        final File outputFile = new File(outputDirectory, menuName + ".yml");
        converted.save(outputFile);
        deluxeConfig.set("gui_menus." + menuName + ".file", outputFolder + "/" + outputFile.getName());
        result.menuConverted();
    }

    private @NotNull YamlConfiguration convertPanel(
            final @NotNull String menuName,
            final @NotNull ConfigurationSection panel,
            final @NotNull CommandPanelsConversionResult result
    ) {
        final YamlConfiguration out = new YamlConfiguration();
        out.options().indent(2);

        out.set("menu_title", translateText(panel.getString("title", panel.getString("menu_title", menuName))));

        final int size = resolveSize(panel, result, menuName);
        out.set("size", size);

        final List<String> openCommands = getOpenCommands(panel);
        if (!openCommands.isEmpty()) {
            out.set("open_command", openCommands);
        }

        final int refreshInterval = panel.getInt("refresh-delay", panel.getInt("refresh_delay", panel.getInt("update-delay", panel.getInt("update_delay", -1))));
        if (refreshInterval > 0) {
            out.set("refresh", true);
            out.set("refresh_interval", refreshInterval);
            out.set("update_interval", refreshInterval);
        }

        if (panel.getBoolean("hide-player-inventory", panel.getBoolean("hide_player_inventory", false))) {
            out.set("hide_player_inventory", true);
        }

        final ConfigurationSection legacyItems = panel.getConfigurationSection("item");
        final ConfigurationSection modernItems = panel.getConfigurationSection("items");
        final List<String> layout = getStringList(panel, "layout");

        if (legacyItems != null) {
            convertLegacyItems(out, legacyItems, result, menuName);
        } else if (modernItems != null && !layout.isEmpty()) {
            convertLayoutItems(out, modernItems, layout, result, menuName);
        } else if (modernItems != null) {
            convertNamedItems(out, modernItems, result, menuName);
        }

        return out;
    }

    private int resolveSize(
            final @NotNull ConfigurationSection panel,
            final @NotNull CommandPanelsConversionResult result,
            final @NotNull String menuName
    ) {
        if (panel.isInt("rows")) {
            return Math.max(1, Math.min(6, panel.getInt("rows"))) * 9;
        }

        if (panel.isInt("size")) {
            final int size = panel.getInt("size");
            return Math.max(9, Math.min(54, size - (size % 9)));
        }

        final String rows = panel.getString("rows");
        if (rows != null) {
            try {
                return Math.max(1, Math.min(6, Integer.parseInt(rows))) * 9;
            } catch (final NumberFormatException ignored) {
                result.warn("Menu " + menuName + " uses non-chest rows value '" + rows + "'. It was converted as a 54-slot chest.");
            }
        }

        return 54;
    }

    private void convertLegacyItems(
            final @NotNull YamlConfiguration out,
            final @NotNull ConfigurationSection items,
            final @NotNull CommandPanelsConversionResult result,
            final @NotNull String menuName
    ) {
        for (final String slotKey : items.getKeys(false)) {
            final int slot = parseSlot(slotKey, result, menuName);
            if (slot < 0) {
                continue;
            }

            final ConfigurationSection item = items.getConfigurationSection(slotKey);
            if (item == null) {
                continue;
            }

            int priority = 1;
            for (final String key : item.getKeys(false)) {
                if (!key.toLowerCase(Locale.ROOT).matches("has\\d+")) {
                    continue;
                }

                final ConfigurationSection variant = item.getConfigurationSection(key);
                if (variant == null) {
                    continue;
                }

                final String itemPath = "items." + itemName(slot, key) + ".";
                convertItem(out, itemPath, variant, slot, priority++, result, menuName);
                addLegacyHasRequirements(out, itemPath + "view_requirement", variant);
            }

            convertItem(out, "items." + itemName(slot, "base") + ".", item, slot, priority, result, menuName);
        }
    }

    private void convertLayoutItems(
            final @NotNull YamlConfiguration out,
            final @NotNull ConfigurationSection items,
            final @NotNull List<String> layout,
            final @NotNull CommandPanelsConversionResult result,
            final @NotNull String menuName
    ) {
        final Set<Integer> occupiedSlots = new HashSet<>();
        int slot = 0;
        for (final String line : layout) {
            for (final String token : tokenizeLayout(line)) {
                if (token.equalsIgnoreCase("empty") || token.equalsIgnoreCase("air") || token.equals("-")) {
                    slot++;
                    continue;
                }

                final ConfigurationSection item = items.getConfigurationSection(token);
                if (item == null) {
                    result.warn("Menu " + menuName + " references unknown CommandPanels layout item '" + token + "'.");
                    slot++;
                    continue;
                }

                convertItem(out, "items." + itemName(slot, token) + ".", item, slot, 1, result, menuName);
                occupiedSlots.add(slot);
                slot++;
            }
        }

        final String fillItemName = getFillItemName(items);
        if (fillItemName == null) {
            return;
        }

        final ConfigurationSection fillItem = items.getConfigurationSection(fillItemName);
        if (fillItem == null) {
            return;
        }

        for (int index = 0; index < slot; index++) {
            if (occupiedSlots.contains(index)) {
                continue;
            }

            convertItem(out, "items." + itemName(index, "fill") + ".", fillItem, index, 1, result, menuName);
        }
    }

    private void convertNamedItems(
            final @NotNull YamlConfiguration out,
            final @NotNull ConfigurationSection items,
            final @NotNull CommandPanelsConversionResult result,
            final @NotNull String menuName
    ) {
        for (final String key : items.getKeys(false)) {
            final ConfigurationSection item = items.getConfigurationSection(key);
            if (item == null || !item.isInt("slot")) {
                continue;
            }

            final int slot = item.getInt("slot");
            convertItem(out, "items." + itemName(slot, key) + ".", item, slot, 1, result, menuName);
        }
    }

    private void convertItem(
            final @NotNull YamlConfiguration out,
            final @NotNull String path,
            final @NotNull ConfigurationSection item,
            final int slot,
            final int priority,
            final @NotNull CommandPanelsConversionResult result,
            final @NotNull String menuName
    ) {
        out.set(path + "material", convertMaterial(item.getString("material", "STONE")));
        out.set(path + "slot", slot);
        out.set(path + "priority", priority);

        setIfPresent(out, path + "display_name", translateText(item.getString("name", item.getString("display-name", null))));
        setIfPresent(out, path + "lore", translateTextList(getStringList(item, "lore")));
        setIfPresent(out, path + "model_data", item.getString("customdata", item.getString("custom-model-data", item.getString("custom_model_data", null))));
        setIfPresent(out, path + "amount", item.getString("stack", item.getString("amount", null)));
        setIfPresent(out, path + "damage", item.getString("damage", null));

        if (item.getBoolean("tooltip", true) == false) {
            out.set(path + "hide_tooltip", "true");
        }

        final List<String> enchantments = convertEnchantments(getStringList(item, "enchantments"));
        if (!enchantments.isEmpty()) {
            out.set(path + "enchantments", enchantments);
        }

        final List<String> commands = convertCommands(getStringList(item, "commands"));
        if (!commands.isEmpty()) {
            out.set(path + "click_commands", commands);
        }

        final List<String> actions = convertCommands(getStringList(item, "actions.commands"));
        if (!actions.isEmpty()) {
            out.set(path + "click_commands", actions);
        }

        final List<String> leftActions = convertCommands(getStringList(item, "left-click.commands"));
        if (!leftActions.isEmpty()) {
            out.set(path + "left_click_commands", leftActions);
        }

        final List<String> rightActions = convertCommands(getStringList(item, "right-click.commands"));
        if (!rightActions.isEmpty()) {
            out.set(path + "right_click_commands", rightActions);
        }

        final String conditions = item.getString("conditions", null);
        if (conditions != null && !conditions.isBlank()) {
            if (!addExpressionRequirements(out, path + "view_requirement", conditions)) {
                result.warn("Menu " + menuName + " item at slot " + slot + " has an unsupported condition expression: " + conditions);
            }
        }

        final boolean update = containsPlaceholder(item.getString("name", "")) || getStringList(item, "lore").stream().anyMatch(this::containsPlaceholder);
        if (update) {
            out.set(path + "update", true);
        }
    }

    private boolean addExpressionRequirements(final @NotNull YamlConfiguration out, final @NotNull String path, final @NotNull String expression) {
        final String connector;
        if (expression.contains("$AND") && !expression.contains("$OR")) {
            connector = "\\$AND";
        } else if (expression.contains("$OR") && !expression.contains("$AND")) {
            connector = "\\$OR";
            out.set(path + ".minimum_requirements", 1);
        } else {
            connector = null;
        }

        final String[] parts = connector == null ? new String[]{expression} : expression.split(connector);
        int requirement = 1;
        for (final String part : parts) {
            if (!addExpressionRequirement(out, path + ".requirements.condition_" + requirement, part.trim())) {
                return false;
            }
            requirement++;
        }
        return true;
    }

    private boolean addExpressionRequirement(final @NotNull YamlConfiguration out, final @NotNull String path, final @NotNull String expression) {
        final String cleanExpression = expression.replace("(", "").replace(")", "").trim();
        final boolean inverted = cleanExpression.startsWith("$NOT ");
        final String normalized = inverted ? cleanExpression.substring(5).trim() : cleanExpression;
        final String[] parts = normalized.split("\\s+", 3);
        if (parts.length < 3) {
            return false;
        }

        final String input = translateText(parts[0]);
        final String operator = parts[1].toUpperCase(Locale.ROOT);
        final String output = translateText(parts[2]);

        switch (operator) {
            case "$HASPERM":
                out.set(path + ".type", inverted ? "!has permission" : "has permission");
                out.set(path + ".permission", output);
                return true;
            case "$EQUALS":
                out.set(path + ".type", inverted ? "string does not equal ignorecase" : "string equals ignorecase");
                out.set(path + ".input", input);
                out.set(path + ".output", output);
                return true;
            case "$ATLEAST":
                out.set(path + ".type", inverted ? "<" : ">=");
                out.set(path + ".input", input);
                out.set(path + ".output", output);
                return true;
            default:
                return false;
        }
    }

    private void addLegacyHasRequirements(final @NotNull YamlConfiguration out, final @NotNull String path, final @NotNull ConfigurationSection variant) {
        int requirement = 1;
        for (int index = 0; index < 20; index++) {
            final String value = variant.getString("value" + index, null);
            final String compare = variant.getString("compare" + index, null);
            if (value == null || compare == null) {
                continue;
            }

            addLegacyComparison(out, path + ".requirements.condition_" + requirement, value, compare);
            requirement++;
        }
    }

    private void addLegacyComparison(
            final @NotNull YamlConfiguration out,
            final @NotNull String path,
            final @NotNull String rawValue,
            final @NotNull String rawCompare
    ) {
        String value = translateText(rawValue.trim());
        String compare = translateText(rawCompare.trim());
        String type = "string equals ignorecase";
        String input;
        String output;

        if (value.toUpperCase(Locale.ROOT).startsWith("NOT ")) {
            type = "string does not equal ignorecase";
            input = compare;
            output = value.substring(4).trim();
        } else if (value.toUpperCase(Locale.ROOT).endsWith(" ISGREATER")) {
            type = ">";
            input = value.substring(0, value.length() - " ISGREATER".length()).trim();
            output = compare;
        } else if (value.toUpperCase(Locale.ROOT).endsWith(" ISLESS")) {
            type = "<";
            input = value.substring(0, value.length() - " ISLESS".length()).trim();
            output = compare;
        } else if (value.toUpperCase(Locale.ROOT).endsWith(" ISEQUAL")) {
            type = "string equals ignorecase";
            input = value.substring(0, value.length() - " ISEQUAL".length()).trim();
            output = compare;
        } else if (value.toUpperCase(Locale.ROOT).endsWith(" ISNOTEQUAL")) {
            type = "string does not equal ignorecase";
            input = value.substring(0, value.length() - " ISNOTEQUAL".length()).trim();
            output = compare;
        } else if (containsPlaceholder(value) || !containsPlaceholder(compare)) {
            input = value;
            output = compare;
        } else {
            input = compare;
            output = value;
        }

        out.set(path + ".type", type);
        out.set(path + ".input", input);
        out.set(path + ".output", output);
    }

    private @NotNull List<String> convertCommands(final @NotNull List<String> sourceCommands) {
        final List<String> commands = new ArrayList<>();

        for (final String sourceCommand : sourceCommands) {
            if (sourceCommand == null || sourceCommand.isBlank()) {
                continue;
            }

            final String command = translateText(sourceCommand.trim());
            final int equalsIndex = command.indexOf('=');

            if (command.equalsIgnoreCase("cpc") || command.equalsIgnoreCase("close") || command.equalsIgnoreCase("[close]")) {
                commands.add("[close]");
                continue;
            }

            if (command.startsWith("[")) {
                commands.add(convertBracketCommand(command));
                continue;
            }

            if (equalsIndex > 0) {
                final String type = command.substring(0, equalsIndex).trim().toLowerCase(Locale.ROOT);
                final String executable = command.substring(equalsIndex + 1).trim();

                switch (type) {
                    case "open":
                        commands.add("[openguimenu] " + executable);
                        continue;
                    case "open_gui_inventory":
                    case "openguiinventory":
                    case "open_inventory":
                    case "openinventory":
                        commands.add("[open_gui_inventory] " + executable);
                        continue;
                    case "sound":
                        commands.add("[sound] " + executable);
                        continue;
                    case "console":
                        commands.add("[console] " + executable);
                        continue;
                    case "msg":
                    case "message":
                        commands.add((looksMiniMessage(executable) ? "[minimessage] " : "[message] ") + executable);
                        continue;
                    case "minimessage":
                    case "mini_message":
                        commands.add("[minimessage] " + executable);
                        continue;
                    case "server":
                    case "connect":
                        commands.add("[connect] " + executable);
                        continue;
                    case "chat":
                        commands.add("[chat] " + executable);
                        continue;
                    case "close":
                        commands.add("[close]");
                        continue;
                    case "refresh":
                        commands.add("[refresh]");
                        continue;
                    default:
                        commands.add("[player] " + stripSlash(command));
                        continue;
                }
            }

            commands.add("[player] " + stripSlash(command));
        }

        return commands;
    }

    private @NotNull String convertBracketCommand(final @NotNull String command) {
        final int endIndex = command.indexOf(']');
        if (endIndex <= 1) {
            return "[player] " + stripSlash(command);
        }

        final String type = command.substring(1, endIndex).trim().toLowerCase(Locale.ROOT);
        final String executable = command.substring(endIndex + 1).trim();

        switch (type) {
            case "open":
                return "[openguimenu] " + executable;
            case "open_gui_inventory":
            case "openguiinventory":
            case "open_inventory":
            case "openinventory":
                return "[open_gui_inventory] " + executable;
            case "msg":
            case "message":
                return (looksMiniMessage(executable) ? "[minimessage] " : "[message] ") + executable;
            case "server":
                return "[connect] " + executable;
            case "sound":
            case "console":
            case "chat":
            case "close":
            case "refresh":
                return "[" + type + "]" + (executable.isBlank() ? "" : " " + executable);
            default:
                return "[player] " + stripSlash(command);
        }
    }

    private @NotNull String convertMaterial(final @NotNull String sourceMaterial) {
        final String material = translateText(sourceMaterial.trim());
        final String lowerMaterial = material.toLowerCase(Locale.ROOT);

        if (containsPlaceholder(material)) {
            return "placeholder-" + material;
        }

        if (lowerMaterial.startsWith("cps=")) {
            final String value = material.substring(material.indexOf('=') + 1).trim();
            return value.equalsIgnoreCase("self") ? "head-%player_name%" : "head-" + value;
        }

        if (lowerMaterial.startsWith("head=") || lowerMaterial.startsWith("skull=")) {
            return "head-" + material.substring(material.indexOf('=') + 1).trim();
        }

        if (lowerMaterial.startsWith("basehead=")) {
            return "basehead-" + material.substring(material.indexOf('=') + 1).trim();
        }

        if (lowerMaterial.startsWith("hdb=")) {
            return "hdb-" + material.substring(material.indexOf('=') + 1).trim();
        }

        if (lowerMaterial.startsWith("itemsadder=")) {
            return "itemsadder-" + material.substring(material.indexOf('=') + 1).trim();
        }

        if (lowerMaterial.startsWith("nexo=")) {
            return "nexo-" + material.substring(material.indexOf('=') + 1).trim();
        }

        if (lowerMaterial.startsWith("oraxen=")) {
            return "oraxen-" + material.substring(material.indexOf('=') + 1).trim();
        }

        if (lowerMaterial.startsWith("craftengine=")) {
            return "craftengine-" + material.substring(material.indexOf('=') + 1).trim();
        }

        if (lowerMaterial.startsWith("mmo=") || lowerMaterial.startsWith("mmoitems=")) {
            final String value = material.substring(material.indexOf('=') + 1).trim().replace(' ', ':');
            return "mmoitems-" + value;
        }

        return material.toUpperCase(Locale.ROOT);
    }

    private @NotNull List<String> convertEnchantments(final @NotNull List<String> sourceEnchantments) {
        final List<String> enchantments = new ArrayList<>();
        for (final String enchantment : sourceEnchantments) {
            final String[] parts = enchantment.trim().split("\\s+", 2);
            if (parts.length == 0 || parts[0].isBlank()) {
                continue;
            }

            final String level = parts.length > 1 ? parts[1].trim() : "1";
            enchantments.add(parts[0].toUpperCase(Locale.ROOT) + ";" + level);
        }
        return enchantments;
    }

    private @NotNull List<String> getOpenCommands(final @NotNull ConfigurationSection panel) {
        final List<String> commands = new ArrayList<>();

        if (panel.isList("commands")) {
            commands.addAll(getStringList(panel, "commands"));
        }

        if (panel.isString("command")) {
            commands.add(panel.getString("command", ""));
        }

        commands.addAll(getStringList(panel, "aliases"));
        commands.removeIf(String::isBlank);
        return commands;
    }

    private @Nullable String getFillItemName(final @NotNull ConfigurationSection items) {
        for (final String key : items.getKeys(false)) {
            final ConfigurationSection item = items.getConfigurationSection(key);
            if (item != null && item.getBoolean("fill", false)) {
                return key;
            }
        }
        return null;
    }

    private @NotNull List<String> tokenizeLayout(final @NotNull String line) {
        final String trimmed = line.trim();
        if (trimmed.contains(" ")) {
            return List.of(trimmed.split("\\s+"));
        }

        final List<String> tokens = new ArrayList<>();
        for (int i = 0; i < trimmed.length(); i++) {
            tokens.add(String.valueOf(trimmed.charAt(i)));
        }
        return tokens;
    }

    private @NotNull File resolvePanelsFolder(final @NotNull File source) throws IOException {
        if (!source.exists()) {
            throw new IOException("Source path does not exist: " + source.getPath());
        }

        if (source.isFile()) {
            return source.getParentFile();
        }

        final File nestedPanels = new File(source, "panels");
        if (nestedPanels.isDirectory()) {
            return nestedPanels;
        }

        return source;
    }

    private @NotNull File resolveDefaultSource() throws IOException {
        final File converterDirectory = plugin.getConfiguration().getConverterDirectory();
        if (!converterDirectory.exists() && !converterDirectory.mkdirs()) {
            throw new IOException("Could not create converter folder " + converterDirectory.getPath());
        }

        final List<File> candidates = List.of(
                new File(converterDirectory, "CommandPanels"),
                new File(converterDirectory, "commandpanels"),
                new File(converterDirectory, "panels"),
                converterDirectory
        );

        for (final File candidate : candidates) {
            if (!candidate.exists()) {
                continue;
            }

            final File panelsFolder = resolvePanelsFolder(candidate);
            final File[] files = panelsFolder.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".yml"));
            if (files != null && files.length > 0) {
                return candidate;
            }
        }

        throw new IOException("Put your CommandPanels folder into " + converterDirectory.getPath() + " and run /dm convertcommandpanels again.");
    }

    private int parseSlot(final @NotNull String key, final @NotNull CommandPanelsConversionResult result, final @NotNull String menuName) {
        try {
            return Integer.parseInt(key);
        } catch (final NumberFormatException exception) {
            result.warn("Menu " + menuName + " contains non-numeric legacy item slot '" + key + "'.");
            return -1;
        }
    }

    private @NotNull String itemName(final int slot, final @NotNull String suffix) {
        return "slot_" + slot + "_" + sanitizeMenuName(suffix);
    }

    private @NotNull String stripExtension(final @NotNull String filename) {
        final int index = filename.lastIndexOf('.');
        return index == -1 ? filename : filename.substring(0, index);
    }

    private @NotNull String sanitizeFolder(final @NotNull String name) {
        return name.replace("\\", "/").replace("..", "").replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private @NotNull String sanitizeMenuName(final @NotNull String name) {
        final String sanitized = name.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "_");
        return sanitized.isBlank() ? "menu" : sanitized;
    }

    private void setIfPresent(final @NotNull YamlConfiguration out, final @NotNull String path, final @Nullable Object value) {
        if (value == null) {
            return;
        }

        if (value instanceof List && ((List<?>) value).isEmpty()) {
            return;
        }

        out.set(path, value);
    }

    private @NotNull List<String> getStringList(final @NotNull ConfigurationSection section, final @NotNull String path) {
        if (section.isList(path)) {
            return section.getStringList(path);
        }

        if (section.isString(path)) {
            return List.of(section.getString(path, ""));
        }

        return List.of();
    }

    private @NotNull List<String> translateTextList(final @NotNull List<String> source) {
        final List<String> translated = new ArrayList<>();
        for (final String line : source) {
            if (line == null) {
                continue;
            }

            translated.add(translateText(line));
        }
        return translated;
    }

    private @Nullable String translateText(final @Nullable String source) {
        if (source == null) {
            return null;
        }

        return source
                .replace("%cp-player-name%", "%player_name%")
                .replace("%cp-player-uuid%", "%player_uuid%")
                .replace("%cp-player-world%", "%player_world%");
    }

    private boolean containsPlaceholder(final @NotNull String input) {
        return input.contains("%");
    }

    private boolean looksMiniMessage(final @NotNull String input) {
        final String lowerInput = input.toLowerCase(Locale.ROOT);
        return lowerInput.contains("<#")
                || lowerInput.contains("<gradient")
                || lowerInput.contains("<rainbow")
                || lowerInput.contains("<white>")
                || lowerInput.contains("<gray>")
                || lowerInput.contains("<green>")
                || lowerInput.contains("<red>")
                || lowerInput.contains("<blue>")
                || lowerInput.contains("<yellow>")
                || lowerInput.contains("<gold>");
    }

    private @NotNull String stripSlash(final @NotNull String command) {
        return command.startsWith("/") ? command.substring(1) : command;
    }
}
