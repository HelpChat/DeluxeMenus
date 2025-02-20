package com.extendedclip.deluxemenus.menu;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.hooks.ItemHook;
import com.extendedclip.deluxemenus.menu.options.HeadType;
import com.extendedclip.deluxemenus.menu.options.LoreAppendMode;
import com.extendedclip.deluxemenus.menu.options.MenuItemOptions;
import com.extendedclip.deluxemenus.nbt.NbtProvider;
import com.extendedclip.deluxemenus.utils.DebugLevel;
import com.extendedclip.deluxemenus.utils.ItemUtils;
import com.extendedclip.deluxemenus.utils.StringUtils;
import com.extendedclip.deluxemenus.utils.VersionHelper;
import com.google.common.collect.ImmutableMultimap;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Banner;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Light;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.extendedclip.deluxemenus.utils.Constants.INVENTORY_ITEM_ACCESSORS;
import static com.extendedclip.deluxemenus.utils.Constants.PLACEHOLDER_PREFIX;

public class MenuItem {

    private final DeluxeMenus plugin;
    private final MenuItemOptions options;

    public MenuItem(@NotNull final DeluxeMenus plugin, @NotNull final MenuItemOptions options) {
        this.plugin = plugin;
        this.options = options;
    }

    public ItemStack getItemStack(@NotNull final MenuHolder holder) {
        final Player viewer = holder.getViewer();

        ItemStack itemStack = null;
        int amount = 1;

        String stringMaterial = this.options.material();
        String lowercaseStringMaterial = stringMaterial.toLowerCase(Locale.ROOT);

        if (ItemUtils.isPlaceholderOption(lowercaseStringMaterial)) {
            stringMaterial = holder.setPlaceholdersAndArguments(stringMaterial.substring(PLACEHOLDER_PREFIX.length()));
            lowercaseStringMaterial = stringMaterial.toLowerCase(Locale.ENGLISH);
        }

        if (ItemUtils.isPlayerItem(lowercaseStringMaterial)) {
            final ItemStack playerItem = INVENTORY_ITEM_ACCESSORS.get(lowercaseStringMaterial).apply(viewer.getInventory());

            if (playerItem == null || playerItem.getType() == Material.AIR) {
                return new ItemStack(Material.AIR);
            }

            itemStack = playerItem.clone();
            amount = playerItem.getAmount();
        }

        final int temporaryAmount = amount;

        final String finalMaterial = lowercaseStringMaterial;
        final ItemHook pluginHook = plugin.getItemHooks().values()
            .stream()
            .filter(x -> finalMaterial.startsWith(x.getPrefix()))
            .findFirst()
            .orElse(null);

        if (pluginHook != null) {
            itemStack = pluginHook.getItem(
                    viewer,
                    holder.setPlaceholdersAndArguments(stringMaterial.substring(pluginHook.getPrefix().length()))
            );
        }

        if (ItemUtils.isWaterBottle(stringMaterial)) {
            itemStack = ItemUtils.createWaterBottles(amount);
        }

        // The item is neither a water bottle nor plugin hook item
        if (itemStack == null) {
            final Material material = Material.getMaterial(stringMaterial.toUpperCase(Locale.ROOT));
            if (material == null) {
                plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Material: " + stringMaterial + " is not valid! Setting to Stone."
                );
                itemStack = new ItemStack(Material.STONE, temporaryAmount);
            } else {
                itemStack = new ItemStack(material, temporaryAmount);
            }
        }

        if (ItemUtils.isBanner(itemStack.getType())) {
            final BannerMeta meta = (BannerMeta) itemStack.getItemMeta();
            if (meta != null) {
                if (!this.options.bannerMeta().isEmpty()) {
                    meta.setPatterns(this.options.bannerMeta());
                }
                itemStack.setItemMeta(meta);
            }
        }

        if (ItemUtils.isShield(itemStack.getType())) {
            final BlockStateMeta blockStateMeta = (BlockStateMeta) itemStack.getItemMeta();

            if (blockStateMeta != null) {
                final Banner banner = (Banner) blockStateMeta.getBlockState();
                if (this.options.baseColor().isPresent()) {
                    banner.setBaseColor(this.options.baseColor().get());
                    banner.update();
                    blockStateMeta.setBlockState(banner);
                }
                if (!this.options.bannerMeta().isEmpty()) {
                    banner.setPatterns(this.options.bannerMeta());
                    banner.update();
                    blockStateMeta.setBlockState(banner);
                }

                itemStack.setItemMeta(blockStateMeta);
            }
        }

        if (ItemUtils.hasPotionMeta(itemStack)) {
            final PotionMeta meta = (PotionMeta) itemStack.getItemMeta();

            if (meta != null) {
                if (this.options.rgb().isPresent()) {
                    final String rgbString = holder.setPlaceholdersAndArguments(this.options.rgb().get());
                    final String[] parts = rgbString.split(",");

                    try {
                        meta.setColor(Color.fromRGB(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()),
                                Integer.parseInt(parts[2].trim())));
                    } catch (Exception ignored) {
                    }
                }

                if (!this.options.potionEffects().isEmpty()) {
                    for (PotionEffect effect : this.options.potionEffects()) {
                        meta.addCustomEffect(effect, true);
                    }
                }

                itemStack.setItemMeta(meta);
            }
        }

        if (itemStack.getType() == Material.AIR) {
            return itemStack;
        }

        if (this.options.damage().isPresent()) {
            final String parsedDamage = holder.setPlaceholdersAndArguments(this.options.damage().get());
            try {
                int damage = Integer.parseInt(parsedDamage);
                if (damage > 0) {
                    final ItemMeta meta = itemStack.getItemMeta();
                    if (meta instanceof Damageable) {
                        ((Damageable) meta).setDamage(damage);
                        itemStack.setItemMeta(meta);
                    }
                }
            } catch (final NumberFormatException exception) {
                plugin.printStacktrace(
                        "Invalid damage found: " + parsedDamage + ".",
                        exception
                );
            }
        }

        if (this.options.amount() != -1) {
            amount = this.options.amount();
        }

        if (this.options.dynamicAmount().isPresent()) {
            try {
                final int dynamicAmount = (int) Double.parseDouble(holder.setPlaceholdersAndArguments(this.options.dynamicAmount().get()));
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

        if (this.options.customModelData().isPresent() && VersionHelper.IS_CUSTOM_MODEL_DATA) {
            try {
                final int modelData = Integer.parseInt(holder.setPlaceholdersAndArguments(this.options.customModelData().get()));
                itemMeta.setCustomModelData(modelData);
            } catch (final Exception ignored) {
            }
        }

        if (this.options.displayName().isPresent()) {
            final String displayName = holder.setPlaceholdersAndArguments(this.options.displayName().get());
            itemMeta.setDisplayName(StringUtils.color(displayName));
        }

        List<String> lore = new ArrayList<>();
        // This checks if a lore should be kept from the hooked item, and then if a lore exists on the item
        // ItemMeta.getLore is nullable. In that case, we just create a new ArrayList so we don't add stuff to a null list.
        List<String> itemLore = Objects.requireNonNullElse(itemMeta.getLore(), new ArrayList<>());
        // Ensures backwards compatibility with how hooked items are currently handled
        LoreAppendMode mode = this.options.loreAppendMode().orElse(LoreAppendMode.OVERRIDE);
        if (!this.options.hasLore() && this.options.loreAppendMode().isEmpty()) mode = LoreAppendMode.IGNORE;
        switch (mode) {
            case IGNORE: // DM lore is not added at all
                lore.addAll(itemLore);
                break;
            case TOP: // DM lore is added at the top
                lore.addAll(getMenuItemLore(holder, this.options.lore()));
                lore.addAll(itemLore);
                break;
            case BOTTOM: // DM lore is bottom at the bottom
                lore.addAll(itemLore);
                lore.addAll(getMenuItemLore(holder, this.options.lore()));
                break;
            case OVERRIDE: // Lore from DM overrides the lore from the item
                lore.addAll(getMenuItemLore(holder, this.options.lore()));
                break;
        }

        itemMeta.setLore(lore);

        if (this.options.unbreakable()) {
            itemMeta.setUnbreakable(true);
        }

        if (VersionHelper.HAS_DATA_COMPONENTS) {
            if (this.options.hideTooltip().isPresent()) {
                String hideTooltip = holder.setPlaceholdersAndArguments(this.options.hideTooltip().get());
                itemMeta.setHideTooltip(Boolean.parseBoolean(hideTooltip));
            }
            if (this.options.enchantmentGlintOverride().isPresent()) {
                String enchantmentGlintOverride = holder.setPlaceholdersAndArguments(this.options.enchantmentGlintOverride().get());
                itemMeta.setEnchantmentGlintOverride(Boolean.parseBoolean(enchantmentGlintOverride));
            }
            if (this.options.rarity().isPresent()) {
                String rarity = holder.setPlaceholdersAndArguments(this.options.rarity().get());
                try {
                    itemMeta.setRarity(ItemRarity.valueOf(rarity.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Rarity " + rarity + " is not a valid!"
                    );
                }
            }
        }
        if (VersionHelper.HAS_TOOLTIP_STYLE) {
            if (this.options.tooltipStyle().isPresent()) {
                NamespacedKey tooltipStyle = NamespacedKey.fromString(holder.setPlaceholdersAndArguments(this.options.tooltipStyle().get()));
                if (tooltipStyle != null) itemMeta.setTooltipStyle(tooltipStyle);
            }
            if (this.options.itemModel().isPresent()) {
                NamespacedKey itemModel = NamespacedKey.fromString(holder.setPlaceholdersAndArguments(this.options.itemModel().get()));
                if (itemModel != null) itemMeta.setItemModel(itemModel);
            }
        }

        if (VersionHelper.HAS_ARMOR_TRIMS && ItemUtils.hasArmorMeta(itemStack)) {
            final Optional<String> trimMaterialName = this.options.trimMaterial();
            final Optional<String> trimPatternName = this.options.trimPattern();

            if (trimMaterialName.isPresent() && trimPatternName.isPresent()) {
                final TrimMaterial trimMaterial = Registry.TRIM_MATERIAL.match(holder.setPlaceholdersAndArguments(trimMaterialName.get()));
                final TrimPattern trimPattern = Registry.TRIM_PATTERN.match(holder.setPlaceholdersAndArguments(trimPatternName.get()));

                if (trimMaterial != null && trimPattern != null) {
                    final ArmorTrim armorTrim = new ArmorTrim(trimMaterial, trimPattern);
                    final ArmorMeta armorMeta = (ArmorMeta) itemMeta;
                    armorMeta.setTrim(armorTrim);
                    itemStack.setItemMeta(armorMeta);
                } else {
                    if (trimMaterial == null) {
                        plugin.debug(
                                DebugLevel.HIGHEST,
                                Level.WARNING,
                                "Trim material " + trimMaterialName.get() + " is not a valid!"
                        );
                    }

                    if (trimPattern == null) {
                        plugin.debug(
                                DebugLevel.HIGHEST,
                                Level.WARNING,
                                "Trim pattern " + trimPatternName.get() + " is not a valid!"
                        );
                    }
                }
            } else if (trimMaterialName.isPresent()) {
                plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Trim pattern is not set for item with trim material " + trimMaterialName.get()
                );
            } else if (trimPatternName.isPresent()) {
                plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Trim material is not set for item with trim pattern " + trimPatternName.get()
                );
            }
        }

        if (itemMeta instanceof LeatherArmorMeta && this.options.rgb().isPresent()) {
            final String rgbString = holder.setPlaceholdersAndArguments(this.options.rgb().get());
            final String[] parts = rgbString.split(",");
            final LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemMeta;

            try {
                leatherArmorMeta.setColor(Color.fromRGB(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()),
                        Integer.parseInt(parts[2].trim())));
                itemStack.setItemMeta(leatherArmorMeta);
            } catch (final Exception exception) {
                plugin.printStacktrace(
                        "Invalid rgb colors found for leather armor: " + parts[0].trim() + ", " + parts[1].trim() + ", " +
                                parts[2].trim(),
                        exception
                );
            }
        } else if (itemMeta instanceof FireworkEffectMeta && this.options.rgb().isPresent()) {
            final String rgbString = holder.setPlaceholdersAndArguments(this.options.rgb().get());
            final String[] parts = rgbString.split(",");
            final FireworkEffectMeta fireworkEffectMeta = (FireworkEffectMeta) itemMeta;

            try {
                fireworkEffectMeta.setEffect(FireworkEffect.builder().withColor(Color.fromRGB(Integer.parseInt(parts[0].trim()),
                        Integer.parseInt(parts[1].trim()), Integer.parseInt(parts[2].trim()))).build());
                itemStack.setItemMeta(fireworkEffectMeta);
            } catch (final Exception exception) {
                plugin.printStacktrace(
                        "Invalid rgb colors found for firework or firework star: " + parts[0].trim() + ", "
                                + parts[1].trim() + ", " + parts[2].trim(),
                        exception
                );
            }
        } else if (itemMeta instanceof EnchantmentStorageMeta && !this.options.enchantments().isEmpty()) {
            final EnchantmentStorageMeta enchantmentStorageMeta = (EnchantmentStorageMeta) itemMeta;
            for (final Map.Entry<Enchantment, Integer> entry : this.options.enchantments().entrySet()) {
                final boolean result = enchantmentStorageMeta.addStoredEnchant(entry.getKey(), entry.getValue(), true);
                if (!result) {
                    plugin.debug(
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

        if (!(itemMeta instanceof EnchantmentStorageMeta) && !this.options.enchantments().isEmpty()) {
            this.options.enchantments().forEach((enchantment, level) -> itemMeta.addEnchant(enchantment, level, true));
        }

        if (this.options.lightLevel().isPresent() && itemMeta instanceof BlockDataMeta) {
            final BlockDataMeta blockDataMeta = (BlockDataMeta) itemStack.getItemMeta();
            final BlockData blockData = blockDataMeta.getBlockData(itemStack.getType());
            if (blockData instanceof Light) {
                final Light light = (Light) blockData;
                final String parsedLightLevel = holder.setPlaceholdersAndArguments(this.options.lightLevel().get());
                try {
                    final int lightLevel = Math.min(Integer.parseInt(parsedLightLevel), light.getMaximumLevel());
                    light.setLevel(Math.max(lightLevel, 0));
                    if (lightLevel < 0) {
                        plugin.debug(
                                DebugLevel.MEDIUM,
                                Level.WARNING,
                                "Invalid light level found for light block: " + parsedLightLevel + ". Setting to 0."
                        );
                    }
                    if (lightLevel > light.getMaximumLevel()) {
                        plugin.debug(
                                DebugLevel.MEDIUM,
                                Level.WARNING,
                                "Invalid light level found for light block: " + parsedLightLevel + ". Setting to " + light.getMaximumLevel() + "."
                        );
                    }

                    blockDataMeta.setBlockData(light);
                    itemStack.setItemMeta(blockDataMeta);
                } catch (final Exception exception) {
                    plugin.printStacktrace(
                            "Invalid light level found for light block: " + parsedLightLevel,
                            exception
                    );
                }
            }
        }

        if (!this.options.itemFlags().isEmpty()) {
            for (final ItemFlag flag : this.options.itemFlags()) {
                itemMeta.addItemFlags(flag);

                if (flag == ItemFlag.HIDE_ATTRIBUTES && VersionHelper.HAS_DATA_COMPONENTS) {
                    itemMeta.setAttributeModifiers(ImmutableMultimap.of());
                }
            }
        }

        itemStack.setItemMeta(itemMeta);

        if (NbtProvider.isAvailable()) {
            if (this.options.nbtString().isPresent()) {
                final String tag = holder.setPlaceholdersAndArguments(this.options.nbtString().get());
                if (tag.contains(":")) {
                    final String[] parts = tag.split(":", 2);
                    itemStack = NbtProvider.setString(itemStack, parts[0], parts[1]);
                }
            }

            if (this.options.nbtByte().isPresent()) {
                final String tag = holder.setPlaceholdersAndArguments(this.options.nbtByte().get());
                if (tag.contains(":")) {
                    final String[] parts = tag.split(":");
                    itemStack = NbtProvider.setByte(itemStack, parts[0], Byte.parseByte(parts[1]));
                }
            }

            if (this.options.nbtShort().isPresent()) {
                final String tag = holder.setPlaceholdersAndArguments(this.options.nbtShort().get());
                if (tag.contains(":")) {
                    final String[] parts = tag.split(":");
                    itemStack = NbtProvider.setShort(itemStack, parts[0], Short.parseShort(parts[1]));
                }
            }

            if (this.options.nbtInt().isPresent()) {
                final String tag = holder.setPlaceholdersAndArguments(this.options.nbtInt().get());
                if (tag.contains(":")) {
                    final String[] parts = tag.split(":");
                    itemStack = NbtProvider.setInt(itemStack, parts[0], Integer.parseInt(parts[1]));
                }
            }

            for (String nbtTag : this.options.nbtStrings()) {
                final String tag = holder.setPlaceholdersAndArguments(nbtTag);
                if (tag.contains(":")) {
                    final String[] parts = tag.split(":", 2);
                    itemStack = NbtProvider.setString(itemStack, parts[0], parts[1]);
                }
            }

            for (String nbtTag : this.options.nbtBytes()) {
                final String tag = holder.setPlaceholdersAndArguments(nbtTag);
                if (tag.contains(":")) {
                    final String[] parts = tag.split(":");
                    itemStack = NbtProvider.setByte(itemStack, parts[0], Byte.parseByte(parts[1]));
                }
            }

            for (String nbtTag : this.options.nbtShorts()) {
                final String tag = holder.setPlaceholdersAndArguments(nbtTag);
                if (tag.contains(":")) {
                    final String[] parts = tag.split(":");
                    itemStack = NbtProvider.setShort(itemStack, parts[0], Short.parseShort(parts[1]));
                }
            }

            for (String nbtTag : this.options.nbtInts()) {
                final String tag = holder.setPlaceholdersAndArguments(nbtTag);
                if (tag.contains(":")) {
                    final String[] parts = tag.split(":");
                    itemStack = NbtProvider.setInt(itemStack, parts[0], Integer.parseInt(parts[1]));
                }
            }
        }

        return itemStack;
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
        headType.ifPresent(this.options::headType);
        return headType.isPresent();
    }

    private @NotNull Optional<ItemStack> getItemFromHook(String hookName, String... args) {
        return plugin.getItemHook(hookName).map(itemHook -> itemHook.getItem(args));
    }

    private List<String> getMenuItemLore(@NotNull final MenuHolder holder, @NotNull final List<String> lore) {
        return lore.stream()
                .map(holder::setPlaceholdersAndArguments)
                .map(StringUtils::color)
                .map(line -> line.split("\n"))
                .flatMap(Arrays::stream)
                .map(line -> line.split("\\\\n"))
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());
    }

    public @NotNull MenuItemOptions options() {
        return options;
    }
}
