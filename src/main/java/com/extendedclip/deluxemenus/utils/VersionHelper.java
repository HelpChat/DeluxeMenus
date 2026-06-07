package com.extendedclip.deluxemenus.utils;

import com.google.common.primitives.Ints;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
    private static final int V1_21_4 = 1_21_04;
    // Tooltip Style & Item Model
    private static final int V1_21_2 = 1_21_02;
    // Data components
    private static final int V1_20_5 = 1_20_05;
    // ArmorTrims
    private static final int V1_19_4 = 1_19_04;
    // PlayerProfile API
    private static final int V1_18_1 = 1_18_01;
    // Mojang obfuscation changes
    private static final int V1_17   = 1_17_00;
    // Material and components on items change
    private static final int V1_13   = 1_13_00;
    // PDC and customModelData
    private static final int V1_14   = 1_14_00;
    // Hex colors
    private static final int V1_16   = 1_16_00;
    // Paper adventure changes
    private static final int V1_16_5 = 1_16_05;
    // SkullMeta#setOwningPlayer was added
    private static final int V1_12   = 1_12_00;

    public static final int CURRENT_VERSION = getCurrentVersion();

    private static final boolean IS_PAPER = checkPaper();

    // Unversioned craftbukkit package: Spigot >= 26.1.0
    private static final int V26_1_0 = 26_01_00;

    /**
     * Checks if the current version includes the setTooltipStyle and setItemModel
     */
    public static final boolean HAS_TOOLTIP_STYLE = CURRENT_VERSION >= V1_21_2;

    /**
     * Checks if the current version includes the <a href="https://minecraft.wiki/w/Data_component_format">Data Components</a>
     */
    public static final boolean HAS_DATA_COMPONENTS = CURRENT_VERSION >= V1_20_5;

    /**
     * Checks if the current version includes the ArmorTrims API
     */
    public static final boolean HAS_ARMOR_TRIMS = CURRENT_VERSION >= V1_19_4;
    /**
     * Checks if current version includes the PlayerProfile API
     */
    public static final boolean HAS_PLAYER_PROFILES = CURRENT_VERSION >= V1_18_1;

    /**
     * Checks if the current version was a version without versioned packages.
     */
    public static final boolean HAS_OBFUSCATED_NAMES = CURRENT_VERSION >= V1_17;

    /**
     * Checks if the version supports Components or not
     * Paper versions above 1.16.5 would be true
     * Spigot always false
     */
    public static final boolean IS_COMPONENT = IS_PAPER && CURRENT_VERSION >= V1_16_5;

    /**
     * Checks if the version is lower than 1.13 due to the item changes
     */
    public static final boolean IS_ITEM_LEGACY = CURRENT_VERSION < V1_13;

    /**
     * Checks if the version supports {@link org.bukkit.persistence.PersistentDataContainer}
     */
    public static final boolean IS_PDC_VERSION = CURRENT_VERSION >= V1_14;

    /**
     * Checks if the version doesn't have {@link org.bukkit.inventory.meta.SkullMeta#setOwningPlayer(OfflinePlayer)} and
     * {@link org.bukkit.inventory.meta.SkullMeta#setOwner(String)} should be used instead
     */
    public static final boolean IS_SKULL_OWNER_LEGACY = CURRENT_VERSION <= V1_12;

    /**
     * Checks if the version has {@link org.bukkit.inventory.meta.ItemMeta#setCustomModelData(Integer)}
     */
    public static final boolean IS_CUSTOM_MODEL_DATA = CURRENT_VERSION >= V1_14;

    public static final boolean IS_CUSTOM_MODEL_DATA_COMPONENT = CURRENT_VERSION >= V1_21_4;

    public static final boolean IS_HEX_VERSION = CURRENT_VERSION >= V1_16;

    private static List<InventoryType> CHEST_INVENTORY_TYPES = null;

    private static List<InventoryType> VALID_INVENTORY_TYPES = null;

    private static List<InventoryType> getChestInventoryTypes() {
        if (CHEST_INVENTORY_TYPES != null) return CHEST_INVENTORY_TYPES;

        if (CURRENT_VERSION >= V1_14) {
            CHEST_INVENTORY_TYPES = List.of(
                    InventoryType.BARREL,
                    InventoryType.CHEST,
                    InventoryType.CRAFTING,
                    InventoryType.CREATIVE,
                    InventoryType.ENDER_CHEST,
                    InventoryType.LECTERN,
                    InventoryType.MERCHANT,
                    InventoryType.SHULKER_BOX
            );

            return CHEST_INVENTORY_TYPES;
        }

        CHEST_INVENTORY_TYPES = List.of(
                InventoryType.CHEST,
                InventoryType.CRAFTING,
                InventoryType.CREATIVE,
                InventoryType.ENDER_CHEST,
                InventoryType.MERCHANT,
                InventoryType.SHULKER_BOX
        );

        return CHEST_INVENTORY_TYPES;
    }

    public static List<InventoryType> getValidInventoryTypes() {
        if (VALID_INVENTORY_TYPES != null) return VALID_INVENTORY_TYPES;

        final List<InventoryType> chestInventoryTypes = getChestInventoryTypes();
        final List<InventoryType> validInventoryTypes = new ArrayList<>();

        for (final InventoryType inventoryType : InventoryType.values()) {
            if (inventoryType != InventoryType.CHEST && chestInventoryTypes.contains(inventoryType)) continue;
            validInventoryTypes.add(inventoryType);
        }

        VALID_INVENTORY_TYPES = validInventoryTypes;
        return VALID_INVENTORY_TYPES;
    }

    /**
     * Check if the server has access to the Paper API
     * Taken from <a href="https://github.com/PaperMC/PaperLib">PaperLib</a>
     *
     * @return True if on Paper server (or forks), false anything else
     */
    private static boolean checkPaper() {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    /**
     * Gets the current server version
     *
     * @return A protocol like number representing the version, for example 1.16.5 - 1165
     */
    private static int getCurrentVersion() {
        // No need to cache since will only run once
        // Format: major + 2-digit minor + 2-digit patch
        // e.g. 1.20.1 -> 1_20_01 -> 12001, 26.1.2 -> 26_01_02 -> 260102
        final Matcher matcher = Pattern.compile("(?<major>\\d+)\\.(?<minor>\\d+)(?:\\.(?<patch>\\d+))?").matcher(Bukkit.getBukkitVersion());

        final StringBuilder stringBuilder = new StringBuilder();
        if (matcher.find()) {
            stringBuilder.append(matcher.group("major"));
            stringBuilder.append(String.format("%02d", Integer.parseInt(matcher.group("minor"))));
            final String patch = matcher.group("patch");
            if (patch == null) stringBuilder.append("00");
            else stringBuilder.append(String.format("%02d", Integer.parseInt(patch)));
        }

        //noinspection UnstableApiUsage
        final Integer version = Ints.tryParse(stringBuilder.toString());

        // Should never fail
        if (version == null) throw new RuntimeException("Could not retrieve server version!");

        return version;
    }

    public static String getNmsVersion() {
        final String version = Bukkit.getServer().getClass().getPackage().getName();
        return version.substring(version.lastIndexOf('.') + 1);
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
        if (CURRENT_VERSION >= V26_1_0 || (IS_PAPER && CURRENT_VERSION >= V1_20_5)) {
            try {
                return Class.forName("org.bukkit.craftbukkit." + name);
            } catch (ClassNotFoundException ignored) { }
        }
        return Class.forName("org.bukkit.craftbukkit." + NMS_VERSION + "." + name);
    }

}
