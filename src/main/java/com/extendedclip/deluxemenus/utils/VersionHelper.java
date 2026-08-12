package com.extendedclip.deluxemenus.utils;

import com.google.common.primitives.Ints;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

/**
 * Class for detecting server version.
 *
 * @author Matt from triumph-gui
 */
public final class VersionHelper {

    private static final String PACKAGE_NAME = Bukkit.getServer().getClass().getPackage().getName();
    public static final String NMS_VERSION = PACKAGE_NAME.substring(PACKAGE_NAME.lastIndexOf('.') + 1);

    // Custom Model Data Component
    private static final int V1_21_4 = 1_21_4;
    // Tooltip Style & Item Model
    private static final int V1_21_2 = 1_21_2;
    // Mojang obfuscation changes
    private static final int V1_17 = 1170;

    public static final int CURRENT_VERSION = getCurrentVersion();

    /**
     * Checks if the current version includes the setTooltipStyle and setItemModel
     */
    public static final boolean HAS_TOOLTIP_STYLE = CURRENT_VERSION >= V1_21_2;

    /**
     * Checks if the current version was a version without versioned packages.
     */
    public static final boolean HAS_OBFUSCATED_NAMES = CURRENT_VERSION >= V1_17;

    public static final boolean IS_CUSTOM_MODEL_DATA_COMPONENT = CURRENT_VERSION >= V1_21_4;

    private static List<InventoryType> VALID_INVENTORY_TYPES = null;

    private static final List<InventoryType> CHEST_INVENTORY_TYPES = List.of(
            InventoryType.BARREL,
            InventoryType.CHEST,
            InventoryType.CRAFTING,
            InventoryType.CREATIVE,
            InventoryType.ENDER_CHEST,
            InventoryType.LECTERN,
            InventoryType.MERCHANT,
            InventoryType.SHULKER_BOX
    );

    public static List<InventoryType> getValidInventoryTypes() {
        if (VALID_INVENTORY_TYPES != null) return VALID_INVENTORY_TYPES;

        final List<InventoryType> validInventoryTypes = new ArrayList<>();

        for (final InventoryType inventoryType : InventoryType.values()) {
            if (inventoryType != InventoryType.CHEST && CHEST_INVENTORY_TYPES.contains(inventoryType)) continue;
            validInventoryTypes.add(inventoryType);
        }

        VALID_INVENTORY_TYPES = validInventoryTypes;
        return VALID_INVENTORY_TYPES;
    }

    /**
     * Gets the current server version
     *
     * @return A protocol like number representing the version, for example 1.16.5 - 1165
     */
    private static int getCurrentVersion() {
        // No need to cache since will only run once
        final Matcher matcher = Pattern.compile("(?<version>\\d+\\.\\d+)(?<patch>\\.\\d+)?").matcher(Bukkit.getBukkitVersion());

        final StringBuilder stringBuilder = new StringBuilder();
        if (matcher.find()) {
            stringBuilder.append(matcher.group("version").replace(".", ""));
            final String patch = matcher.group("patch");
            if (patch == null) stringBuilder.append("0");
            else stringBuilder.append(patch.replace(".", ""));
        }

        //noinspection UnstableApiUsage
        final Integer version = Ints.tryParse(stringBuilder.toString());

        // Should never fail
        if (version == null) throw new RuntimeException("Could not retrieve server version!");

        return version;
    }

    /**
     * Gets the NMS class from class name.
     *
     * @return The NMS class.
     */
    public static Class<?> getNMSClass(final String pkg, final String className) throws ClassNotFoundException {
        if (VersionHelper.HAS_OBFUSCATED_NAMES) {
            return Class.forName("net.minecraft." + pkg + "."  + className);
        }
        return Class.forName("net.minecraft.server." + VersionHelper.NMS_VERSION + "." + className);
    }

    /**
     * Gets the craft class from class name.
     *
     * @return The craft class.
     */
    public static Class<?> getCraftClass(@NotNull final String name) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + NMS_VERSION + "." + name);
    }

}
