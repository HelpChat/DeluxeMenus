package com.extendedclip.deluxemenus.requirement;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.MenuHolder;
import com.extendedclip.deluxemenus.persistentmeta.PersistentMetaHandler;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
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
        Player player = holder.getViewer();
        if (player == null) {
            return false;
        }
        String parsedKey = holder.setPlaceholdersAndArguments(key);
        final NamespacedKey namespacedKey = plugin.getPersistentMetaHandler().getKey(parsedKey);
        if (namespacedKey == null) {
            return invert;
        }

        final PersistentDataType<?, ?> type = PersistentMetaHandler.getSupportedTypeByName(typeName);
        if (type == null) {
            return invert;
        }

        Object metaValue = plugin.getPersistentMetaHandler().getMetaValue(player, namespacedKey, type);
        if (metaValue == null) {
            return invert;
        }

        final String expectedValue = holder.setPlaceholdersAndArguments(value);
        final String actualValue = holder.setPlaceholdersAndArguments(String.valueOf(metaValue));

        switch (typeName) {
            case "STRING":
            case "BOOLEAN":
                return invert != actualValue.equalsIgnoreCase(expectedValue);
            case "INTEGER":
            case "LONG":
                try {
                    long metaNum = Long.parseLong(actualValue);
                    long toCheck = Long.parseLong(expectedValue);
                    boolean pass = metaNum >= toCheck;
                    return invert != pass;
                } catch (Exception ex) {
                    // In case of an exception, we will return the invert value
                }
            case "DOUBLE":
                try {
                    double metaNum = Double.parseDouble(actualValue);
                    double toCheck = Double.parseDouble(expectedValue);
                    boolean pass = metaNum >= toCheck;
                    return invert != pass;
                } catch (Exception ex) {
                    // In case of an exception, we will return the invert value
                }
        }
        return invert;
    }
}
