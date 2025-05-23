package com.extendedclip.deluxemenus.persistentmeta;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.utils.DebugLevel;
import com.extendedclip.deluxemenus.utils.Pair;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Longs;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

@SuppressWarnings({"unchecked", "rawtypes"})
public class PersistentMetaHandler {

    private final DeluxeMenus plugin;

    public PersistentMetaHandler(@NotNull final DeluxeMenus plugin) {
        this.plugin = plugin;
    }

    /**
     * Check if a player has a meta value in their {@link org.bukkit.persistence.PersistentDataContainer}.
     * It will check all supported types. See {@link DataType#getSupportedTypes()}.
     *
     * @param player The player to check.
     * @param key    The key of the meta value.
     * @return True if the player has the meta value, false if not.
     */
    public boolean hasMetaValue(
            @NotNull final Player player,
            @NotNull final NamespacedKey key
    ) {
        return DataType.getSupportedTypes()
                .stream()
                .distinct()
                .anyMatch(type -> hasMetaValue(player, key, type));
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
            @NotNull final DataType<?, ?> type
    ) {
        return player.getPersistentDataContainer().has(key, type.getPDType()) && type.isSupported(player.getPersistentDataContainer().get(key, type.getPDType()));
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
            @NotNull final DataType<?, T> type
    ) {
        if (!player.getPersistentDataContainer().has(key, type.getPDType())) {
            return null;
        }

        final T value = player.getPersistentDataContainer().get(key, type.getPDType());
        if (value == null || !type.isSupported(value)) {
            return null;
        }

        return value;
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
            @NotNull final DataType<?, T> type,
            @NotNull final T defaultValue
    ) {
        final T value = getMetaValue(player, key, type);
        if (value != null) {
            return value;
        }
        return defaultValue;
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
            @NotNull final DataType<?, T> type
    ) {
        return player.getPersistentDataContainer().getKeys().stream()
                .filter(key -> player.getPersistentDataContainer().has(key, type.getPDType()))
                .map(key -> Pair.of(key.toString(), player.getPersistentDataContainer().get(key, type.getPDType())))
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> type.isSupported(entry.getValue()))
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
    public @NotNull OperationResult setMetaValue(
            @NotNull final Player player,
            @NotNull final NamespacedKey key,
            @NotNull final DataType type,
            @NotNull final Object value
    ) {
        if (!type.isSupported(value)) {
            return OperationResult.NEW_VALUE_IS_DIFFERENT_TYPE;
        }

        if (player.getPersistentDataContainer().has(key) &&
                (!player.getPersistentDataContainer().has(key, type.getPDType()) || !type.isSupported(player.getPersistentDataContainer().get(key, type.getPDType())))) {
            return OperationResult.EXISTENT_VALUE_IS_DIFFERENT_TYPE;
        }

        player.getPersistentDataContainer().set(key, type.getPDType(), value);
        return OperationResult.SUCCESS;
    }

    /**
     * Remove a meta value from a player's {@link org.bukkit.persistence.PersistentDataContainer}.
     *
     * @param player The player to remove the meta value from.
     * @param key    The key of the meta value.
     * @param type  The type of the meta value.
     * @return The result of the operation.
     */
    public @NotNull OperationResult removeMetaValue(
            @NotNull final Player player,
            @NotNull final NamespacedKey key,
            @NotNull final DataType<?, ?> type
    ) {
        if (player.getPersistentDataContainer().has(key) &&
                (!player.getPersistentDataContainer().has(key, type.getPDType()) || !type.isSupported(player.getPersistentDataContainer().get(key, type.getPDType())))) {
            return OperationResult.EXISTENT_VALUE_IS_DIFFERENT_TYPE;
        }

        if (!player.getPersistentDataContainer().has(key, type.getPDType())) {
            return OperationResult.VALUE_NOT_FOUND;
        }

        player.getPersistentDataContainer().remove(key);
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
            @NotNull final NamespacedKey key
    ) {
        if (!player.getPersistentDataContainer().has(key)) {
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
        if (player.getPersistentDataContainer().has(key) && !player.getPersistentDataContainer().has(key, DataType.BOOLEAN.getPDType())) {
            return OperationResult.EXISTENT_VALUE_IS_DIFFERENT_TYPE;
        }

        final String currentValue = player.getPersistentDataContainer().getOrDefault(key, DataType.BOOLEAN.getPDType(), "false");
        if (!DataType.BOOLEAN.isSupported(currentValue)) {
            return OperationResult.EXISTENT_VALUE_IS_DIFFERENT_TYPE;
        }

        player.getPersistentDataContainer().set(key, DataType.BOOLEAN.getPDType(), currentValue.equalsIgnoreCase("true") ? "false" : "true");
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
            @NotNull final DataType type,
            @NotNull final Number value
    ) {
        if (type != DataType.DOUBLE && type != DataType.LONG && type != DataType.INTEGER) {
            return OperationResult.INVALID_TYPE;
        }

        if (player.getPersistentDataContainer().has(key) &&
                (!player.getPersistentDataContainer().has(key, type.getPDType()) || !type.isSupported(player.getPersistentDataContainer().get(key, type.getPDType())))) {
            return OperationResult.EXISTENT_VALUE_IS_DIFFERENT_TYPE;
        }

        final Object currentValue = player.getPersistentDataContainer().get(key, type.getPDType());

        if (type == DataType.DOUBLE) {
            final double newValue = (double) (currentValue == null ? 0.0 : currentValue) + value.doubleValue();
            player.getPersistentDataContainer().set(key, type.getPDType(), newValue);
            return OperationResult.SUCCESS;
        }

        final long newValue = (long) (currentValue == null ? 0 : currentValue) + value.longValue();
        player.getPersistentDataContainer().set(key, type.getPDType(), newValue);
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
            @NotNull final DataType type,
            @NotNull final Number value
    ) {
        if (type != DataType.DOUBLE && type != DataType.LONG && type != DataType.INTEGER) {
            return OperationResult.INVALID_TYPE;
        }

        if (player.getPersistentDataContainer().has(key) &&
                (!player.getPersistentDataContainer().has(key, type.getPDType()) || !type.isSupported(player.getPersistentDataContainer().get(key, type.getPDType())))) {
            return OperationResult.EXISTENT_VALUE_IS_DIFFERENT_TYPE;
        }

        final Object currentValue = player.getPersistentDataContainer().get(key, type.getPDType());

        if (type == DataType.DOUBLE) {
            final double newValue = (double) (currentValue == null ? 0.0 : currentValue) - value.doubleValue();
            player.getPersistentDataContainer().set(key, type.getPDType(), newValue);
            return OperationResult.SUCCESS;
        }

        final long newValue = (long) (currentValue == null ? 0 : currentValue) - value.longValue();
        player.getPersistentDataContainer().set(key, type.getPDType(), newValue);
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

        DataAction action = DataAction.getActionByName(args[0]);
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

        final DataType<?, ?> type = DataType.getSupportedTypeByName(args[2]);
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
            @NotNull final DataType<?, ?> type,
            @Nullable final String value
    ) {
        if (value == null) {
            return null;
        }

        if (type == DataType.BOOLEAN) {
            if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                return null;
            }

            return value;
        }

        if (type == DataType.STRING) {
            return value;
        }

        if (type == DataType.DOUBLE) {
            return Doubles.tryParse(value);
        }

        if (type == DataType.LONG || type == DataType.INTEGER) {
            return Longs.tryParse(value);
        }

        return null;
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

    public enum OperationResult {
        SUCCESS,  // Operation was successful
        INVALID_SYNTAX,  // Used when parsing an action from a string and the syntax is invalid
        INVALID_TYPE,  // Used when the type is not supported
        VALUE_NOT_FOUND,  // Used when no value was found with the specified key and/or type
        EXISTENT_VALUE_IS_DIFFERENT_TYPE,  // Used when the value already exists but is a different type
        NEW_VALUE_IS_DIFFERENT_TYPE  // Used when the new value is of an unsupported type
    }
}
