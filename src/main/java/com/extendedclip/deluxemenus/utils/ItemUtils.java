package com.extendedclip.deluxemenus.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import static com.extendedclip.deluxemenus.utils.Constants.INVENTORY_ITEM_ACCESSORS;
import static com.extendedclip.deluxemenus.utils.Constants.PLACEHOLDER_PREFIX;
import static com.extendedclip.deluxemenus.utils.Constants.WATER_BOTTLE;

public final class ItemUtils {

    private ItemUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Checks if the string starts with the substring "placeholder-". The check is case-sensitive.
     *
     * @param material The string to check
     * @return true if the string starts with "placeholder-", false otherwise
     */
    public static boolean isPlaceholderMaterial(@NotNull final String material) {
        return material.startsWith(PLACEHOLDER_PREFIX);
    }

    /**
     * Checks if the string is a player item. The check is case-sensitive.
     * Player items are: "main_hand", "off_hand", "armor_helmet", "armor_chestplate", "armor_leggings", "armor_boots"
     *
     * @param material The string to check
     * @return true if the string is a player item, false otherwise
     */
    public static boolean isPlayerItem(@NotNull final String material) {
        return INVENTORY_ITEM_ACCESSORS.containsKey(material);
    }

    /**
     * Checks if the material is a water bottle. The check is case-insensitive.
     *
     * @param material The material to check
     * @return true if the material is a water bottle, false otherwise
     */
    public static boolean isWaterBottle(@NotNull final String material) {
        return material.equalsIgnoreCase(WATER_BOTTLE);
    }

    /**
     * Checks if the material is a banner.
     *
     * @param material The material to check
     * @return true if the material is a banner, false otherwise
     */
    public static boolean isBanner(@NotNull final Material material) {
        return material.name().endsWith("_BANNER");
    }

    /**
     * Checks if the material is a shield.
     *
     * @param material The material to check
     * @return true if the material is a shield, false otherwise
     */
    public static boolean isShield(@NotNull final Material material) {
        return material == Material.SHIELD;
    }

    /**
     * Checks if the ItemStack is a potion or can hold potion effects.
     *
     * @param itemStack The ItemStack to check
     * @return true if the ItemStack is a potion or can hold a potion effect, false otherwise
     */
    public static boolean hasPotionMeta(@NotNull final ItemStack itemStack) {
        return itemStack.getItemMeta() instanceof PotionMeta;
    }

    /**
     * Creates water bottles stack
     * @param amount the amount of water bottles to put in the stack
     * @return the water bottles stack
     */
    public static @NotNull ItemStack createWaterBottles(final int amount) {
        final ItemStack itemStack = new ItemStack(Material.POTION, amount);
        final PotionMeta itemMeta = (PotionMeta) itemStack.getItemMeta();

        if (itemMeta != null) {
            final PotionData potionData = new PotionData(PotionType.WATER);
            itemMeta.setBasePotionData(potionData);
            itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }
}
