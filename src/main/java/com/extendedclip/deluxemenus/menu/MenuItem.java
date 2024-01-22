package com.extendedclip.deluxemenus.menu;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.hooks.ItemHook;
import com.extendedclip.deluxemenus.nbt.NbtProvider;
import com.extendedclip.deluxemenus.utils.DebugLevel;
import com.extendedclip.deluxemenus.utils.ItemUtils;
import com.extendedclip.deluxemenus.utils.StringUtils;
import com.extendedclip.deluxemenus.utils.VersionHelper;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.extendedclip.deluxemenus.utils.Constants.INVENTORY_ITEM_ACCESSORS;
import static com.extendedclip.deluxemenus.utils.Constants.PLACEHOLDER_PREFIX;

public class MenuItem {

    private final @NotNull MenuItemOptions options;

    public MenuItem(@NotNull final MenuItemOptions options) {
        this.options = options;
    }

    public ItemStack getItemStack(@NotNull final MenuHolder holder) {
        final Player viewer = holder.getViewer();

        ItemStack itemStack = null;
        int amount = 1;

        String stringMaterial = this.options.material();
        String lowercaseStringMaterial = stringMaterial.toLowerCase(Locale.ROOT);

        if (ItemUtils.isPlaceholderMaterial(lowercaseStringMaterial)) {
            stringMaterial = holder.setPlaceholders(stringMaterial.substring(PLACEHOLDER_PREFIX.length()));
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
        final ItemHook pluginHook = DeluxeMenus.getInstance().getItemHooks().values()
            .stream()
            .filter(x -> finalMaterial.startsWith(x.getPrefix()))
            .findFirst()
            .orElse(null);

        if (pluginHook != null) {
            itemStack = pluginHook.getItem(stringMaterial.substring(pluginHook.getPrefix().length()));
        }

        if (ItemUtils.isWaterBottle(stringMaterial)) {
            itemStack = ItemUtils.createWaterBottles(amount);
        }

        // The item is neither a water bottle nor plugin hook item
        if (itemStack == null) {
            final Material material = Material.getMaterial(stringMaterial.toUpperCase(Locale.ROOT));
            if (material == null) {
                DeluxeMenus.debug(
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
                if (this.options.baseColor().isPresent()) {
                    meta.setBaseColor(this.options.baseColor().get());
                }
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
                    final String rgbString = holder.setPlaceholders(this.options.rgb().get());
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

        short data = this.options.data();

        if (this.options.placeholderData().isPresent()) {
            final String parsedData = holder.setPlaceholders(this.options.placeholderData().get());
            try {
                data = Short.parseShort(parsedData);
            } catch (final NumberFormatException exception) {
                DeluxeMenus.printStacktrace(
                        "Invalid placeholder data found: " + parsedData + ".",
                        exception
                );
            }
        }

        if (data > 0) {
            itemStack.setDurability(data);
        }

        if (this.options.amount() != -1) {
            amount = this.options.amount();
        }

        if (this.options.dynamicAmount().isPresent()) {
            try {
                final int dynamicAmount = (int) Double.parseDouble(holder.setPlaceholders(this.options.dynamicAmount().get()));
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
                final int modelData = Integer.parseInt(holder.setPlaceholders(this.options.customModelData().get()));
                itemMeta.setCustomModelData(modelData);
            } catch (final Exception ignored) {
            }
        }

        if (this.options.displayName().isPresent()) {
            final String displayName = holder.setPlaceholders(this.options.displayName().get());
            itemMeta.setDisplayName(StringUtils.color(displayName));
        }

        if (!this.options.lore().isEmpty()) {
            final List<String> lore = this.options.lore().stream()
                    .map(holder::setPlaceholders)
                    .map(StringUtils::color)
                    .map(line -> line.split("\n"))
                    .flatMap(Arrays::stream)
                    .map(line -> line.split("\\\\n"))
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toList());

            itemMeta.setLore(lore);
        }

        if (!this.options.itemFlags().isEmpty()) {
            for (final ItemFlag flag : this.options.itemFlags()) {
                itemMeta.addItemFlags(flag);
            }
        }

        if (this.options.hideAttributes()) {
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }

        if (this.options.hideEnchants()) {
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        if (this.options.hidePotionEffects()) {
            itemMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        }

        if (this.options.unbreakable()) {
            itemMeta.setUnbreakable(true);
        }

        if (this.options.hideUnbreakable()) {
            itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        }

        if (itemMeta instanceof LeatherArmorMeta && this.options.rgb().isPresent()) {
            final String rgbString = holder.setPlaceholders(this.options.rgb().get());
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
        } else if (itemMeta instanceof FireworkEffectMeta && this.options.rgb().isPresent()) {
            final String rgbString = holder.setPlaceholders(this.options.rgb().get());
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
        } else if (itemMeta instanceof EnchantmentStorageMeta && !this.options.enchantments().isEmpty()) {
            final EnchantmentStorageMeta enchantmentStorageMeta = (EnchantmentStorageMeta) itemMeta;
            for (final Map.Entry<Enchantment, Integer> entry : this.options.enchantments().entrySet()) {
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

        if (!(itemMeta instanceof EnchantmentStorageMeta) && !this.options.enchantments().isEmpty()) {
            itemStack.addUnsafeEnchantments(this.options.enchantments());
        }

        if (NbtProvider.isAvailable()) {
            if (this.options.nbtString().isPresent()) {
                final String tag = holder.setPlaceholders(this.options.nbtString().get());
                if (tag.contains(":")) {
                    final String[] parts = tag.split(":", 2);
                    itemStack = NbtProvider.setString(itemStack, parts[0], parts[1]);
                }
            }

            if (this.options.nbtInt().isPresent()) {
                final String tag = holder.setPlaceholders(this.options.nbtInt().get());
                if (tag.contains(":")) {
                    final String[] parts = tag.split(":");
                    itemStack = NbtProvider.setInt(itemStack, parts[0], Integer.parseInt(parts[1]));
                }
            }

            for (String nbtTag : this.options.nbtStrings()) {
                final String tag = holder.setPlaceholders(nbtTag);
                if (tag.contains(":")) {
                    final String[] parts = tag.split(":", 2);
                    itemStack = NbtProvider.setString(itemStack, parts[0], parts[1]);
                }
            }

            for (String nbtTag : this.options.nbtInts()) {
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
        return DeluxeMenus.getInstance()
                .getItemHook(hookName)
                .map(itemHook -> itemHook.getItem(args));
    }

    public @NotNull MenuItemOptions options() {
        return options;
    }
}
