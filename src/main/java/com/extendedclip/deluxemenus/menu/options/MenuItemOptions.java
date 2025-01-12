package com.extendedclip.deluxemenus.menu.options;

import com.extendedclip.deluxemenus.action.ClickHandler;
import com.extendedclip.deluxemenus.config.DeluxeMenusConfig;
import com.extendedclip.deluxemenus.requirement.RequirementList;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MenuItemOptions {

    private final String material;
    private final String damage;
    private final int amount;
    private final String customModelData;
    private final String dynamicAmount;
    private final String lightLevel;
    private final String displayName;
    private final List<String> lore;
    private final DyeColor baseColor;
    private HeadType headType;
    private final String rgb;

    private final String trimMaterial;
    private final String trimPattern;

    private final String hideTooltip;
    private final String enchantmentGlintOverride;
    private final String rarity;
    private final String tooltipStyle;
    private final String itemModel;

    private final Map<Enchantment, Integer> enchantments;
    private final List<PotionEffect> potionEffects;
    private final List<Pattern> bannerMeta;
    private final Set<ItemFlag> itemFlags = new HashSet<>();

    private final boolean unbreakable;

    private final boolean displayNameHasPlaceholders;
    private final boolean loreHasPlaceholders;
    private final boolean hasLore;
    private final LoreAppendMode loreAppendMode;

    private final String nbtString;
    private final String nbtByte;
    private final String nbtShort;
    private final String nbtInt;
    private final List<String> nbtStrings;
    private final List<String> nbtBytes;
    private final List<String> nbtShorts;
    private final List<String> nbtInts;

    private final int slot;
    private final int priority;
    private final boolean updatePlaceholders;

    private final ClickHandler clickHandler;
    private final ClickHandler leftClickHandler;
    private final ClickHandler rightClickHandler;
    private final ClickHandler shiftLeftClickHandler;
    private final ClickHandler shiftRightClickHandler;
    private final ClickHandler middleClickHandler;

    private final RequirementList viewRequirements;
    private final RequirementList clickRequirements;
    private final RequirementList leftClickRequirements;
    private final RequirementList rightClickRequirements;
    private final RequirementList shiftLeftClickRequirements;
    private final RequirementList shiftRightClickRequirements;
    private final RequirementList middleClickRequirements;

    private MenuItemOptions(final @NotNull MenuItemOptionsBuilder builder) {
        this.material = builder.material;
        this.damage = builder.damage;
        this.amount = builder.amount;
        this.customModelData = builder.customModelData;
        this.dynamicAmount = builder.dynamicAmount;
        this.lightLevel = builder.lightLevel;
        this.displayName = builder.displayName;
        this.lore = builder.lore;
        this.hasLore = builder.hasLore;
        this.loreAppendMode = builder.loreAppendMode;
        this.baseColor = builder.baseColor;
        this.headType = builder.headType;
        this.rgb = builder.rgb;
        this.trimMaterial = builder.trimMaterial;
        this.trimPattern = builder.trimPattern;
        this.hideTooltip = builder.hideTooltip;
        this.enchantmentGlintOverride = builder.enchantmentGlintOverride;
        this.rarity = builder.rarity;
        this.tooltipStyle = builder.tooltipStyle;
        this.itemModel = builder.itemModel;
        this.enchantments = builder.enchantments;
        this.potionEffects = builder.potionEffects;
        this.bannerMeta = builder.bannerMeta;
        this.itemFlags.addAll(builder.itemFlags);
        this.unbreakable = builder.unbreakable;
        this.displayNameHasPlaceholders = builder.displayNameHasPlaceholders;
        this.loreHasPlaceholders = builder.loreHasPlaceholders;
        this.nbtString = builder.nbtString;
        this.nbtByte = builder.nbtByte;
        this.nbtShort = builder.nbtShort;
        this.nbtInt = builder.nbtInt;
        this.nbtStrings = builder.nbtStrings;
        this.nbtBytes = builder.nbtBytes;
        this.nbtShorts = builder.nbtShorts;
        this.nbtInts = builder.nbtInts;
        this.slot = builder.slot;
        this.priority = builder.priority;
        this.updatePlaceholders = builder.updatePlaceholders;
        this.clickHandler = builder.clickHandler;
        this.leftClickHandler = builder.leftClickHandler;
        this.rightClickHandler = builder.rightClickHandler;
        this.shiftLeftClickHandler = builder.shiftLeftClickHandler;
        this.shiftRightClickHandler = builder.shiftRightClickHandler;
        this.middleClickHandler = builder.middleClickHandler;
        this.viewRequirements = builder.viewRequirements;
        this.clickRequirements = builder.clickRequirements;
        this.leftClickRequirements = builder.leftClickRequirements;
        this.rightClickRequirements = builder.rightClickRequirements;
        this.shiftLeftClickRequirements = builder.shiftLeftClickRequirements;
        this.shiftRightClickRequirements = builder.shiftRightClickRequirements;
        this.middleClickRequirements = builder.middleClickRequirements;
    }

    public static @NotNull MenuItemOptionsBuilder builder() {
        return new MenuItemOptionsBuilder();
    }

    public @NotNull String material() {
        return material;
    }

    public @NotNull Optional<String> damage() {
        return Optional.ofNullable(damage);
    }

    public int amount() {
        return amount;
    }

    public @NotNull Optional<String> customModelData() {
        return Optional.ofNullable(customModelData);
    }

    public @NotNull Optional<String> dynamicAmount() {
        return Optional.ofNullable(dynamicAmount);
    }

    public @NotNull Optional<String> lightLevel() {
        return Optional.ofNullable(lightLevel);
    }

    public @NotNull Optional<String> displayName() {
        return Optional.ofNullable(displayName);
    }

    public @NotNull List<String> lore() {
        return lore;
    }

    public @NotNull Optional<DyeColor> baseColor() {
        return Optional.ofNullable(baseColor);
    }

    public void headType(final @Nullable HeadType headType) {
        this.headType = headType;
    }

    public @NotNull Optional<HeadType> headType() {
        return Optional.ofNullable(headType);
    }

    public @NotNull Optional<String> rgb() {
        return Optional.ofNullable(rgb);
    }

    public @NotNull Optional<String> trimMaterial() {
        return Optional.ofNullable(trimMaterial);
    }

    public @NotNull Optional<String> trimPattern() {
        return Optional.ofNullable(trimPattern);
    }

    public @NotNull Optional<String> hideTooltip() {
        return Optional.ofNullable(hideTooltip);
    }

    public @NotNull Optional<String> enchantmentGlintOverride() {
        return Optional.ofNullable(enchantmentGlintOverride);
    }

    public @NotNull Optional<String> rarity() {
        return Optional.ofNullable(rarity);
    }

    public @NotNull Optional<String> tooltipStyle() {
        return Optional.ofNullable(tooltipStyle);
    }

    public @NotNull Optional<String> itemModel() {
        return Optional.ofNullable(itemModel);
    }

    public @NotNull Map<Enchantment, Integer> enchantments() {
        return enchantments;
    }

    public @NotNull List<PotionEffect> potionEffects() {
        return potionEffects;
    }

    public @NotNull List<Pattern> bannerMeta() {
        return bannerMeta;
    }

    public @NotNull Set<ItemFlag> itemFlags() {
        return itemFlags;
    }

    public boolean unbreakable() {
        return unbreakable;
    }

    public boolean displayNameHasPlaceholders() {
        return displayNameHasPlaceholders;
    }

    public boolean loreHasPlaceholders() {
        return loreHasPlaceholders;
    }

    public boolean hasLore() {
        return hasLore;
    }

    public @NotNull Optional<LoreAppendMode> loreAppendMode() {
        return Optional.ofNullable(loreAppendMode);
    }

    public @NotNull Optional<String> nbtString() {
        return Optional.ofNullable(nbtString);
    }

    public @NotNull Optional<String> nbtByte() {
        return Optional.ofNullable(nbtByte);
    }

    public @NotNull Optional<String> nbtShort() {
        return Optional.ofNullable(nbtShort);
    }

    public @NotNull Optional<String> nbtInt() {
        return Optional.ofNullable(nbtInt);
    }

    public @NotNull List<String> nbtStrings() {
        return nbtStrings;
    }

    public @NotNull List<String> nbtBytes() {
        return nbtBytes;
    }

    public @NotNull List<String> nbtShorts() {
        return nbtShorts;
    }

    public @NotNull List<String> nbtInts() {
        return nbtInts;
    }

    public int slot() {
        return slot;
    }

    public int priority() {
        return priority;
    }

    public boolean updatePlaceholders() {
        return updatePlaceholders;
    }

    public @NotNull Optional<ClickHandler> clickHandler() {
        return Optional.ofNullable(clickHandler);
    }

    public @NotNull Optional<ClickHandler> leftClickHandler() {
        return Optional.ofNullable(leftClickHandler);
    }

    public @NotNull Optional<ClickHandler> rightClickHandler() {
        return Optional.ofNullable(rightClickHandler);
    }

    public @NotNull Optional<ClickHandler> shiftLeftClickHandler() {
        return Optional.ofNullable(shiftLeftClickHandler);
    }

    public @NotNull Optional<ClickHandler> shiftRightClickHandler() {
        return Optional.ofNullable(shiftRightClickHandler);
    }

    public @NotNull Optional<ClickHandler> middleClickHandler() {
        return Optional.ofNullable(middleClickHandler);
    }

    public @NotNull Optional<RequirementList> viewRequirements() {
        return Optional.ofNullable(viewRequirements);
    }

    public @NotNull Optional<RequirementList> clickRequirements() {
        return Optional.ofNullable(clickRequirements);
    }

    public @NotNull Optional<RequirementList> leftClickRequirements() {
        return Optional.ofNullable(leftClickRequirements);
    }

    public @NotNull Optional<RequirementList> rightClickRequirements() {
        return Optional.ofNullable(rightClickRequirements);
    }

    public @NotNull Optional<RequirementList> shiftLeftClickRequirements() {
        return Optional.ofNullable(shiftLeftClickRequirements);
    }

    public @NotNull Optional<RequirementList> shiftRightClickRequirements() {
        return Optional.ofNullable(shiftRightClickRequirements);
    }

    public @NotNull Optional<RequirementList> middleClickRequirements() {
        return Optional.ofNullable(middleClickRequirements);
    }

    public @NotNull MenuItemOptionsBuilder asBuilder() {
        return MenuItemOptions.builder()
                .material(this.material)
                .damage(this.damage)
                .amount(this.amount)
                .customModelData(this.customModelData)
                .dynamicAmount(this.dynamicAmount)
                .lightLevel(this.lightLevel)
                .displayName(this.displayName)
                .lore(this.lore)
                .hasLore(this.hasLore)
                .loreAppendMode(this.loreAppendMode)
                .baseColor(this.baseColor)
                .headType(this.headType)
                .rgb(this.rgb)
                .trimMaterial(this.trimMaterial)
                .trimPattern(this.trimPattern)
                .hideTooltip(this.hideTooltip)
                .enchantmentGlintOverride(this.enchantmentGlintOverride)
                .rarity(this.rarity)
                .tooltipStyle(this.tooltipStyle)
                .itemModel(this.itemModel)
                .enchantments(this.enchantments)
                .potionEffects(this.potionEffects)
                .bannerMeta(this.bannerMeta)
                .itemFlags(this.itemFlags)
                .unbreakable(this.unbreakable)
                .nbtString(this.nbtString)
                .nbtByte(this.nbtByte)
                .nbtShort(this.nbtShort)
                .nbtInt(this.nbtInt)
                .nbtStrings(this.nbtStrings)
                .nbtBytes(this.nbtBytes)
                .nbtShorts(this.nbtShorts)
                .nbtInts(this.nbtInts)
                .slot(this.slot)
                .priority(this.priority)
                .updatePlaceholders(this.updatePlaceholders)
                .clickHandler(this.clickHandler)
                .leftClickHandler(this.leftClickHandler)
                .rightClickHandler(this.rightClickHandler)
                .shiftLeftClickHandler(this.shiftLeftClickHandler)
                .shiftRightClickHandler(this.shiftRightClickHandler)
                .middleClickHandler(this.middleClickHandler)
                .viewRequirements(this.viewRequirements)
                .clickRequirements(this.clickRequirements)
                .leftClickRequirements(this.leftClickRequirements)
                .rightClickRequirements(this.rightClickRequirements)
                .shiftLeftClickRequirements(this.shiftLeftClickRequirements)
                .shiftRightClickRequirements(this.shiftRightClickRequirements)
                .middleClickRequirements(this.middleClickRequirements);
    }

    public static class MenuItemOptionsBuilder {

        private String material;
        private String damage;
        private int amount;
        private String customModelData;
        private String dynamicAmount;
        private String lightLevel;
        private String displayName;
        private List<String> lore = Collections.emptyList();
        private DyeColor baseColor;
        private HeadType headType;
        private String rgb;

        private String trimMaterial;
        private String trimPattern;

        private String hideTooltip;
        private String enchantmentGlintOverride;
        private String rarity;
        private String tooltipStyle;
        private String itemModel;

        private Map<Enchantment, Integer> enchantments = Collections.emptyMap();
        private List<PotionEffect> potionEffects = Collections.emptyList();
        private List<Pattern> bannerMeta = Collections.emptyList();
        private final Set<ItemFlag> itemFlags = new HashSet<>();

        private boolean unbreakable;

        private boolean displayNameHasPlaceholders;
        private boolean loreHasPlaceholders;
        private boolean hasLore;
        private LoreAppendMode loreAppendMode;

        private String nbtString;
        private String nbtByte;
        private String nbtShort;
        private String nbtInt;
        private List<String> nbtStrings = Collections.emptyList();
        private List<String> nbtBytes = Collections.emptyList();
        private List<String> nbtShorts = Collections.emptyList();
        private List<String> nbtInts = Collections.emptyList();

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

        private MenuItemOptionsBuilder() {
        }

        public MenuItemOptionsBuilder material(final @NotNull String configMaterial) {
            this.material = configMaterial;
            return this;
        }

        public MenuItemOptionsBuilder damage(final @Nullable String configDamage) {
            this.damage = configDamage;
            return this;
        }

        public MenuItemOptionsBuilder amount(final int configAmount) {
            this.amount = configAmount;
            return this;
        }

        public MenuItemOptionsBuilder customModelData(final @Nullable String customModelData) {
            this.customModelData = customModelData;
            return this;
        }

        public MenuItemOptionsBuilder dynamicAmount(final @Nullable String configDynamicAmount) {
            this.dynamicAmount = configDynamicAmount;
            return this;
        }

        public MenuItemOptionsBuilder lightLevel(final @Nullable String lightLevel) {
            this.lightLevel = lightLevel;
            return this;
        }

        public MenuItemOptionsBuilder displayName(final @Nullable String configDisplayName) {
            this.displayName = configDisplayName;
            if (this.displayName != null) {
                this.displayNameHasPlaceholders = DeluxeMenusConfig.containsPlaceholders(this.displayName);
            }
            return this;
        }

        public MenuItemOptionsBuilder lore(final @NotNull List<String> configLore) {
            this.lore = configLore;
            this.loreHasPlaceholders = configLore.stream().anyMatch(DeluxeMenusConfig::containsPlaceholders);
            return this;
        }

        public MenuItemOptionsBuilder baseColor(final @Nullable DyeColor configBaseColor) {
            this.baseColor = configBaseColor;
            return this;
        }

        public MenuItemOptionsBuilder headType(final @Nullable HeadType headType) {
            this.headType = headType;
            return this;
        }

        public MenuItemOptionsBuilder rgb(final @Nullable String rgb) {
            this.rgb = rgb;
            return this;
        }

        public MenuItemOptionsBuilder trimMaterial(final @Nullable String trimMaterial) {
            this.trimMaterial = trimMaterial;
            return this;
        }

        public MenuItemOptionsBuilder trimPattern(final @Nullable String trimPattern) {
            this.trimPattern = trimPattern;
            return this;
        }

        public MenuItemOptionsBuilder hideTooltip(final @Nullable String hideTooltip) {
            this.hideTooltip = hideTooltip;
            return this;
        }

        public MenuItemOptionsBuilder enchantmentGlintOverride(final @Nullable String enchantmentGlintOverride) {
            this.enchantmentGlintOverride = enchantmentGlintOverride;
            return this;
        }

        public MenuItemOptionsBuilder rarity(final @Nullable String rarity) {
            this.rarity = rarity;
            return this;
        }

        public MenuItemOptionsBuilder tooltipStyle(final @Nullable String tooltipStyle) {
            this.tooltipStyle = tooltipStyle;
            return this;
        }

        public MenuItemOptionsBuilder itemModel(final @Nullable String itemModel) {
            this.itemModel = itemModel;
            return this;
        }

        public MenuItemOptionsBuilder enchantments(final @NotNull Map<Enchantment, Integer> enchantments) {
            this.enchantments = enchantments;
            return this;
        }

        public MenuItemOptionsBuilder potionEffects(final @NotNull List<PotionEffect> potionEffects) {
            this.potionEffects = potionEffects;
            return this;
        }

        public MenuItemOptionsBuilder bannerMeta(final @NotNull List<Pattern> bannerMeta) {
            this.bannerMeta = bannerMeta;
            return this;
        }

        public MenuItemOptionsBuilder itemFlags(final @NotNull Collection<ItemFlag> itemFlags) {
            this.itemFlags.addAll(itemFlags);
            return this;
        }

        public MenuItemOptionsBuilder unbreakable(final boolean unbreakable) {
            this.unbreakable = unbreakable;
            return this;
        }

        /**
         * @deprecated Use {@link #itemFlags(Collection)} with {@link ItemFlag#HIDE_ATTRIBUTES}
         */
        @Deprecated()
        public MenuItemOptionsBuilder hideAttributes(final boolean hideAttributes) {
            if (hideAttributes) {
                this.itemFlags.add(ItemFlag.HIDE_ATTRIBUTES);
            }
            return this;
        }

        /**
         * @deprecated Use {@link #itemFlags(Collection)} with {@link ItemFlag#HIDE_ENCHANTS}
         */
        @Deprecated
        public MenuItemOptionsBuilder hideEnchants(final boolean hideEnchants) {
            if (hideEnchants) {
                this.itemFlags.add(ItemFlag.HIDE_ENCHANTS);
            }
            return this;
        }

        /**
         * @deprecated Use {@link #itemFlags(Collection)} with {@link ItemFlag#HIDE_UNBREAKABLE}
         */
        @Deprecated
        public MenuItemOptionsBuilder hideUnbreakable(final boolean hideUnbreakable) {
            if (hideUnbreakable) {
                this.itemFlags.add(ItemFlag.HIDE_UNBREAKABLE);
            }
            return this;
        }

        public MenuItemOptionsBuilder nbtString(final @Nullable String nbtString) {
            this.nbtString = nbtString;
            return this;
        }

        public MenuItemOptionsBuilder nbtByte(final @Nullable String nbtByte) {
            this.nbtByte = nbtByte;
            return this;
        }

        public MenuItemOptionsBuilder nbtShort(final @Nullable String nbtShort) {
            this.nbtShort = nbtShort;
            return this;
        }

        public MenuItemOptionsBuilder nbtInt(final @Nullable String nbtInt) {
            this.nbtInt = nbtInt;
            return this;
        }

        public MenuItemOptionsBuilder nbtStrings(final @NotNull List<String> nbtStrings) {
            this.nbtStrings = nbtStrings;
            return this;
        }

        public MenuItemOptionsBuilder nbtBytes(final @NotNull List<String> nbtBytes) {
            this.nbtBytes = nbtBytes;
            return this;
        }

        public MenuItemOptionsBuilder nbtShorts(final @NotNull List<String> nbtShorts) {
            this.nbtShorts = nbtShorts;
            return this;
        }

        public MenuItemOptionsBuilder nbtInts(final @NotNull List<String> nbtInts) {
            this.nbtInts = nbtInts;
            return this;
        }

        public MenuItemOptionsBuilder slot(final int slot) {
            this.slot = slot;
            return this;
        }

        public MenuItemOptionsBuilder priority(final int priority) {
            this.priority = priority;
            return this;
        }

        public MenuItemOptionsBuilder updatePlaceholders(final boolean updatePlaceholders) {
            this.updatePlaceholders = updatePlaceholders;
            return this;
        }

        public MenuItemOptionsBuilder hasLore(final boolean hasLore) {
            this.hasLore = hasLore;
            return this;
        }

        public MenuItemOptionsBuilder loreAppendMode(final LoreAppendMode loreAppendMode) {
            this.loreAppendMode = loreAppendMode;
            return this;
        }

        public MenuItemOptionsBuilder clickHandler(final @Nullable ClickHandler clickHandler) {
            this.clickHandler = clickHandler;
            return this;
        }

        public MenuItemOptionsBuilder leftClickHandler(final @Nullable ClickHandler leftClickHandler) {
            this.leftClickHandler = leftClickHandler;
            return this;
        }

        public MenuItemOptionsBuilder rightClickHandler(final @Nullable ClickHandler rightClickHandler) {
            this.rightClickHandler = rightClickHandler;
            return this;
        }

        public MenuItemOptionsBuilder shiftLeftClickHandler(final @Nullable ClickHandler shiftLeftClickHandler) {
            this.shiftLeftClickHandler = shiftLeftClickHandler;
            return this;
        }

        public MenuItemOptionsBuilder shiftRightClickHandler(final @Nullable ClickHandler shiftRightClickHandler) {
            this.shiftRightClickHandler = shiftRightClickHandler;
            return this;
        }

        public MenuItemOptionsBuilder middleClickHandler(final @Nullable ClickHandler middleClickHandler) {
            this.middleClickHandler = middleClickHandler;
            return this;
        }

        public MenuItemOptionsBuilder viewRequirements(final @Nullable RequirementList viewRequirements) {
            this.viewRequirements = viewRequirements;
            return this;
        }

        public MenuItemOptionsBuilder clickRequirements(final @Nullable RequirementList clickRequirements) {
            this.clickRequirements = clickRequirements;
            return this;
        }

        public MenuItemOptionsBuilder leftClickRequirements(final @Nullable RequirementList leftClickRequirements) {
            this.leftClickRequirements = leftClickRequirements;
            return this;
        }

        public MenuItemOptionsBuilder rightClickRequirements(final @Nullable RequirementList rightClickRequirements) {
            this.rightClickRequirements = rightClickRequirements;
            return this;
        }

        public MenuItemOptionsBuilder shiftLeftClickRequirements(final @Nullable RequirementList shiftLeftClickRequirements) {
            this.shiftLeftClickRequirements = shiftLeftClickRequirements;
            return this;
        }

        public MenuItemOptionsBuilder shiftRightClickRequirements(final @Nullable RequirementList shiftRightClickRequirements) {
            this.shiftRightClickRequirements = shiftRightClickRequirements;
            return this;
        }

        public MenuItemOptionsBuilder middleClickRequirements(final @Nullable RequirementList middleClickRequirements) {
            this.middleClickRequirements = middleClickRequirements;
            return this;
        }

        public MenuItemOptions build() {
            return new MenuItemOptions(this);
        }
    }
}
