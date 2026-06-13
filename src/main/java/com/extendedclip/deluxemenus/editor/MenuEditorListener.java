package com.extendedclip.deluxemenus.editor;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.listener.Listener;
import com.extendedclip.deluxemenus.menu.Menu;
import java.io.IOException;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;

public class MenuEditorListener extends Listener {

    private final MenuEditorManager editorManager;
    private final MenuConfigEditor configEditor;

    public MenuEditorListener(final @NotNull DeluxeMenus plugin) {
        super(plugin);
        this.editorManager = new MenuEditorManager(plugin);
        this.configEditor = new MenuConfigEditor(plugin);
    }

    @EventHandler
    public void onChat(final @NotNull AsyncPlayerChatEvent event) {
        if (!MenuEditPromptRegistry.hasPrompt(event.getPlayer())) {
            return;
        }

        event.setCancelled(true);
        plugin.getScheduler().runTask(event.getPlayer(), () -> MenuEditPromptRegistry.submit(plugin, event.getPlayer(), event.getMessage()));
    }

    @EventHandler
    public void onQuit(final @NotNull PlayerQuitEvent event) {
        MenuEditPromptRegistry.cancel(event.getPlayer());
    }

    @EventHandler
    public void onDrag(final @NotNull InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof MenuEditorHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClick(final @NotNull InventoryClickEvent event) {
        final InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof MenuEditorHolder)) {
            return;
        }

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getWhoClicked();
        final MenuEditorHolder editorHolder = (MenuEditorHolder) holder;
        final Optional<Menu> optionalMenu = findMenu(editorHolder.menuName());
        if (optionalMenu.isEmpty()) {
            player.closeInventory();
            plugin.sms(player, text("Menu is no longer loaded.", NamedTextColor.RED));
            return;
        }

        final int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) {
            return;
        }

        if (editorHolder.view() == MenuEditorHolder.View.PREVIEW) {
            editorManager.openSlot(player, optionalMenu.get(), slot);
            return;
        }

        handleSlotEditor(player, optionalMenu.get(), editorHolder.slot(), slot);
    }

    private void handleSlotEditor(
            final @NotNull Player player,
            final @NotNull Menu menu,
            final int editedSlot,
            final int buttonSlot
    ) {
        switch (buttonSlot) {
            case MenuEditorManager.BUTTON_MATERIAL:
                beginPrompt(player, menu, editedSlot, "material");
                break;
            case MenuEditorManager.BUTTON_AMOUNT:
                beginPrompt(player, menu, editedSlot, "amount");
                break;
            case MenuEditorManager.BUTTON_DISPLAY_NAME:
                beginPrompt(player, menu, editedSlot, "display_name");
                break;
            case MenuEditorManager.BUTTON_LORE:
                beginPrompt(player, menu, editedSlot, "lore");
                break;
            case MenuEditorManager.BUTTON_MODEL_DATA:
                beginPrompt(player, menu, editedSlot, "model_data");
                break;
            case MenuEditorManager.BUTTON_ITEM_FLAGS:
                beginPrompt(player, menu, editedSlot, "item_flags");
                break;
            case MenuEditorManager.BUTTON_UPDATE:
                toggleUpdate(player, menu, editedSlot);
                break;
            case MenuEditorManager.BUTTON_CLICK_COMMANDS:
                beginPrompt(player, menu, editedSlot, "click_commands");
                break;
            case MenuEditorManager.BUTTON_LEFT_CLICK_COMMANDS:
                beginPrompt(player, menu, editedSlot, "left_click_commands");
                break;
            case MenuEditorManager.BUTTON_RIGHT_CLICK_COMMANDS:
                beginPrompt(player, menu, editedSlot, "right_click_commands");
                break;
            case MenuEditorManager.BUTTON_SHIFT_LEFT_CLICK_COMMANDS:
                beginPrompt(player, menu, editedSlot, "shift_left_click_commands");
                break;
            case MenuEditorManager.BUTTON_SHIFT_RIGHT_CLICK_COMMANDS:
                beginPrompt(player, menu, editedSlot, "shift_right_click_commands");
                break;
            case MenuEditorManager.BUTTON_MIDDLE_CLICK_COMMANDS:
                beginPrompt(player, menu, editedSlot, "middle_click_commands");
                break;
            case MenuEditorManager.BUTTON_PRIORITY:
                beginPrompt(player, menu, editedSlot, "priority");
                break;
            case MenuEditorManager.BUTTON_DELETE:
                deleteItem(player, menu, editedSlot);
                break;
            case MenuEditorManager.BUTTON_BACK:
                editorManager.open(player, menu);
                break;
            case MenuEditorManager.BUTTON_REFRESH:
                editorManager.openSlot(player, menu, editedSlot);
                break;
            default:
                break;
        }
    }

    private void beginPrompt(
            final @NotNull Player player,
            final @NotNull Menu menu,
            final int slot,
            final @NotNull String option
    ) {
        MenuEditPromptRegistry.begin(player, menu.options().name(), slot, option);
        player.closeInventory();

        final String current = configEditor.getItemString(menu, slot, option).orElse("-");
        plugin.sms(player, text("Editing " + option + " for " + menu.options().name() + " slot " + slot, NamedTextColor.GOLD)
                .append(newline())
                .append(text("Current: ", NamedTextColor.GRAY))
                .append(text(current, NamedTextColor.WHITE))
                .append(newline())
                .append(text("Send a value in chat. Use | between lore or command lines. ", NamedTextColor.YELLOW))
                .append(text("Cancel", NamedTextColor.RED).clickEvent(ClickEvent.suggestCommand("cancel"))));
    }

    private void toggleUpdate(final @NotNull Player player, final @NotNull Menu menu, final int slot) {
        final boolean current = Boolean.parseBoolean(configEditor.getItemString(menu, slot, "update").orElse("false"));
        try {
            configEditor.setItemValue(menu, slot, "update", String.valueOf(!current));
            configEditor.reload(menu);
        } catch (final IOException exception) {
            plugin.printStacktrace("Failed to save menu edit.", exception);
            plugin.sms(player, text("Failed to save menu edit.", NamedTextColor.RED));
            return;
        }

        findMenu(menu.options().name()).ifPresent(reloaded -> editorManager.openSlot(player, reloaded, slot));
    }

    private void deleteItem(final @NotNull Player player, final @NotNull Menu menu, final int slot) {
        try {
            if (!configEditor.deleteItem(menu, slot)) {
                plugin.sms(player, text("No item config was found for slot " + slot + ".", NamedTextColor.RED));
                return;
            }
            configEditor.reload(menu);
        } catch (final IOException exception) {
            plugin.printStacktrace("Failed to delete menu item.", exception);
            plugin.sms(player, text("Failed to delete menu item.", NamedTextColor.RED));
            return;
        }

        findMenu(menu.options().name()).ifPresent(reloaded -> editorManager.open(player, reloaded));
    }

    private @NotNull Optional<Menu> findMenu(final @NotNull String menuName) {
        final Optional<Menu> menu = Menu.getMenuByName(menuName);
        if (menu.isPresent()) {
            return menu;
        }

        return Menu.getSubMenuByName(menuName);
    }
}
