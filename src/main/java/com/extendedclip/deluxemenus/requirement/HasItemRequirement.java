package com.extendedclip.deluxemenus.requirement;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.hooks.ItemHook;
import com.extendedclip.deluxemenus.menu.MenuHolder;
import com.extendedclip.deluxemenus.requirement.wrappers.ItemWrapper;
import com.extendedclip.deluxemenus.utils.StringUtils;
import com.extendedclip.deluxemenus.utils.VersionHelper;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HasItemRequirement extends Requirement {

  private final ItemWrapper wrapper;
  private final boolean invert;

  public HasItemRequirement(ItemWrapper wrapper, boolean invert) {
    this.wrapper = wrapper;
    this.invert = invert;
  }

  @Override
  public boolean evaluate(MenuHolder holder) {
    String materialName = holder.setPlaceholdersAndArguments(wrapper.getMaterial());
    Material material = DeluxeMenus.MATERIALS.get(materialName.toUpperCase());
    ItemHook pluginHook = null;
    if (material == null) {
      pluginHook = DeluxeMenus.getInstance().getItemHooks().values()
              .stream()
              .filter(x -> materialName.toLowerCase().startsWith(x.getPrefix()))
              .findFirst()
              .orElse(null);
      if (pluginHook == null) return invert;
    }

    if (material == Material.AIR) return invert == (holder.getViewer().getInventory().firstEmpty() == -1);

    ItemStack[] armor = wrapper.checkArmor() ? holder.getViewer().getInventory().getArmorContents() : null;
    ItemStack[] offHand = wrapper.checkOffhand() ? holder.getViewer().getInventory().getExtraContents() : null;
    ItemStack[] inventory = holder.getViewer().getInventory().getStorageContents();

    int total = 0;
    for (ItemStack itemToCheck: inventory) {
      if (!isRequiredItem(itemToCheck, holder, material, pluginHook)) continue;
      total += itemToCheck.getAmount();
    }

    if (offHand != null) {
      for (ItemStack itemToCheck: offHand) {
        if (!isRequiredItem(itemToCheck, holder, material, pluginHook)) continue;
        total += itemToCheck.getAmount();
      }
    }

    if (armor != null) {
      for (ItemStack itemToCheck: armor) {
        if (!isRequiredItem(itemToCheck, holder, material, pluginHook)) continue;
        total += itemToCheck.getAmount();
      }
    }

    return invert == (total < wrapper.getAmount());
  }

  private boolean isRequiredItem(ItemStack itemToCheck, MenuHolder holder, Material material, ItemHook pluginHook) {
    if (itemToCheck == null || itemToCheck.getType() == Material.AIR) return false;

    if (pluginHook != null) {
      if (!pluginHook.itemMatchesIdentifiers(itemToCheck, holder.setPlaceholdersAndArguments(wrapper.getMaterial().substring(pluginHook.getPrefix().length())))) return false;
    }
    else if (wrapper.getMaterial() != null && itemToCheck.getType() != material) return false;
    if (wrapper.hasData() && itemToCheck.getDurability() != wrapper.getData()) return false;

    ItemMeta metaToCheck = itemToCheck.getItemMeta();
    if (wrapper.isStrict()) {
      if (metaToCheck != null) {
        if (VersionHelper.IS_CUSTOM_MODEL_DATA) {
          if (metaToCheck.hasCustomModelData()) return false;
        }
        if (metaToCheck.hasLore()) return false;
        return !metaToCheck.hasDisplayName();
      }

    } else {
      if ((wrapper.getCustomData() != 0 || wrapper.getName() != null || wrapper.getLore() != null) && metaToCheck == null)
        return false;

      if (wrapper.getCustomData() != 0) {
        if (VersionHelper.IS_CUSTOM_MODEL_DATA) {
          if (!metaToCheck.hasCustomModelData()) return false;
          if (metaToCheck.getCustomModelData() != wrapper.getCustomData()) return false;
        }
      }

      if (wrapper.getName() != null) {
        if (!metaToCheck.hasDisplayName()) return false;

        String name = StringUtils.color(holder.setPlaceholdersAndArguments(wrapper.getName()));
        String nameToCheck = StringUtils.color(holder.setPlaceholdersAndArguments(metaToCheck.getDisplayName()));

        if (wrapper.checkNameContains() && wrapper.checkNameIgnoreCase()) {
          if (!org.apache.commons.lang3.StringUtils.containsIgnoreCase(nameToCheck, name)) return false;
        }
        else if (wrapper.checkNameContains()) {
          if (!nameToCheck.contains(name)) return false;
        }
        else if (wrapper.checkNameIgnoreCase()) {
          if (!nameToCheck.equalsIgnoreCase(name)) return false;
        }
        else if (!nameToCheck.equals(name)) {
          return false;
        }
      }

      if (wrapper.getLoreList() != null) {
        List<String> loreX = metaToCheck.getLore();
        if (loreX == null) return false;

        String lore = wrapper.getLoreList().stream().map(holder::setPlaceholdersAndArguments).map(StringUtils::color).collect(Collectors.joining("&&"));
        String loreToCheck = loreX.stream().map(holder::setPlaceholdersAndArguments).map(StringUtils::color).collect(Collectors.joining("&&"));

        if (wrapper.checkLoreContains() && wrapper.checkLoreIgnoreCase()) {
          if (!org.apache.commons.lang3.StringUtils.containsIgnoreCase(loreToCheck, lore)) return false;
        }
        else if (wrapper.checkLoreContains()) {
          if (!loreToCheck.contains(lore)) return false;
        }
        else if (wrapper.checkLoreIgnoreCase()) {
          if (!loreToCheck.equalsIgnoreCase(lore)) return false;
        }
        else if (!loreToCheck.equals(lore)) {
          return false;
        }
      }

      if (wrapper.getLore() != null) {
        List<String> loreX = metaToCheck.getLore();
        if (loreX == null) return false;

        String lore = StringUtils.color(holder.setPlaceholdersAndArguments(wrapper.getLore()));
        String loreToCheck = loreX.stream().map(holder::setPlaceholdersAndArguments).map(StringUtils::color).collect(Collectors.joining("&&"));

        if (wrapper.checkLoreContains() && wrapper.checkLoreIgnoreCase()) {
          return org.apache.commons.lang3.StringUtils.containsIgnoreCase(loreToCheck, lore);
        }
        else if (wrapper.checkLoreContains()) {
          return loreToCheck.contains(lore);
        }
        else if (wrapper.checkLoreIgnoreCase()) {
          return loreToCheck.equalsIgnoreCase(lore);
        }
        else return loreToCheck.equals(lore);
      }
    }
    return true;
  }
}
