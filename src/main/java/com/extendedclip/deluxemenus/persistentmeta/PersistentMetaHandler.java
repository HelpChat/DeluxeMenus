package com.extendedclip.deluxemenus.persistentmeta;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.utils.DebugLevel;
import com.extendedclip.deluxemenus.utils.Pair;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Longs;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PersistentMetaHandler {

    private static final Map<String, PersistentDataType<?, ?>> SUPPORTED_TYPES = Map.of(
            "DOUBLE", PersistentDataType.DOUBLE,
            "INTEGER", PersistentDataType.LONG,
            "LONG", PersistentDataType.LONG,
            "STRING", PersistentDataType.STRING,
            "BOOLEAN", PersistentDataType.STRING
    );

    private final DeluxeMenus plugin;

    public PersistentMetaHandler(@NotNull final DeluxeMenus plugin) {
        this.plugin = plugin;
    }

    /**
     * Check if a player has a meta value in their {@link org.bukkit.persistence.PersistentDataContainer}.
     *
     * @param player The player to check.
     * @param key    The key of the meta value.
     * @return True if the player has the meta value, false if not.
     */
    public boolean hasMetaValue(
            @NotNull final Player player,
            @NotNull final NamespacedKey key
    ) {
        return player.getPersistentDataContainer().has(key);
    }

    /**
     * Check if a player has a meta value in their {@link org.bukkit.persistence.PersistentDataContainer}.
     *
     * @param player The player to check.
     * @param key    The key of the meta value.
     * @param type   The type of the meta value.
     * @return True if the player has the meta value, false if not.
     */
    public boolean hasMetaValue(
            @NotNull final Player player,
            @NotNull final NamespacedKey key,
            @NotNull final PersistentDataType<?, ?> type
    ) {
        return player.getPersistentDataContainer().has(key, type);
    }

    /**
     * Get a meta value from a player's {@link org.bukkit.persistence.PersistentDataContainer}.
     * If the meta value is not found, null is returned.
     *
     * @param player The player to get the meta value from.
     * @param key    The key of the meta value.
     * @param type   The type of the meta value.
     * @return The meta value or null if no meta value was found.
     */
    public @Nullable <T> T getMetaValue(
            @NotNull final Player player,
            @NotNull final NamespacedKey key,
            @NotNull final PersistentDataType<?, T> type
    ) {
        if (!player.getPersistentDataContainer().has(key, type)) {
            return null;
        }

        return player.getPersistentDataContainer().get(key, type);
    }

    /**
     * Get a meta value from a player's {@link org.bukkit.persistence.PersistentDataContainer}.
     * If the meta value is not found, the default value is returned.
     *
     * @param player       The player to get the meta value from.
     * @param key          The key of the meta value.
     * @param type         The type of the meta value.
     * @param defaultValue The default value to return if no meta value was found.
     * @return The meta value or the default value if no meta value was found.
     */
    public @NotNull <T> T getMetaValueOrDefault(
            @NotNull final Player player,
            @NotNull final NamespacedKey key,
            @NotNull final PersistentDataType<?, T> type,
            @NotNull final T defaultValue
    ) {
        if (!player.getPersistentDataContainer().has(key, type)) {
            return defaultValue;
        }

        final T result = player.getPersistentDataContainer().get(key, type);
        return result == null ? defaultValue : result;
    }

    /**
     * Get a list of all meta values of the given type from a player's {@link org.bukkit.persistence.PersistentDataContainer}.
     *
     * @param player The player to get the meta values from.
     * @param type   The type of the meta values.
     * @return A map of all meta values.
     */
    public <T> Map<String, T> getMetaValues(
            @NotNull final Player player,
            @NotNull final PersistentDataType<?, T> type
    ) {
        return player.getPersistentDataContainer().getKeys().stream()
                .filter(key -> player.getPersistentDataContainer().has(key, type))
                .map(key -> Pair.of(key.toString(), player.getPersistentDataContainer().get(key, type)))
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    /**
     * Set a meta value in a player's {@link org.bukkit.persistence.PersistentDataContainer}.
     * If the meta value already exists, it will be overwritten.
     *
     * @param player The player to set the meta value for.
     * @param key    The key of the meta value.
     * @param type   The type of the meta value.
     * @param value  The value to set.
     * @return The result of the operation.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public @NotNull OperationResult setMetaValue(
            @NotNull final Player player,
            @NotNull final NamespacedKey key,
            @NotNull final PersistentDataType type,
            @NotNull final Object value
    ) {
        if (!type.getComplexType().isInstance(value)) {
            return OperationResult.NEW_VALUE_IS_DIFFERENT_TYPE;
        }

        if (player.getPersistentDataContainer().has(key) && !player.getPersistentDataContainer().has(key, type)) {
            return OperationResult.EXISTENT_VALUE_IS_DIFFERENT_TYPE;
        }

        player.getPersistentDataContainer().set(key, type, value);
        return OperationResult.SUCCESS;
    }

    /**
     * Remove a meta value from a player's {@link org.bukkit.persistence.PersistentDataContainer}.
     *
     * @param player The player to remove the meta value from.
     * @param key    The key of the meta value.
     * @return The result of the operation.
     */
    public @NotNull OperationResult removeMetaValue(
            @NotNull final Player player,
            @NotNull final NamespacedKey key,
            @NotNull final PersistentDataType<?, ?> type
    ) {
        if (player.getPersistentDataContainer().has(key) && !player.getPersistentDataContainer().has(key, type)) {
            return OperationResult.EXISTENT_VALUE_IS_DIFFERENT_TYPE;
        }

        if (!player.getPersistentDataContainer().has(key, type)) {
            return OperationResult.VALUE_NOT_FOUND;
        }

        player.getPersistentDataContainer().remove(key);
        return OperationResult.SUCCESS;
    }

    /**
     * Switch a meta value in a player's {@link org.bukkit.persistence.PersistentDataContainer}.
     * <p>The value must be a boolean.
     * If the meta value does not exist, it will be created and set to true.
     * If the meta value is not a boolean, it will not be changed.
     *
     * @param player The player to switch the meta value for.
     * @param key    The key of the meta value.
     * @return The result of the operation.
     */
    public @NotNull OperationResult switchMetaValue(
            @NotNull final Player player,
            @NotNull final NamespacedKey key
    ) {
        if (player.getPersistentDataContainer().has(key) && !player.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            return OperationResult.EXISTENT_VALUE_IS_DIFFERENT_TYPE;
        }

        final String currentValue = player.getPersistentDataContainer().getOrDefault(key, PersistentDataType.STRING, "false");
        if (!currentValue.equalsIgnoreCase("true") && !currentValue.equalsIgnoreCase("false")) {
            return OperationResult.EXISTENT_VALUE_IS_DIFFERENT_TYPE;
        }

        player.getPersistentDataContainer().set(key, PersistentDataType.STRING, currentValue.equalsIgnoreCase("true") ? "false" : "true");
        return OperationResult.SUCCESS;
    }

    /**
     * Perform addition on a meta value in a player's {@link org.bukkit.persistence.PersistentDataContainer}.
     * <p>The value must be a number.
     * If the meta value does not exist, it will be created with the given value.
     * If the meta value is not a number, it will not be changed.
     *
     * @param player The player to add the meta value for.
     * @param key    The key of the meta value.
     * @param type   The type of the meta value.
     * @param value  The value to add.
     * @return The result of the operation.
     */
    public @NotNull OperationResult addMetaValue(
            @NotNull final Player player,
            @NotNull final NamespacedKey key,
            @NotNull final PersistentDataType<?, ?> type,
            @NotNull final Number value
    ) {
        if (type != PersistentDataType.DOUBLE && type != PersistentDataType.LONG) {
            return OperationResult.INVALID_TYPE;
        }

        if (player.getPersistentDataContainer().has(key) && !player.getPersistentDataContainer().has(key, type)) {
            return OperationResult.EXISTENT_VALUE_IS_DIFFERENT_TYPE;
        }

        final Object currentValue = player.getPersistentDataContainer().get(key, type);

        if (type == PersistentDataType.DOUBLE) {
            final double newValue = (double) (currentValue == null ? 0.0 : currentValue) + value.doubleValue();
            player.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, newValue);
            return OperationResult.SUCCESS;
        }

        final long newValue = (long) (currentValue == null ? 0 : currentValue) + value.longValue();
        player.getPersistentDataContainer().set(key, PersistentDataType.LONG, newValue);
        return OperationResult.SUCCESS;
    }

    /**
     * Perform subtraction on a meta value in a player's {@link org.bukkit.persistence.PersistentDataContainer}.
     * <p>The value must be a number.
     * If the meta value does not exist, it will be created with the given value.
     * If the meta value is not a number, it will not be changed.
     *
     * @param player The player to subtract the meta value for.
     * @param key    The key of the meta value.
     * @param type   The type of the meta value.
     * @param value  The value to subtract.
     * @return The result of the operation.
     */
    public @NotNull OperationResult subtractMetaValue(
            @NotNull final Player player,
            @NotNull final NamespacedKey key,
            @NotNull final PersistentDataType<?, ?> type,
            @NotNull final Number value
    ) {
        if (type != PersistentDataType.DOUBLE && type != PersistentDataType.LONG) {
            return OperationResult.INVALID_TYPE;
        }

        if (player.getPersistentDataContainer().has(key) && !player.getPersistentDataContainer().has(key, type)) {
            return OperationResult.EXISTENT_VALUE_IS_DIFFERENT_TYPE;
        }

        final Object currentValue = player.getPersistentDataContainer().get(key, type);

        if (type == PersistentDataType.DOUBLE) {
            final double newValue = (double) (currentValue == null ? 0.0 : currentValue) - value.doubleValue();
            player.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, newValue);
            return OperationResult.SUCCESS;
        }

        final long newValue = (long) (currentValue == null ? 0 : currentValue) - value.longValue();
        player.getPersistentDataContainer().set(key, PersistentDataType.LONG, newValue);
        return OperationResult.SUCCESS;
    }

    /**
     * Parse and execute a meta action from a string.
     * <br>The format is: &lt;action&gt; &lt;key&gt; &lt;type&gt; [value].
     * <br>Example: set points INTEGER 0
     *
     * @param player The player to execute the action for.
     * @param input  The action to execute.
     * @return The result of the operation.
     **/
    public @NotNull OperationResult parseAndExecuteMetaActionFromString(
            @NotNull final Player player,
            @NotNull final String input
    ) {
        // <action> <key> [type] [value] - type is optional for switch action since it only toggles a boolean
        String[] args = input.split(" ", 4);

        if (args.length < 2) {
            return OperationResult.INVALID_SYNTAX;
        }

        DataAction action = getActionByName(args[0]);
        if (action == null) {
            return OperationResult.INVALID_SYNTAX;
        }

        final NamespacedKey key = getKey(args[1]);
        if (key == null) {
            return OperationResult.INVALID_SYNTAX;
        }

        if (action == DataAction.SWITCH) {
            return switchMetaValue(player, key);
        }

        if (args.length < 3) {
            return OperationResult.INVALID_SYNTAX;
        }

        final PersistentDataType<?, ?> type = getSupportedTypeByName(args[2]);
        if (type == null) {
            return OperationResult.INVALID_SYNTAX;
        }

        final Object parsedValue = parseValueByType(type, args.length >= 4 ? args[3] : null);

        switch (action) {
            case SET:
                if (parsedValue == null) {
                    return OperationResult.NEW_VALUE_IS_DIFFERENT_TYPE;
                }

                return setMetaValue(player, key, type, parsedValue);
            case REMOVE:
                return removeMetaValue(player, key, type);
            case ADD:
                if (!(parsedValue instanceof Number)) {
                    return OperationResult.NEW_VALUE_IS_DIFFERENT_TYPE;
                }

                return addMetaValue(player, key, type, (Number) parsedValue);
            case SUBTRACT:
                if (!(parsedValue instanceof Number)) {
                    return OperationResult.NEW_VALUE_IS_DIFFERENT_TYPE;
                }

                return subtractMetaValue(player, key, type, (Number) parsedValue);
        }

        return OperationResult.INVALID_SYNTAX;
    }

    /**
     * Parse a string value into an object based on the type.
     *
     * @param type  The type to parse the value for.
     * @param value The value to parse.
     * @return The parsed value or null if the value is null or could not be parsed.
     */
    public @Nullable Object parseValueByType(
            @NotNull final PersistentDataType<?, ?> type,
            @Nullable final String value
    ) {
        plugin.getLogger().info("Parsing value by type. Value: " + value + ", type: <" + type.getPrimitiveType().getTypeName() + ", " + type.getComplexType().getTypeName() + ">.");
        if (value == null) {
            plugin.getLogger().info("Value is null.");
            return null;
        }

        if (type == PersistentDataType.STRING) {
            plugin.getLogger().info("Value is a string.");
            return value;
        }

        if (type == PersistentDataType.DOUBLE) {
            final Double result = Doubles.tryParse(value);
            plugin.getLogger().info("Value is a double: " + result);
            return result;
        }

        if (type == PersistentDataType.LONG) {
            final Long result = Longs.tryParse(value);
            plugin.getLogger().info("Value is a long: " + result);
            return result;
        }

        plugin.getLogger().info("Unsupported type found.");
        return null;
    }


    // Helper methods

    /**
     * Get a list of all supported types.
     *
     * @return A set of all supported types.
     */
    public static @NotNull Set<String> getSupportedTypes() {
        return SUPPORTED_TYPES.keySet();
    }

    /**
     * Helper method to parse a string into a {@link PersistentDataType}.
     *
     * @param name The name of the type.
     * @return The type, or null if type does not exist or is not supported.
     */
    public static @Nullable PersistentDataType<?, ?> getSupportedTypeByName(@NotNull final String name) {
        return SUPPORTED_TYPES.get(name.toUpperCase(Locale.ROOT));
    }

    /**
     * Helper method to parse a string into a {@link DataAction}.
     *
     * @param name The name of the action type.
     * @return The {@link DataAction} or null if it does not exist.
     */
    public static @Nullable DataAction getActionByName(@NotNull final String name) {
        return DataAction.getByName(name);
    }

    /**
     * Helper method to parse a string into a {@link NamespacedKey}.
     * If the key contains a namespace, it will use that, otherwise it will use the plugin's namespace. If the key is
     * invalid, it will log a warning and return null.
     *
     * @param key The string to parse.
     * @return The {@link NamespacedKey} or null if the key could not be parsed.
     */
    @SuppressWarnings("UnstableApiUsage")
    public @Nullable NamespacedKey getKey(@NotNull final String key) {
        final NamespacedKey namespacedKey;

        try {
            if (key.contains(":")) {
                final String[] split = key.split(":", 2);
                namespacedKey = new NamespacedKey(split[0], split[1]);
            } else {
                namespacedKey = new NamespacedKey(plugin, key);
            }
        } catch (final IllegalArgumentException e) {
            plugin.debug(
                    DebugLevel.HIGHEST,
                    Level.WARNING,
                    "Failed to parse meta key with value: '" + key + "'. Reason: " + e.getMessage()
            );
            return null;
        }

        return namespacedKey;
    }

    public enum DataAction {
        SET, REMOVE, ADD, SUBTRACT, SWITCH;

        final static Map<String, DataAction> BY_NAME = Arrays.stream(values())
                .collect(Collectors.toUnmodifiableMap(Enum::name, Function.identity()));

        /**
         * Get a {@link DataAction} by its name.
         *
         * @param name The name of the action type.
         * @return The {@link DataAction} or null if it does not exist.
         */
        public static @Nullable DataAction getByName(@NotNull final String name) {
            return BY_NAME.get(name.toUpperCase(Locale.ROOT));
        }
    }

    public enum OperationResult {
        SUCCESS,  // Operation was successful
        INVALID_SYNTAX,  // Used when parsing an action from a string and the syntax is invalid
        INVALID_TYPE,  // Used when the type is not supported
        VALUE_NOT_FOUND,  // Used when no value was found with the specified key and/or type
        EXISTENT_VALUE_IS_DIFFERENT_TYPE,  // Used when the value already exists but is a different type
        NEW_VALUE_IS_DIFFERENT_TYPE  // Used when the new value is of an unsupported type
    }
}
