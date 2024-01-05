package com.extendedclip.deluxemenus.utils;

import java.util.List;

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

    public static final List<String> HEAD_PREFIXES = List.of(NAMED_HEAD_PREFIX, TEXTURE_HEAD_PREFIX, BASE64_HEAD_PREFIX, HDB_HEAD_PREFIX);

    public static final String NAMED_HEAD_TYPE = "namedhead";
    public static final String TEXTURE_HEAD_TYPE = "texture";
    public static final String BASE64_HEAD_TYPE = "basehead";
    public static final String HDB_HEAD_TYPE = "hdb";

    public static final List<String> HEAD_TYPES = List.of(NAMED_HEAD_TYPE, TEXTURE_HEAD_TYPE, BASE64_HEAD_TYPE, HDB_HEAD_TYPE);

    public static final String PLACEHOLDER_PREFIX = "placeholder-";
    public static final String WATER_BOTTLE = "water_bottle";

    public static final String ITEMSADDER_PREFIX = "itemsadder-";
    public static final String ORAXEN_PREFIX = "oraxen-";
    public static final String MMOITEMS_PREFIX = "mmoitems-";


}
