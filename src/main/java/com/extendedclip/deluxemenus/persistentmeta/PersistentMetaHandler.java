package com.extendedclip.deluxemenus.persistentmeta;

import com.extendedclip.deluxemenus.DeluxeMenus;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.extendedclip.deluxemenus.utils.DebugLevel;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PersistentMetaHandler {

  private final DeluxeMenus plugin;

  public PersistentMetaHandler(@NotNull final DeluxeMenus plugin) {
      this.plugin = plugin;
  }

  /**
   * Get a {@link PersistentDataType} from its name.
   *
   * @param name The name of the type.
   * @return The type, or null if type does not exist or is not supported.
   */
  public @Nullable PersistentDataType<?, ?> getSupportedType(@NotNull final String name) {
    switch (name.toUpperCase(Locale.ROOT)) {
      case "DOUBLE":
        return PersistentDataType.DOUBLE;
      case "INTEGER":
      case "LONG":
        return PersistentDataType.LONG;
      case "STRING":
      case "BOOLEAN":
        return PersistentDataType.STRING;
    }
    return null;
  }

  /**
   * Get a {@link NamespacedKey} from a string. If the key contains a namespace, it will use that, otherwise it will
   * use the plugin's namespace.
   *
   * @param key The string to get the {@link NamespacedKey} from.
   * @return The {@link NamespacedKey}.
   */
  private @NotNull NamespacedKey getKey(@NotNull final String key) {
    final NamespacedKey namespacedKey;

    if (key.contains(":")) {
      final String[] split = key.split(":", 2);
      namespacedKey = new NamespacedKey(split[0], split[1]);
    } else {
      namespacedKey = new NamespacedKey(plugin, key);
    }

    return namespacedKey;
  }

  /**
   * Get a meta value from a player's {@link org.bukkit.persistence.PersistentDataContainer}.
   *
   * @param player The player to get the meta value from.
   * @param key The key of the meta value.
   * @param typeName The name of the type of the meta value.
   * @param defaultValue The default value if no meta value will be found.
   * @return The meta value or the default value if no meta value was found.
   */
  public @Nullable String getMeta(
      @NotNull final Player player,
      @NotNull final String key,
      @NotNull final String typeName,
      @Nullable final String defaultValue
  ) {
    final NamespacedKey namespacedKey;
    try {
      namespacedKey = getKey(key);
    } catch (final IllegalArgumentException e) {
      plugin.debug(
              DebugLevel.HIGHEST,
              Level.WARNING,
              "Failed to get meta value for player " + player.getName() + " with key '" + key + "' and type '" + typeName.toUpperCase(Locale.ROOT) + "'. Reason: " + e.getMessage()
      );
      return defaultValue;
    }
    final PersistentDataType<?, ?> type = getSupportedType(typeName);

    if (type == null) {
      return defaultValue;
    }

    final Object result;
    try {
      result = player.getPersistentDataContainer().get(namespacedKey, type);
    } catch (final IllegalArgumentException e) {
      plugin.debug(
              DebugLevel.HIGHEST,
              Level.WARNING,
              "Failed to get meta value for player " + player.getName() + " with key '" + key + "' and type '" + typeName.toUpperCase(Locale.ROOT) + "'. Reason: Saved tag can not be converted to type: " + typeName.toUpperCase(Locale.ROOT)
      );
      return defaultValue;
    }

    if (result == null) {
      return defaultValue;
    }

    return result.toString();
  }

  /**
   * Set a meta value in a player's {@link org.bukkit.persistence.PersistentDataContainer}.
   *
   * @param player The player to set the meta value for.
   * @param input The action type, key name, data type and value joined by spaces.
   * @throws NumberFormatException If the value is not a number while the type is LONG or DOUBLE.
   */
  public boolean setMeta(@NotNull final Player player, @NotNull final String input) throws NumberFormatException {
    // [meta] set points INTEGER 0
    String[] args = input.split(" ", 4);

    if (args.length < 4) {
      return false;
    }

    DataAction action = DataAction.getByName(args[0]);
    if (action == null) {
      return false;
    }

    final PersistentDataType<?, ?> type = getSupportedType(args[2]);
    if (type == null) {
      return false;
    }

    final NamespacedKey namespacedKey;
    try {
      namespacedKey = getKey(args[1]);
    } catch (final IllegalArgumentException e) {
      plugin.debug(
              DebugLevel.HIGHEST,
              Level.WARNING,
              "Failed to set meta value for player " + player.getName() + " with key '" + args[1] + "' and type '" + args[2].toUpperCase(Locale.ROOT) + "'. Reason: " + e.getMessage()
      );
      return false;
    }

    return setMeta(player, namespacedKey, type, action, args[3]);
  }

  /**
   * Set a meta value in a player's {@link org.bukkit.persistence.PersistentDataContainer}.
   *
   * @param player The player to set the meta value in.
   * @param key The key of the meta value.
   * @param type The type of the meta value.
   * @param action The action to perform on the meta value.
   * @param value The value to use.
   * @return True if the meta value was changed, false if not.
   * @throws NumberFormatException If the value is not a number while the type is LONG or DOUBLE.
   */
  public boolean setMeta(
      @NotNull final Player player,
      @NotNull final NamespacedKey key,
      @NotNull final PersistentDataType type,
      @NotNull final DataAction action,
      @NotNull final String value
  ) throws NumberFormatException {
    if (value.equalsIgnoreCase("null")) {
      player.getPersistentDataContainer().remove(key);
      return true;
    }

    switch (action) {
      case SET:
        if (type == PersistentDataType.STRING) {
          player.getPersistentDataContainer().set(key, type, value);
          return true;
        }

        if (type == PersistentDataType.DOUBLE) {
          player.getPersistentDataContainer().set(key, type, Double.parseDouble(value));
          return true;
        }

        if (type == PersistentDataType.LONG) {
          player.getPersistentDataContainer().set(key, type, Long.parseLong(value));
          return true;
        }

        return false;

      case REMOVE:
        player.getPersistentDataContainer().remove(key);
        return true;

      case SWITCH:
        final boolean currentValueSwitch = Boolean.parseBoolean(
            player.getPersistentDataContainer().getOrDefault(key, type, value));

        player.getPersistentDataContainer().set(key, type, String.valueOf(!currentValueSwitch));
        return true;

      case ADD:
        if (type == PersistentDataType.STRING) {
          return false;
        }

        final Object currentValueAdd = player.getPersistentDataContainer().getOrDefault(key, type, 0);

        if (type == PersistentDataType.DOUBLE) {
          final double toAdd = Double.parseDouble(currentValueAdd.toString()) + Double.parseDouble(value);
          player.getPersistentDataContainer().set(key, type, toAdd);
          return true;
        }

        final long toAddLong = Long.parseLong(currentValueAdd.toString()) + Long.parseLong(value);
        player.getPersistentDataContainer().set(key, type, toAddLong);
        return true;

      case SUBTRACT:
        if (type == PersistentDataType.STRING) {
          return false;
        }

        final Object currentValueSubtract = player.getPersistentDataContainer().getOrDefault(key, type, 0);

        if (type == PersistentDataType.DOUBLE) {
          final double toSub = ((double) currentValueSubtract) - Double.parseDouble(value);
          player.getPersistentDataContainer().set(key, type, toSub);
          return true;
        }

        final long toSubLong = Long.parseLong(currentValueSubtract.toString()) - Long.parseLong(value);
        player.getPersistentDataContainer().set(key, type, toSubLong);
        return true;
    }

    return false;
  }

  public enum DataAction {
    SET, REMOVE, ADD, SUBTRACT, SWITCH;

    final static Map<String, DataAction> BY_NAME = Arrays.stream(values())
        .collect(Collectors.toUnmodifiableMap(Enum::name, Function.identity()));

    /**
     * Get a {@link DataAction} by its name.
     * @param name The name of the action type.
     * @return The {@link DataAction} or null if it does not exist.
     */
    public static @Nullable DataAction getByName(@NotNull final String name) {
      return BY_NAME.get(name.toUpperCase(Locale.ROOT));
    }
  }
}
