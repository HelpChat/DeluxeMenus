package com.extendedclip.deluxemenus.requirement;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.MenuHolder;
import com.extendedclip.deluxemenus.persistentmeta.DataType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HasMetaRequirement extends Requirement {

    private final DeluxeMenus plugin;
    private final String key;
    private final String value;
    private final String typeName;
    private final boolean invert;

    public HasMetaRequirement(@NotNull final DeluxeMenus plugin, String key, String typeName, String value, boolean invert) {
        this.plugin = plugin;
        this.key = key;
        this.typeName = typeName.toUpperCase();
        this.value = value;
        this.invert = invert;
    }

    @Override
    public boolean evaluate(MenuHolder holder) {
        final Player player = holder.getViewer();
        if (player == null) {
            return false;
        }

        final String parsedKey = holder.setPlaceholdersAndArguments(key);
        final NamespacedKey namespacedKey = plugin.getPersistentMetaHandler().getKey(parsedKey);
        if (namespacedKey == null) {
            return invert;
        }

        final DataType<?, ?> type = DataType.getSupportedTypeByName(typeName);
        if (type == null) {
            return invert;
        }

        final Object metaValue = plugin.getPersistentMetaHandler().getMetaValue(player, namespacedKey, type);
        if (metaValue == null) {
            return invert;
        }

        final String expectedValue = holder.setPlaceholdersAndArguments(value);
        // TODO: Is there any reason to parse placeholders in the stored value when reading them?
        //  Placeholders are parsed before value are stored. This means there will (or should) be no placeholders when reading.
        final String actualValue = holder.setPlaceholdersAndArguments(String.valueOf(metaValue));

        if (type.equals(DataType.STRING) || type.equals(DataType.BOOLEAN)) {
            return invert != actualValue.equalsIgnoreCase(expectedValue);
        }

        if (type.equals(DataType.LONG) || type.equals(DataType.INTEGER)) {
            try {
                long metaNum = Long.parseLong(actualValue);
                long toCheck = Long.parseLong(expectedValue);
                boolean pass = metaNum >= toCheck;
                return invert != pass;
            } catch (Exception ex) {
                return invert;
            }
        }

        if (type.equals(DataType.DOUBLE)) {
            try {
                double metaNum = Double.parseDouble(actualValue);
                double toCheck = Double.parseDouble(expectedValue);
                boolean pass = metaNum >= toCheck;
                return invert != pass;
            } catch (Exception ex) {
                return invert;
            }
        }

        return invert;
    }
}
