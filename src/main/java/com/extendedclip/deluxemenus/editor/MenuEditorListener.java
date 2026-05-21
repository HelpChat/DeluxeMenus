package com.extendedclip.deluxemenus.editor;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.listener.Listener;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.menu.MenuItem;
import java.util.Optional;
import java.util.TreeMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;

public class MenuEditorListener extends Listener {

    public MenuEditorListener(final @NotNull DeluxeMenus plugin) {
        super(plugin);
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
        final Optional<Menu> optionalMenu = Menu.getMenuByName(editorHolder.menuName());
        if (optionalMenu.isEmpty()) {
            player.closeInventory();
            return;
        }

        final int slot = event.getRawSlot();
        final TreeMap<Integer, MenuItem> items = optionalMenu.get().getMenuItems().get(slot);
        if (items == null || items.isEmpty()) {
            plugin.sms(player, text("Slot " + slot + " is empty.", NamedTextColor.GRAY)
                    .append(newline())
                    .append(action(editorHolder.menuName(), slot, "material")));
            return;
        }

        final MenuItem item = items.firstEntry().getValue();
        plugin.sms(player, text("Editing " + editorHolder.menuName() + " slot " + slot, NamedTextColor.GOLD)
                .append(newline())
                .append(text("> Material: ", NamedTextColor.GRAY))
                .append(text(item.options().material(), NamedTextColor.WHITE))
                .append(newline())
                .append(text("> Priority: ", NamedTextColor.GRAY))
                .append(text(String.valueOf(item.options().priority()), NamedTextColor.WHITE))
                .append(newline())
                .append(action(editorHolder.menuName(), slot, "material"))
                .append(newline())
                .append(action(editorHolder.menuName(), slot, "display_name"))
                .append(newline())
                .append(action(editorHolder.menuName(), slot, "lore"))
                .append(newline())
                .append(action(editorHolder.menuName(), slot, "left_click_commands"))
                .append(newline())
                .append(action(editorHolder.menuName(), slot, "right_click_commands")));
    }

    private @NotNull Component action(final @NotNull String menuName, final int slot, final @NotNull String option) {
        final String command = "/dm edit " + menuName + " prompt " + slot + " " + option;
        return text(command, NamedTextColor.YELLOW)
                .clickEvent(ClickEvent.runCommand(command));
    }
}
