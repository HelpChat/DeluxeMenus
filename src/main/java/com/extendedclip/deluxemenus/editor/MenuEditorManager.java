package com.extendedclip.deluxemenus.editor;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.menu.MenuHolder;
import com.extendedclip.deluxemenus.menu.MenuItem;
import com.extendedclip.deluxemenus.utils.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MenuEditorManager {

    public static final int BUTTON_MATERIAL = 19;
    public static final int BUTTON_AMOUNT = 20;
    public static final int BUTTON_DISPLAY_NAME = 21;
    public static final int BUTTON_LORE = 22;
    public static final int BUTTON_MODEL_DATA = 23;
    public static final int BUTTON_ITEM_FLAGS = 24;
    public static final int BUTTON_UPDATE = 25;
    public static final int BUTTON_CLICK_COMMANDS = 28;
    public static final int BUTTON_LEFT_CLICK_COMMANDS = 29;
    public static final int BUTTON_RIGHT_CLICK_COMMANDS = 30;
    public static final int BUTTON_SHIFT_LEFT_CLICK_COMMANDS = 31;
    public static final int BUTTON_SHIFT_RIGHT_CLICK_COMMANDS = 32;
    public static final int BUTTON_MIDDLE_CLICK_COMMANDS = 33;
    public static final int BUTTON_PRIORITY = 37;
    public static final int BUTTON_DELETE = 40;
    public static final int BUTTON_BACK = 45;
    public static final int BUTTON_REFRESH = 49;

    private final DeluxeMenus plugin;
    private final MenuConfigEditor configEditor;

    public MenuEditorManager(final @NotNull DeluxeMenus plugin) {
        this.plugin = plugin;
        this.configEditor = new MenuConfigEditor(plugin);
    }

    public void open(final @NotNull Player player, final @NotNull Menu menu) {
        final int size = menu.options().size();
        final MenuEditorHolder editorHolder = new MenuEditorHolder(menu.options().name());
        final Inventory inventory = Bukkit.createInventory(editorHolder, size, title("Edit: " + menu.options().name()));
        editorHolder.setInventory(inventory);

        final MenuHolder renderHolder = renderHolder(player, menu);
        for (final Map.Entry<Integer, TreeMap<Integer, MenuItem>> entry : menu.getMenuItems().entrySet()) {
            final int slot = entry.getKey();
            if (slot < 0 || slot >= size || entry.getValue().isEmpty()) {
                continue;
            }

            final ItemStack itemStack = entry.getValue().firstEntry().getValue().getItemStack(renderHolder);
            if (itemStack != null) {
                inventory.setItem(slot, itemStack);
            }
        }

        player.openInventory(inventory);
    }

    public void openSlot(final @NotNull Player player, final @NotNull Menu menu, final int slot) {
        final MenuEditorHolder editorHolder = new MenuEditorHolder(menu.options().name(), MenuEditorHolder.View.SLOT, slot);
        final Inventory inventory = Bukkit.createInventory(editorHolder, 54, title("Slot " + slot + ": " + menu.options().name()));
        editorHolder.setInventory(inventory);

        inventory.setItem(4, previewItem(player, menu, slot));
        inventory.setItem(13, button(Material.PAPER, "&eSlot " + slot,
                "&7Menu: &f" + menu.options().name(),
                "&7File: &f" + menu.path(),
                "&7Loaded item: &f" + (hasItem(menu, slot) ? "yes" : "no")));

        inventory.setItem(BUTTON_MATERIAL, promptButton(Material.STONE, "Material", menu, slot, "material"));
        inventory.setItem(BUTTON_AMOUNT, promptButton(Material.EMERALD, "Amount", menu, slot, "amount"));
        inventory.setItem(BUTTON_DISPLAY_NAME, promptButton(Material.NAME_TAG, "Display Name", menu, slot, "display_name"));
        inventory.setItem(BUTTON_LORE, promptButton(Material.BOOK, "Lore", menu, slot, "lore"));
        inventory.setItem(BUTTON_MODEL_DATA, promptButton(Material.ITEM_FRAME, "Model Data", menu, slot, "model_data"));
        inventory.setItem(BUTTON_ITEM_FLAGS, promptButton(Material.HOPPER, "Item Flags", menu, slot, "item_flags"));
        inventory.setItem(BUTTON_UPDATE, button(Material.CLOCK, "&eToggle Update",
                "&7Current: &f" + value(menu, slot, "update", "false"),
                "&7Click to toggle placeholder refresh."));
        inventory.setItem(BUTTON_CLICK_COMMANDS, promptButton(Material.COMMAND_BLOCK, "Click Commands", menu, slot, "click_commands"));
        inventory.setItem(BUTTON_LEFT_CLICK_COMMANDS, promptButton(Material.COMMAND_BLOCK, "Left Click Commands", menu, slot, "left_click_commands"));
        inventory.setItem(BUTTON_RIGHT_CLICK_COMMANDS, promptButton(Material.COMMAND_BLOCK, "Right Click Commands", menu, slot, "right_click_commands"));
        inventory.setItem(BUTTON_SHIFT_LEFT_CLICK_COMMANDS, promptButton(Material.COMMAND_BLOCK, "Shift Left Commands", menu, slot, "shift_left_click_commands"));
        inventory.setItem(BUTTON_SHIFT_RIGHT_CLICK_COMMANDS, promptButton(Material.COMMAND_BLOCK, "Shift Right Commands", menu, slot, "shift_right_click_commands"));
        inventory.setItem(BUTTON_MIDDLE_CLICK_COMMANDS, promptButton(Material.COMMAND_BLOCK, "Middle Click Commands", menu, slot, "middle_click_commands"));
        inventory.setItem(BUTTON_PRIORITY, promptButton(Material.COMPARATOR, "Priority", menu, slot, "priority"));
        inventory.setItem(BUTTON_DELETE, button(Material.BARRIER, "&cDelete Item", "&7Removes the item config for this slot."));
        inventory.setItem(BUTTON_BACK, button(Material.ARROW, "&aBack", "&7Return to the menu preview."));
        inventory.setItem(BUTTON_REFRESH, button(Material.CHEST, "&bReload Preview", "&7Reload the menu editor view."));

        player.openInventory(inventory);
    }

    private @NotNull MenuHolder renderHolder(final @NotNull Player player, final @NotNull Menu menu) {
        final MenuHolder renderHolder = new MenuHolder(plugin, player);
        renderHolder.setMenuName(menu.options().name());
        renderHolder.setPlaceholderPlayer(player);
        renderHolder.parsePlaceholdersInArguments(menu.options().parsePlaceholdersInArguments());
        renderHolder.parsePlaceholdersAfterArguments(menu.options().parsePlaceholdersAfterArguments());
        return renderHolder;
    }

    private @NotNull ItemStack previewItem(final @NotNull Player player, final @NotNull Menu menu, final int slot) {
        final TreeMap<Integer, MenuItem> items = menu.getMenuItems().get(slot);
        if (items != null && !items.isEmpty()) {
            final ItemStack itemStack = items.firstEntry().getValue().getItemStack(renderHolder(player, menu));
            if (itemStack != null) {
                return itemStack;
            }
        }

        return button(Material.GRAY_STAINED_GLASS_PANE, "&7Empty Slot", "&7Set a material to create an item.");
    }

    private @NotNull ItemStack promptButton(
            final @NotNull Material material,
            final @NotNull String label,
            final @NotNull Menu menu,
            final int slot,
            final @NotNull String option
    ) {
        return button(material, "&e" + label,
                "&7Current:",
                "&f" + compact(value(menu, slot, option, "")),
                "&7Click to edit.");
    }

    private @NotNull ItemStack button(final @NotNull Material material, final @NotNull String name, final @NotNull String... lore) {
        final ItemStack itemStack = new ItemStack(material);
        final ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return itemStack;
        }

        meta.setDisplayName(StringUtils.color(name));
        final List<String> coloredLore = new ArrayList<>();
        for (final String line : lore) {
            coloredLore.add(StringUtils.color(line));
        }
        meta.setLore(coloredLore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private @NotNull String value(final @NotNull Menu menu, final int slot, final @NotNull String option, final @NotNull String fallback) {
        return configEditor.getItemString(menu, slot, option).orElse(fallback);
    }

    private boolean hasItem(final @NotNull Menu menu, final int slot) {
        final TreeMap<Integer, MenuItem> items = menu.getMenuItems().get(slot);
        return items != null && !items.isEmpty();
    }

    private @NotNull String compact(final @Nullable String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }

        final String oneLine = value.replace("\r", "").replace("\n", " | ");
        if (oneLine.length() <= 34) {
            return oneLine;
        }

        return oneLine.substring(0, 31) + "...";
    }

    private @NotNull String title(final @NotNull String title) {
        final String colored = StringUtils.color("&8" + title);
        if (colored.length() <= 32) {
            return colored;
        }

        return colored.substring(0, 32);
    }
}
