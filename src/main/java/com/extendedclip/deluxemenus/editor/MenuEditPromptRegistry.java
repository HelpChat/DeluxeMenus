package com.extendedclip.deluxemenus.editor;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.Menu;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.text;

public final class MenuEditPromptRegistry {

    private static final Map<UUID, Prompt> PROMPTS = new ConcurrentHashMap<>();

    private MenuEditPromptRegistry() {
    }

    public static void begin(final @NotNull Player player, final @NotNull String menuName, final int slot, final @NotNull String option) {
        PROMPTS.put(player.getUniqueId(), new Prompt(menuName, slot, option));
    }

    public static boolean hasPrompt(final @NotNull Player player) {
        return PROMPTS.containsKey(player.getUniqueId());
    }

    public static void cancel(final @NotNull Player player) {
        PROMPTS.remove(player.getUniqueId());
    }

    public static void submit(final @NotNull DeluxeMenus plugin, final @NotNull Player player, final @NotNull String value) {
        final Prompt prompt = PROMPTS.remove(player.getUniqueId());
        if (prompt == null) {
            return;
        }

        if ("cancel".equalsIgnoreCase(value)) {
            plugin.sms(player, text("Edit cancelled.", NamedTextColor.GRAY));
            return;
        }

        final Optional<Menu> optionalMenu = findMenu(prompt.menuName);
        if (optionalMenu.isEmpty()) {
            plugin.sms(player, text("Menu is no longer loaded.", NamedTextColor.RED));
            return;
        }

        final Menu menu = optionalMenu.get();
        final MenuConfigEditor configEditor = new MenuConfigEditor(plugin);
        try {
            configEditor.setItemValue(menu, prompt.slot, prompt.option, value);
        } catch (final IOException exception) {
            plugin.printStacktrace("Failed to save menu edit.", exception);
            plugin.sms(player, text("Failed to save menu edit.", NamedTextColor.RED));
            return;
        }

        configEditor.reload(menu);
        plugin.sms(player, text("Updated " + prompt.option + " for slot " + prompt.slot + " in " + configEditor.describeTarget(menu) + ".", NamedTextColor.GREEN));
        findMenu(prompt.menuName).ifPresent(reloaded -> new MenuEditorManager(plugin).openSlot(player, reloaded, prompt.slot));
    }

    private static @NotNull Optional<Menu> findMenu(final @NotNull String menuName) {
        final Optional<Menu> menu = Menu.getMenuByName(menuName);
        if (menu.isPresent()) {
            return menu;
        }

        return Menu.getSubMenuByName(menuName);
    }

    private static class Prompt {
        private final String menuName;
        private final int slot;
        private final String option;

        private Prompt(final @NotNull String menuName, final int slot, final @NotNull String option) {
            this.menuName = menuName;
            this.slot = slot;
            this.option = option;
        }
    }
}
