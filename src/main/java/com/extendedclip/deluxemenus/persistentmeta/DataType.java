package com.extendedclip.deluxemenus.persistentmeta;


import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataType<P, C> {
    public static final DataType<Double, Double> DOUBLE = new DataType<>("DOUBLE", PersistentDataType.DOUBLE, value -> true);
    public static final DataType<Long, Long> INTEGER = new DataType<>("INTEGER", PersistentDataType.LONG, value -> true);
    public static final DataType<Long, Long> LONG = new DataType<>("LONG", PersistentDataType.LONG, value -> true);
    public static final DataType<String, String> STRING = new DataType<>("STRING", PersistentDataType.STRING, value -> true);
    public static final DataType<String, String> BOOLEAN = new DataType<>("BOOLEAN", PersistentDataType.STRING, value -> "true".equalsIgnoreCase((String) value) || "false".equalsIgnoreCase((String) value));

    private static final List<DataType<?, ?>> SUPPORTED_TYPES = List.of(DOUBLE, INTEGER, LONG, STRING, BOOLEAN);

    private final String name;
    private final PersistentDataType<P, C> pdType;
    private final Function<Object, Boolean> checker;

    private DataType(
            @NotNull final String name,
            @NotNull final PersistentDataType<P, C> pdType,
            @NotNull final Function<@NotNull Object, @NotNull Boolean> checker
    ) {
        this.name = name;
        this.pdType = pdType;
        this.checker = checker;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull PersistentDataType<P, C> getPDType() {
        return pdType;
    }

    public @NotNull Class<C> getComplexType() {
        return pdType.getComplexType();
    }

    public @NotNull Class<P> getPrimitiveType() {
        return pdType.getPrimitiveType();
    }

    public boolean isSupported(@Nullable final Object value) {
        return this.getComplexType().isInstance(value) && checker.apply(value);
    }

    /**
     * Helper method to parse a string into a {@link DataType}.
     *
     * @param name The name of the type.
     * @return The type, or null if type does not exist or is not supported.
     */
    public static @Nullable DataType<?, ?> getSupportedTypeByName(@NotNull final String name) {
        return SUPPORTED_TYPES.stream().filter(type -> type.name.equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /**
     * Get a list of all supported type names.
     *
     * @return A set of all supported type names.
     */
    public static @NotNull Set<String> getSupportedTypeNames() {
        return SUPPORTED_TYPES.stream().map(DataType::getName).collect(Collectors.toSet());
    }

    /**
     * Get a list of all supported types.
     *
     * @return A list of all supported types.
     */
    public static @NotNull List<DataType<?, ?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

}
