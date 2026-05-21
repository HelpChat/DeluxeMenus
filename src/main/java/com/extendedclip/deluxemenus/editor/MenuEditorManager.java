package com.extendedclip.deluxemenus.editor;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.menu.MenuHolder;
import com.extendedclip.deluxemenus.menu.MenuItem;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MenuEditorManager {

    private final DeluxeMenus plugin;

    public MenuEditorManager(final @NotNull DeluxeMenus plugin) {
        this.plugin = plugin;
    }

    public void open(final @NotNull Player player, final @NotNull Menu menu) {
        final int size = menu.options().size();
        final MenuEditorHolder editorHolder = new MenuEditorHolder(menu.options().name());
        final Inventory inventory = Bukkit.createInventory(editorHolder, size, "Editing: " + menu.options().name());
        editorHolder.setInventory(inventory);

        final MenuHolder renderHolder = new MenuHolder(plugin, player);
        renderHolder.setMenuName(menu.options().name());
        renderHolder.setPlaceholderPlayer(player);
        renderHolder.parsePlaceholdersInArguments(menu.options().parsePlaceholdersInArguments());
        renderHolder.parsePlaceholdersAfterArguments(menu.options().parsePlaceholdersAfterArguments());

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
}
