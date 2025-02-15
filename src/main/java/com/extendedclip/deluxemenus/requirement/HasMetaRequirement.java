package com.extendedclip.deluxemenus.requirement;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.MenuHolder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HasMetaRequirement extends Requirement {

  private final DeluxeMenus plugin;
  private final String key;
  private final String value;
  private final String type;
  private final boolean invert;

  public HasMetaRequirement(@NotNull final DeluxeMenus plugin, String key, String type, String value, boolean invert) {
    this.plugin = plugin;
    this.key = key;
    this.type = type.toUpperCase();
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
    String metaVal = plugin.getPersistentMetaHandler()
        .getMeta(player, parsedKey, type, null);
    if (metaVal == null) {
      return invert;
    }

    String expected = holder.setPlaceholdersAndArguments(value);
    metaVal = holder.setPlaceholdersAndArguments(metaVal);

    switch (type) {
      case "STRING":
      case "BOOLEAN":
        return invert != metaVal.equalsIgnoreCase(expected);
      case "INTEGER":
      case "LONG":
        try {
          long metaNum = Long.parseLong(metaVal);
          long toCheck = Long.parseLong(expected);
          boolean pass = metaNum >= toCheck;
          return invert != pass;
        } catch (Exception ex) {
        }
      case "DOUBLE":
        try {
          double metaNum = Double.parseDouble(metaVal);
          double toCheck = Double.parseDouble(expected);
          boolean pass = metaNum >= toCheck;
          return invert != pass;
        } catch (Exception ex) {
        }
    }
    return invert;
  }
}
