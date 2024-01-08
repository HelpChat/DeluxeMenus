package com.extendedclip.deluxemenus.menu;

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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MenuItemOptions {

    private final String material;
    private final short data;
    private final int amount;
    private final String customModelData;
    private final String dynamicAmount;
    private final String displayName;
    private final List<String> lore;
    private final DyeColor baseColor;
    private HeadType headType;
    private final String placeholderData;
    private final String rgb;

    private final Map<Enchantment, Integer> enchantments;
    private final List<PotionEffect> potionEffects;
    private final List<Pattern> bannerMeta;
    private final List<ItemFlag> itemFlags;

    private final boolean unbreakable;
    private final boolean hideAttributes;
    private final boolean hideEnchants;
    private final boolean hidePotionEffects;
    private final boolean hideUnbreakable;

    private final boolean displayNameHasPlaceholders;
    private final boolean loreHasPlaceholders;

    private final String nbtString;
    private final String nbtInt;
    private final List<String> nbtStrings;
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
        this.data = builder.data;
        this.amount = builder.amount;
        this.customModelData = builder.customModelData;
        this.dynamicAmount = builder.dynamicAmount;
        this.displayName = builder.displayName;
        this.lore = builder.lore;
        this.baseColor = builder.baseColor;
        this.headType = builder.headType;
        this.placeholderData = builder.placeholderData;
        this.rgb = builder.rgb;
        this.enchantments = builder.enchantments;
        this.potionEffects = builder.potionEffects;
        this.bannerMeta = builder.bannerMeta;
        this.itemFlags = builder.itemFlags;
        this.unbreakable = builder.unbreakable;
        this.hideAttributes = builder.hideAttributes;
        this.hideEnchants = builder.hideEnchants;
        this.hidePotionEffects = builder.hidePotionEffects;
        this.hideUnbreakable = builder.hideUnbreakable;
        this.displayNameHasPlaceholders = builder.displayNameHasPlaceholders;
        this.loreHasPlaceholders = builder.loreHasPlaceholders;
        this.nbtString = builder.nbtString;
        this.nbtInt = builder.nbtInt;
        this.nbtStrings = builder.nbtStrings;
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

    public short data() {
        return data;
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

    public @NotNull Optional<String> placeholderData() {
        return Optional.ofNullable(placeholderData);
    }

    public @NotNull Optional<String> rgb() {
        return Optional.ofNullable(rgb);
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

    public @NotNull List<ItemFlag> itemFlags() {
        return itemFlags;
    }

    public boolean unbreakable() {
        return unbreakable;
    }

    public boolean hideAttributes() {
        return hideAttributes;
    }

    public boolean hideEnchants() {
        return hideEnchants;
    }

    public boolean hidePotionEffects() {
        return hidePotionEffects;
    }

    public boolean hideUnbreakable() {
        return hideUnbreakable;
    }

    public boolean displayNameHasPlaceholders() {
        return displayNameHasPlaceholders;
    }

    public boolean loreHasPlaceholders() {
        return loreHasPlaceholders;
    }

    public @NotNull Optional<String> nbtString() {
        return Optional.ofNullable(nbtString);
    }

    public @NotNull Optional<String> nbtInt() {
        return Optional.ofNullable(nbtInt);
    }

    public @NotNull List<String> nbtStrings() {
        return nbtStrings;
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
                .data(this.data)
                .amount(this.amount)
                .customModelData(this.customModelData)
                .dynamicAmount(this.dynamicAmount)
                .displayName(this.displayName)
                .lore(this.lore)
                .baseColor(this.baseColor)
                .headType(this.headType)
                .placeholderData(this.placeholderData)
                .rgb(this.rgb)
                .enchantments(this.enchantments)
                .potionEffects(this.potionEffects)
                .bannerMeta(this.bannerMeta)
                .itemFlags(this.itemFlags)
                .unbreakable(this.unbreakable)
                .hideAttributes(this.hideAttributes)
                .hideEnchants(this.hideEnchants)
                .hidePotionEffects(this.hidePotionEffects)
                .hideUnbreakable(this.hideUnbreakable)
                .nbtString(this.nbtString)
                .nbtInt(this.nbtInt)
                .nbtStrings(this.nbtStrings)
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
        private short data;
        private int amount;
        private String customModelData;
        private String dynamicAmount;
        private String displayName;
        private List<String> lore = Collections.emptyList();
        private DyeColor baseColor;
        private HeadType headType;
        private String placeholderData;
        private String rgb;

        private Map<Enchantment, Integer> enchantments = Collections.emptyMap();
        private List<PotionEffect> potionEffects = Collections.emptyList();
        private List<Pattern> bannerMeta = Collections.emptyList();
        private List<ItemFlag> itemFlags = Collections.emptyList();

        private boolean unbreakable;
        private boolean hideAttributes;
        private boolean hideEnchants;
        private boolean hidePotionEffects;
        private boolean hideUnbreakable;

        private boolean displayNameHasPlaceholders;
        private boolean loreHasPlaceholders;

        private String nbtString;
        private String nbtInt;
        private List<String> nbtStrings = Collections.emptyList();
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

        public MenuItemOptionsBuilder data(final short configData) {
            this.data = configData;
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

        public MenuItemOptionsBuilder placeholderData(final @Nullable String placeholderData) {
            this.placeholderData = placeholderData;
            return this;
        }

        public MenuItemOptionsBuilder rgb(final @Nullable String rgb) {
            this.rgb = rgb;
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

        public MenuItemOptionsBuilder itemFlags(final @NotNull List<ItemFlag> itemFlags) {
            this.itemFlags = itemFlags;
            return this;
        }

        public MenuItemOptionsBuilder unbreakable(final boolean unbreakable) {
            this.unbreakable = unbreakable;
            return this;
        }

        public MenuItemOptionsBuilder hideAttributes(final boolean hideAttributes) {
            this.hideAttributes = hideAttributes;
            return this;
        }

        public MenuItemOptionsBuilder hideEnchants(final boolean hideEnchants) {
            this.hideEnchants = hideEnchants;
            return this;
        }

        public MenuItemOptionsBuilder hidePotionEffects(final boolean hidePotionEffects) {
            this.hidePotionEffects = hidePotionEffects;
            return this;
        }

        public MenuItemOptionsBuilder hideUnbreakable(final boolean hideUnbreakable) {
            this.hideUnbreakable = hideUnbreakable;
            return this;
        }

        public MenuItemOptionsBuilder nbtString(final @Nullable String nbtString) {
            this.nbtString = nbtString;
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
