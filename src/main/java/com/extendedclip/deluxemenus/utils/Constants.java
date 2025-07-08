package com.extendedclip.deluxemenus.utils;

import com.google.common.collect.ImmutableMap;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class Constants {
    private Constants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String MAIN_HAND = "main_hand";
    public static final String OFF_HAND = "off_hand";
    public static final String HELMET = "armor_helmet";
    public static final String CHESTPLATE = "armor_chestplate";
    public static final String LEGGINGS = "armor_leggings";
    public static final String BOOTS = "armor_boots";

    public static final List<String> PLAYER_ITEMS = List.of(MAIN_HAND, OFF_HAND, HELMET, CHESTPLATE, LEGGINGS, BOOTS);

    public static final String NAMED_HEAD_PREFIX = "head-";
    public static final String TEXTURE_HEAD_PREFIX = "texture-";
    public static final String BASE64_HEAD_PREFIX = "basehead-";
    public static final String HDB_HEAD_PREFIX = "hdb-";

    public static final String NAMED_HEAD_TYPE = "namedhead";
    public static final String TEXTURE_HEAD_TYPE = "texture";
    public static final String BASE64_HEAD_TYPE = "basehead";
    public static final String HDB_HEAD_TYPE = "hdb";

    public static final String PLACEHOLDER_PREFIX = "placeholder-";
    public static final String WATER_BOTTLE = "water_bottle";

    /**
     * A map between a slot name and the method used to get that item from a player's inventory
     */
    public static final Map<String, Function<PlayerInventory, ItemStack>> INVENTORY_ITEM_ACCESSORS = ImmutableMap.<String, Function<PlayerInventory, ItemStack>>builder()
            .put(MAIN_HAND, PlayerInventory::getItemInMainHand)
            .put(OFF_HAND, PlayerInventory::getItemInOffHand)
            .put(HELMET, PlayerInventory::getHelmet)
            .put(CHESTPLATE, PlayerInventory::getChestplate)
            .put(LEGGINGS, PlayerInventory::getLeggings)
            .put(BOOTS, PlayerInventory::getBoots)
            .build();
}
