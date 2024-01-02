package com.extendedclip.deluxemenus.menu;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.action.ClickHandler;
import com.extendedclip.deluxemenus.config.DeluxeMenusConfig;
import com.extendedclip.deluxemenus.nbt.NbtProvider;
import com.extendedclip.deluxemenus.requirement.RequirementList;
import com.extendedclip.deluxemenus.utils.DebugLevel;
import com.extendedclip.deluxemenus.utils.StringUtils;
import com.extendedclip.deluxemenus.utils.VersionHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import static com.extendedclip.deluxemenus.utils.Constants.*;

public class MenuItem implements Cloneable {

    /**
     * A map between a slot name and the method used to get that item from a player's inventory
     */
    private static final Map<String, Function<PlayerInventory, ItemStack>> INVENTORY_ITEM_ACCESSORS = ImmutableMap.<String, Function<PlayerInventory, ItemStack>>builder()
            .put(MAIN_HAND, PlayerInventory::getItemInMainHand)
            .put(OFF_HAND, PlayerInventory::getItemInOffHand)
            .put(HELMET, PlayerInventory::getHelmet)
            .put(CHESTPLATE, PlayerInventory::getChestplate)
            .put(LEGGINGS, PlayerInventory::getLeggings)
            .put(BOOTS, PlayerInventory::getBoots)
            .build();

    private String configMaterial;
    private short configData;
    private int configAmount;
    private String customModelData;
    private String configDynamicAmount = null;
    private String configDisplayName;
    private List<String> configLore;
    private DyeColor configBaseColor;
    private HeadType headType;
    private String placeholderData;
    private String rgb;

    private Map<Enchantment, Integer> enchantments;
    private List<PotionEffect> potionEffects;
    private List<Pattern> bannerMeta;
    private List<ItemFlag> itemFlags;

    private boolean unbreakable;
    private boolean hideAttributes;
    private boolean hideEnchants;
    private boolean hidePotionEffects;
    private boolean hideUnbreakable;

    private boolean displayNameHasPlaceholders;
    private boolean loreHasPlaceholders;

    private String nbtString;
    private String nbtInt;
    private List<String> nbtStrings;
    private List<String> nbtInts;

    private int slot;
    private int priority;
    private boolean updatePlaceholders;

    private ClickHandler clickHandler;
    private ClickHandler leftClickHandler;
    private ClickHandler rightClickHandler;
    private ClickHandler shiftLeftClickHandler;
    private ClickHandler shiftRightClickHandler;
    private ClickHandler middleClickHandler;

    private RequirementList viewRequirements;
    private RequirementList clickRequirements;
    private RequirementList leftClickRequirements;
    private RequirementList rightClickRequirements;
    private RequirementList shiftLeftClickRequirements;
    private RequirementList shiftRightClickRequirements;
    private RequirementList middleClickRequirements;

    @Override
    public MenuItem clone() {
        try {
            return (MenuItem) super.clone();
        } catch (final CloneNotSupportedException exception) {
            DeluxeMenus.printStacktrace(
                    "Something went wrong while trying to clone menu item.",
                    exception
            );
        }
        return new MenuItem();
    }

    public ItemStack getItemStack(@NotNull final MenuHolder holder) {
        final Player viewer = holder.getViewer();

        ItemStack itemStack = null;
        int amount = 1;

        String stringMaterial = configMaterial;
        String lowercaseStringMaterial = stringMaterial.toLowerCase(Locale.ROOT);

        if (isPlaceholderMaterial(lowercaseStringMaterial)) {
            stringMaterial = holder.setPlaceholders(stringMaterial.substring(PLACEHOLDER_PREFIX.length()));
            lowercaseStringMaterial = stringMaterial.toLowerCase(Locale.ENGLISH);
        }

        if (isPlayerItem(lowercaseStringMaterial)) {
            final ItemStack playerItem = INVENTORY_ITEM_ACCESSORS.get(lowercaseStringMaterial).apply(viewer.getInventory());

            // Some of the methods are marked as @NotNull, and in theory that means they return an item with material STONE
            if (playerItem == null) {
                return new ItemStack(Material.STONE, amount);
            }

            itemStack = playerItem.clone();
            amount = playerItem.getAmount();
        }

        final int temporaryAmount = amount;

        if (isHeadItem(lowercaseStringMaterial)) {
            itemStack = getItemFromHook(headType.getHookName(), holder.setPlaceholders(stringMaterial.substring(headType.getPrefix().length())))
                    .orElseGet(() -> DeluxeMenus.getInstance().getHead().clone());
        } else if (isItemsAdderItem(lowercaseStringMaterial)) {
            itemStack = getItemFromHook("itemsadder", holder.setPlaceholders(stringMaterial.substring(ITEMSADDER_PREFIX.length())))
                    .orElseGet(() -> new ItemStack(Material.STONE, temporaryAmount));
        } else if (isOraxenItem(lowercaseStringMaterial)) {
            itemStack = getItemFromHook("oraxen", holder.setPlaceholders(stringMaterial.substring(ORAXEN_PREFIX.length())))
                    .orElseGet(() -> new ItemStack(Material.STONE, temporaryAmount));
        } else if (isWaterBottle(lowercaseStringMaterial)) {
            itemStack = createWaterBottle(amount);
        } else if (itemStack == null) {
            final Material material = Material.getMaterial(stringMaterial.toUpperCase(Locale.ROOT));
            if (material == null) {
                DeluxeMenus.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Material: " + stringMaterial + " is not valid! Setting to Stone."
                );
                itemStack = new ItemStack(Material.STONE, amount);
            } else {
                itemStack = new ItemStack(material, amount);
            }
        }

        if (isBanner(itemStack.getType())) {
            final BannerMeta meta = (BannerMeta) itemStack.getItemMeta();
            if (meta != null) {
                if (configBaseColor != null) {
                    meta.setBaseColor(configBaseColor);
                }
                if (bannerMeta != null) {
                    meta.setPatterns(bannerMeta);
                }
                itemStack.setItemMeta(meta);
            }
        }

        if (isShield(itemStack.getType())) {
            final BlockStateMeta blockStateMeta = (BlockStateMeta) itemStack.getItemMeta();

            if (blockStateMeta != null) {
                final Banner banner = (Banner) blockStateMeta.getBlockState();
                if (configBaseColor != null) {
                    banner.setBaseColor(configBaseColor);
                    banner.update();
                    blockStateMeta.setBlockState(banner);
                }
                if (bannerMeta != null) {
                    banner.setPatterns(bannerMeta);
                    banner.update();
                    blockStateMeta.setBlockState(banner);
                }

                itemStack.setItemMeta(blockStateMeta);
            }
        }

        if (hasPotionMeta(itemStack)) {
            final PotionMeta meta = (PotionMeta) itemStack.getItemMeta();

            if (meta != null) {
                if (rgb != null) {
                    final String rgbString = holder.setPlaceholders(rgb);
                    final String[] parts = rgbString.split(",");

                    try {
                        meta.setColor(Color.fromRGB(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()),
                                Integer.parseInt(parts[2].trim())));
                    } catch (Exception ignored) {
                    }
                }

                if (potionEffects != null && !potionEffects.isEmpty()) {
                    for (PotionEffect effect : potionEffects) {
                        meta.addCustomEffect(effect, true);
                    }
                }

                itemStack.setItemMeta(meta);
            }
        }

        if (itemStack.getType() == Material.AIR) {
            return itemStack;
        }

        if (placeholderData != null) {
            try {
                configData = Short.parseShort(holder.setPlaceholders(placeholderData));
            } catch (final Exception exception) {
                DeluxeMenus.printStacktrace(
                        "Invalid placeholder data found: " + holder.setPlaceholders(placeholderData) + ".",
                        exception
                );
            }
        }

        if (configData > 0) {
            itemStack.setDurability(configData);
        }

        if (configAmount != -1) {
            amount = configAmount;
        }

        if (this.configDynamicAmount != null) {
            try {
                final int dynamicAmount = (int) Double.parseDouble(holder.setPlaceholders(this.configDynamicAmount));
                amount = Math.max(dynamicAmount, 1);
            } catch (final NumberFormatException ignored) {
            }
        }

        if (amount > 64) {
            amount = 64;
        }

        itemStack.setAmount(amount);

        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return itemStack;
        }

        if (customModelData != null && VersionHelper.IS_CUSTOM_MODEL_DATA) {
            try {
                final int modelData = Integer.parseInt(holder.setPlaceholders(customModelData));
                itemMeta.setCustomModelData(modelData);
            } catch (final Exception ignored) {
            }
        }

        if (this.configDisplayName != null) {
            final String displayName = holder.setPlaceholders(this.configDisplayName);
            itemMeta.setDisplayName(StringUtils.color(displayName));
        }

        if (this.configLore != null) {
            final List<String> lore = configLore.stream()
                    .map(holder::setPlaceholders)
                    .map(StringUtils::color)
                    .map(line -> line.split("\n"))
                    .flatMap(Arrays::stream)
                    .map(line -> line.split("\\\\n"))
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toList());

            itemMeta.setLore(lore);
        }

        if (itemFlags != null && !itemFlags.isEmpty()) {
            for (final ItemFlag flag : itemFlags) {
                itemMeta.addItemFlags(flag);
            }
        }

        if (this.hideAttributes) {
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }

        if (this.hideEnchants) {
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        if (this.hidePotionEffects) {
            itemMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        }

        if (this.unbreakable) {
            itemMeta.setUnbreakable(true);
        }

        if (this.hideUnbreakable) {
            itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        }

        if (itemMeta instanceof LeatherArmorMeta && rgb != null) {
            final String rgbString = holder.setPlaceholders(rgb);
            final String[] parts = rgbString.split(",");
            final LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemMeta;

            try {
                leatherArmorMeta.setColor(Color.fromRGB(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()),
                        Integer.parseInt(parts[2].trim())));
                itemStack.setItemMeta(leatherArmorMeta);
            } catch (final Exception exception) {
                DeluxeMenus.printStacktrace(
                        "Invalid rgb colors found for leather armor: " + parts[0].trim() + ", " + parts[1].trim() + ", " +
                                parts[2].trim(),
                        exception
                );
            }
        } else if (itemMeta instanceof FireworkEffectMeta && rgb != null) {
            final String rgbString = holder.setPlaceholders(rgb);
            final String[] parts = rgbString.split(",");
            final FireworkEffectMeta fireworkEffectMeta = (FireworkEffectMeta) itemMeta;

            try {
                fireworkEffectMeta.setEffect(FireworkEffect.builder().withColor(Color.fromRGB(Integer.parseInt(parts[0].trim()),
                        Integer.parseInt(parts[1].trim()), Integer.parseInt(parts[2].trim()))).build());
                itemStack.setItemMeta(fireworkEffectMeta);
            } catch (final Exception exception) {
                DeluxeMenus.printStacktrace(
                        "Invalid rgb colors found for firework or firework star: " + parts[0].trim() + ", "
                                + parts[1].trim() + ", " + parts[2].trim(),
                        exception
                );
            }
        } else if (itemMeta instanceof EnchantmentStorageMeta && enchantments != null) {
            final EnchantmentStorageMeta enchantmentStorageMeta = (EnchantmentStorageMeta) itemMeta;
            for (final Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                final boolean result = enchantmentStorageMeta.addStoredEnchant(entry.getKey(), entry.getValue(), true);
                if (!result) {
                    DeluxeMenus.debug(
                            DebugLevel.HIGHEST,
                            Level.INFO,
                            "Failed to add enchantment " + entry.getKey().getName() + " to item " + itemStack.getType()
                    );
                }
            }
            itemStack.setItemMeta(enchantmentStorageMeta);
        } else {
            itemStack.setItemMeta(itemMeta);
        }

        if (!(itemMeta instanceof EnchantmentStorageMeta) && this.enchantments != null) {
            itemStack.addUnsafeEnchantments(enchantments);
        }

        if (NbtProvider.isAvailable()) {
            if (this.nbtString != null) {
                final String tag = holder.setPlaceholders(this.nbtString);
                if (tag.contains(":")) {
                    final String[] parts = tag.split(":", 2);
                    itemStack = NbtProvider.setString(itemStack, parts[0], parts[1]);
                }
            }

            if (this.nbtInt != null) {
                final String tag = holder.setPlaceholders(this.nbtInt);
                if (tag.contains(":")) {
                    final String[] parts = tag.split(":");
                    itemStack = NbtProvider.setInt(itemStack, parts[0], Integer.parseInt(parts[1]));
                }
            }

            for (String nbtTag : this.nbtStrings) {
                final String tag = holder.setPlaceholders(nbtTag);
                if (tag.contains(":")) {
                    final String[] parts = tag.split(":", 2);
                    itemStack = NbtProvider.setString(itemStack, parts[0], parts[1]);
                }
            }

            for (String nbtTag : this.nbtInts) {
                final String tag = holder.setPlaceholders(nbtTag);
                if (tag.contains(":")) {
                    final String[] parts = tag.split(":");
                    itemStack = NbtProvider.setInt(itemStack, parts[0], Integer.parseInt(parts[1]));
                }
            }
        }

        return itemStack;
    }

    /**
     * Checks if the string starts with the substring "placeholder-". The check is case-sensitive.
     *
     * @param material The string to check
     * @return true if the string starts with "placeholder-", false otherwise
     */
    private boolean isPlaceholderMaterial(@NotNull final String material) {
        return material.startsWith(PLACEHOLDER_PREFIX);
    }

    /**
     * Checks if the string is a player item. The check is case-sensitive.
     * Player items are: "main_hand", "off_hand", "armor_helmet", "armor_chestplate", "armor_leggings", "armor_boots"
     *
     * @param material The string to check
     * @return true if the string is a player item, false otherwise
     */
    private boolean isPlayerItem(@NotNull final String material) {
        return INVENTORY_ITEM_ACCESSORS.containsKey(material);
    }

    /**
     * Checks if the string is a head item. The check is case-insensitive.
     * Head items are:
     * <ul>
     * <li>"head-{player-name}" (a simple named player head, supports placeholders. eg. "head-%player_name% or head-extendedclip")</li>
     * <li>"texture-{texture-url}" (a head with a custom texture specified by a texture url. eg. "texture-93a728ad8d31486a7f9aad200edb373ea803d1fc5fd4321b2e2a971348234443")</li>
     * <li>"basehead-{base64-encoded-texture-url}" (a head with a custom texture specified by a base64 encoded texture url)</li>
     * <li>"hdb-{hdb-head-id}" (a head with a custom texture specified by a <a href="https://www.spigotmc.org/resources/14280/">HeadDatabase</a> id)</li>
     * </ul>
     *
     * @param material The string to check
     * @return true if the string is a head item, false otherwise
     */
    private boolean isHeadItem(@NotNull final String material) {
        final Optional<HeadType> headType = HeadType.parseHeadType(material);
        headType.ifPresent(type -> this.headType = type);
        return headType.isPresent();
    }

    /**
     * Checks if the string is an ItemsAdder item. The check is case-sensitive.
     * ItemsAdder items are: "itemsadder-{namespace:name}"
     *
     * @param material The string to check
     * @return true if the string is an ItemsAdder item, false otherwise
     */
    private boolean isItemsAdderItem(@NotNull final String material) {
        return material.startsWith(ITEMSADDER_PREFIX);
    }

    /**
     * Checks if the string is an Oraxen item. The check is case-sensitive.
     * ItemsAdder items are: "oraxen-{namespace:name}"
     *
     * @param material The string to check
     * @return true if the string is an Oraxen item, false otherwise
     */
    private boolean isOraxenItem(@NotNull final String material) {
        return material.startsWith(ORAXEN_PREFIX);
    }

    /**
     * Checks if the material is a water bottle. The check is case-insensitive.
     *
     * @param material The material to check
     * @return true if the material is a water bottle, false otherwise
     */
    private boolean isWaterBottle(@NotNull final String material) {
        return material.equalsIgnoreCase(WATER_BOTTLE);
    }

    /**
     * Checks if the material is a banner.
     *
     * @param material The material to check
     * @return true if the material is a banner, false otherwise
     */
    private boolean isBanner(@NotNull final Material material) {
        return material.name().endsWith("_BANNER");
    }

    /**
     * Checks if the material is a shield.
     *
     * @param material The material to check
     * @return true if the material is a shield, false otherwise
     */
    private boolean isShield(@NotNull final Material material) {
        return material == Material.SHIELD;
    }

    /**
     * Checks if the ItemStack is a potion or can hold potion effects.
     *
     * @param itemStack The ItemStack to check
     * @return true if the ItemStack is a potion or can hold a potion effect, false otherwise
     */
    private boolean hasPotionMeta(@NotNull final ItemStack itemStack) {
        return itemStack.getItemMeta() instanceof PotionMeta;
    }

    private @NotNull ItemStack createWaterBottle(final int amount) {
        final ItemStack itemStack = new ItemStack(Material.POTION, amount);
        final PotionMeta itemMeta = (PotionMeta) itemStack.getItemMeta();

        if (itemMeta != null) {
            final PotionData potionData = new PotionData(PotionType.WATER);
            itemMeta.setBasePotionData(potionData);
            itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }

    private @NotNull Optional<ItemStack> getItemFromHook(String hookName, String... args) {
        return DeluxeMenus.getInstance()
                .getItemHook(hookName)
                .map(itemHook -> itemHook.getItem(args));
    }

    public ClickHandler getClickHandler() {
        return clickHandler;
    }

    public void setClickHandler(ClickHandler clickHandler) {
        this.clickHandler = clickHandler;
    }

    public RequirementList getClickRequirements() {
        return clickRequirements;
    }

    public void setClickRequirements(
            RequirementList clickRequirements) {
        this.clickRequirements = clickRequirements;
    }

    public void setPotionEffects(List<PotionEffect> potionEffects) {
        this.potionEffects = potionEffects;
    }

    public String getCustomModelData() {
        return customModelData;
    }

    public void setCustomModelData(String customModelData) {
        this.customModelData = customModelData;
    }

    public DyeColor getConfigBaseColor() {
        return configBaseColor;
    }

    public void setConfigBaseColor(DyeColor configBaseColor) {
        this.configBaseColor = configBaseColor;
    }

    public @NotNull String getConfigMaterial() {
        return configMaterial;
    }

    public void setConfigMaterial(@NotNull final String configMaterial) {
        this.configMaterial = configMaterial;
    }

    public short getConfigData() {
        return configData;
    }

    public void setConfigData(Short configData) {
        this.configData = configData;
    }

    public int getConfigAmount() {
        return configAmount;
    }

    public void setConfigAmount(int configAmount) {
        this.configAmount = configAmount;
    }

    public String getConfigDisplayName() {
        return configDisplayName;
    }

    public void setConfigDisplayName(String configDisplayName) {
        this.configDisplayName = configDisplayName;
        if (this.configDisplayName != null) {
            this.displayNameHasPlaceholders = DeluxeMenusConfig.containsPlaceholders(this.configDisplayName);
        }
    }

    public List<String> getConfigLore() {
        return configLore;
    }

    public void setConfigLore(List<String> configLore) {
        this.configLore = configLore;
        if (this.configLore != null) {
            this.loreHasPlaceholders = configLore.stream().anyMatch(DeluxeMenusConfig::containsPlaceholders);
        }
    }

    public ClickHandler getLeftClickHandler() {
        return leftClickHandler;
    }

    public void setLeftClickHandler(ClickHandler clickHandler) {
        this.leftClickHandler = clickHandler;
    }

    public ClickHandler getRightClickHandler() {
        return rightClickHandler;
    }

    public void setRightClickHandler(ClickHandler clickHandler) {
        this.rightClickHandler = clickHandler;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean hasViewRequirement() {
        return viewRequirements != null && viewRequirements.getRequirements() != null;
    }

    public RequirementList getViewRequirements() {
        return this.viewRequirements;
    }

    public void setViewRequirements(RequirementList r) {
        this.viewRequirements = r;
    }

    public boolean displayNameHasPlaceholders() {
        return displayNameHasPlaceholders;
    }

    public boolean loreHasPlaceholders() {
        return loreHasPlaceholders;
    }

    public boolean updatePlaceholders() {
        return updatePlaceholders;
    }

    public void setUpdatePlaceholders(boolean updatePlaceholders) {
        this.updatePlaceholders = updatePlaceholders;
    }

    public boolean hideAttributes() {
        return this.hideAttributes;
    }

    public void setHideAttributes(boolean hide) {
        this.hideAttributes = hide;
    }

    public List<ItemFlag> itemFlags() {
        return itemFlags;
    }

    public void setItemFlags(List<ItemFlag> flags) {
        itemFlags = flags;
    }

    public boolean hideEnchants() {
        return hideEnchants;
    }

    public void setHideEnchants(boolean hideEnchants) {
        this.hideEnchants = hideEnchants;
    }

    public boolean hidePotionEffects() {
        return hidePotionEffects;
    }

    public void setHidePotionEffects(boolean hidePotionEffects) {
        this.hidePotionEffects = hidePotionEffects;
    }

    public void setEnchantments(Map<Enchantment, Integer> enchantments) {
        this.enchantments = enchantments;
    }

    public RequirementList getLeftClickRequirements() {
        return leftClickRequirements;
    }

    public void setLeftClickRequirements(RequirementList leftClickRequirements) {
        this.leftClickRequirements = leftClickRequirements;
    }

    public RequirementList getRightClickRequirements() {
        return rightClickRequirements;
    }

    public void setRightClickRequirements(RequirementList rightClickRequirements) {
        this.rightClickRequirements = rightClickRequirements;
    }

    public List<Pattern> getBannerMeta() {
        return bannerMeta;
    }

    public void setBannerMeta(List<Pattern> bannerMeta) {
        this.bannerMeta = bannerMeta;
    }

    public String getConfigDynamicAmount() {
        return configDynamicAmount;
    }

    public void setConfigDynamicAmount(String configDynamicAmount) {
        this.configDynamicAmount = configDynamicAmount;
    }

    public boolean hideUnbreakable() {
        return hideUnbreakable;
    }

    public void setHideUnbreakable(boolean b) {
        this.hideUnbreakable = b;
    }

    public String getRGB() {
        return rgb;
    }

    public void setRGB(String rgb) {
        this.rgb = rgb;
    }

    public boolean isUnbreakable() {
        return unbreakable;
    }

    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    public String getPlaceholderData() {
        return placeholderData;
    }

    public void setPlaceholderData(String placeholderData) {
        this.placeholderData = placeholderData;
    }

    public ClickHandler getShiftLeftClickHandler() {
        return shiftLeftClickHandler;
    }

    public void setShiftLeftClickHandler(ClickHandler shiftLeftClickHandler) {
        this.shiftLeftClickHandler = shiftLeftClickHandler;
    }

    public ClickHandler getShiftRightClickHandler() {
        return shiftRightClickHandler;
    }

    public void setShiftRightClickHandler(ClickHandler shiftRightClickHandler) {
        this.shiftRightClickHandler = shiftRightClickHandler;
    }

    public RequirementList getShiftLeftClickRequirements() {
        return shiftLeftClickRequirements;
    }

    public void setShiftLeftClickRequirements(RequirementList shiftLeftClickRequirements) {
        this.shiftLeftClickRequirements = shiftLeftClickRequirements;
    }

    public RequirementList getShiftRightClickRequirements() {
        return shiftRightClickRequirements;
    }

    public void setShiftRightClickRequirements(RequirementList shiftRightClickRequirements) {
        this.shiftRightClickRequirements = shiftRightClickRequirements;
    }

    public ClickHandler getMiddleClickHandler() {
        return middleClickHandler;
    }

    public void setMiddleClickHandler(ClickHandler middleClickHandler) {
        this.middleClickHandler = middleClickHandler;
    }

    public RequirementList getMiddleClickRequirements() {
        return middleClickRequirements;
    }

    public void setMiddleClickRequirements(RequirementList middleClickRequirements) {
        this.middleClickRequirements = middleClickRequirements;
    }

    public String getNbtString() {
        return nbtString;
    }

    public void setNbtString(String nbtString) {
        this.nbtString = nbtString;
    }

    public String getNbtInt() {
        return nbtInt;
    }

    public void setNbtInt(String nbtInt) {
        this.nbtInt = nbtInt;
    }

    public List<String> getNbtStrings() {
        return nbtStrings;
    }

    public void setNbtStrings(List<String> nbtStrings) {
        this.nbtStrings = nbtStrings;
    }

    public List<String> getNbtInts() {
        return nbtInts;
    }

    public void setNbtInts(List<String> nbtInts) {
        this.nbtInts = nbtInts;
    }
}
