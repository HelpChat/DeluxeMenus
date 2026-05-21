package com.extendedclip.deluxemenus.command.subcommand;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.editor.MenuConfigEditor;
import com.extendedclip.deluxemenus.editor.MenuEditPromptRegistry;
import com.extendedclip.deluxemenus.editor.MenuEditorManager;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.utils.Messages;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EditCommand extends SubCommand {

    private static final String EDIT_PERMISSION = "deluxemenus.edit";
    private final MenuEditorManager editorManager;
    private final MenuConfigEditor configEditor;

    public EditCommand(final @NotNull DeluxeMenus plugin) {
        super(plugin);
        this.editorManager = new MenuEditorManager(plugin);
        this.configEditor = new MenuConfigEditor(plugin);
    }

    @Override
    public @NotNull String getName() {
        return "edit";
    }

    @Override
    public void execute(final @NotNull CommandSender sender, final @NotNull List<String> arguments) {
        if (!sender.hasPermission(EDIT_PERMISSION)) {
            plugin.sms(sender, Messages.NO_PERMISSION);
            return;
        }

        if (arguments.isEmpty()) {
            plugin.sms(sender, Messages.WRONG_USAGE);
            return;
        }

        final Optional<Menu> optionalMenu = findMenu(arguments.get(0));
        if (optionalMenu.isEmpty()) {
            plugin.sms(sender, Messages.INVALID_MENU.message().replaceText(MENU_REPLACER_BUILDER.replacement(arguments.get(0)).build()));
            return;
        }

        if (arguments.size() == 1) {
            if (!(sender instanceof Player)) {
                plugin.sms(sender, Messages.MUST_SPECIFY_PLAYER);
                return;
            }

            editorManager.open((Player) sender, optionalMenu.get());
            return;
        }

        if (arguments.size() == 3
                && "slot".equalsIgnoreCase(arguments.get(1))) {
            openSlot(sender, optionalMenu.get(), arguments.get(2));
            return;
        }

        if (arguments.size() >= 5
                && "set".equalsIgnoreCase(arguments.get(1))) {
            setValue(sender, optionalMenu.get(), arguments);
            return;
        }

        if (arguments.size() >= 4
                && "prompt".equalsIgnoreCase(arguments.get(1))) {
            prompt(sender, optionalMenu.get(), arguments);
            return;
        }

        if (arguments.size() >= 4
                && "menu".equalsIgnoreCase(arguments.get(1))) {
            setMenuValue(sender, optionalMenu.get(), arguments);
            return;
        }

        if (arguments.size() == 3
                && "delete".equalsIgnoreCase(arguments.get(1))) {
            deleteItem(sender, optionalMenu.get(), arguments.get(2));
            return;
        }

        plugin.sms(sender, Messages.WRONG_USAGE);
    }

    @Override
    public @Nullable List<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull List<String> arguments) {
        if (!sender.hasPermission(EDIT_PERMISSION)) {
            return null;
        }

        if (arguments.isEmpty()) {
            return List.of(getName());
        }

        if (arguments.size() == 1) {
            return complete(List.of(getName()), arguments.get(0));
        }

        if (!getName().equalsIgnoreCase(arguments.get(0))) {
            return null;
        }

        if (arguments.size() == 2) {
            return complete(menuNames(), arguments.get(1));
        }

        if (arguments.size() == 3) {
            return complete(List.of("set", "prompt", "slot", "menu", "delete"), arguments.get(2));
        }

        if (arguments.size() == 4 && "menu".equalsIgnoreCase(arguments.get(2))) {
            return complete(menuOptions(), arguments.get(3));
        }

        if (arguments.size() == 5 && ("set".equalsIgnoreCase(arguments.get(2)) || "prompt".equalsIgnoreCase(arguments.get(2)))) {
            return complete(editableOptions(), arguments.get(4));
        }

        return null;
    }

    private void openSlot(final @NotNull CommandSender sender, final @NotNull Menu menu, final @NotNull String slotInput) {
        if (!(sender instanceof Player)) {
            plugin.sms(sender, Messages.MUST_SPECIFY_PLAYER);
            return;
        }

        final int slot = parseSlot(sender, slotInput);
        if (slot < 0) {
            return;
        }

        editorManager.openSlot((Player) sender, menu, slot);
    }

    private void setValue(final @NotNull CommandSender sender, final @NotNull Menu menu, final @NotNull List<String> arguments) {
        final int slot;
        try {
            slot = Integer.parseInt(arguments.get(2));
        } catch (final NumberFormatException exception) {
            plugin.sms(sender, Messages.WRONG_USAGE);
            return;
        }

        final String option = arguments.get(3).toLowerCase(Locale.ROOT);
        if (!editableOptions().contains(option)) {
            plugin.sms(sender, Component.text("Unknown editable option: " + option, NamedTextColor.RED));
            return;
        }

        final String value = String.join(" ", arguments.subList(4, arguments.size()));
        try {
            if (!configEditor.setItemValue(menu, slot, option, value)) {
                plugin.sms(sender, Component.text("No editable item was found in slot " + slot + ".", NamedTextColor.RED));
                return;
            }
        } catch (final IOException exception) {
            plugin.printStacktrace("Failed to save menu edit.", exception);
            plugin.sms(sender, Component.text("Failed to save menu edit.", NamedTextColor.RED));
            return;
        }

        configEditor.reload(menu);
        plugin.sms(sender, Component.text("Updated " + option + " for slot " + slot + ".", NamedTextColor.GREEN));
    }

    private void prompt(final @NotNull CommandSender sender, final @NotNull Menu menu, final @NotNull List<String> arguments) {
        if (!(sender instanceof Player)) {
            plugin.sms(sender, Messages.MUST_SPECIFY_PLAYER);
            return;
        }

        final int slot;
        try {
            slot = Integer.parseInt(arguments.get(2));
        } catch (final NumberFormatException exception) {
            plugin.sms(sender, Messages.WRONG_USAGE);
            return;
        }

        final String option = arguments.get(3).toLowerCase(Locale.ROOT);
        if (!editableOptions().contains(option)) {
            plugin.sms(sender, Component.text("Unknown editable option: " + option, NamedTextColor.RED));
            return;
        }

        final Player player = (Player) sender;
        MenuEditPromptRegistry.begin(player, menu.options().name(), slot, option);
        plugin.sms(player, Component.text("Type the new " + option + " in chat, or type cancel.", NamedTextColor.YELLOW));
    }

    private void setMenuValue(final @NotNull CommandSender sender, final @NotNull Menu menu, final @NotNull List<String> arguments) {
        final String option = arguments.get(2).toLowerCase(Locale.ROOT);
        if (!menuOptions().contains(option)) {
            plugin.sms(sender, Component.text("Unknown editable menu option: " + option, NamedTextColor.RED));
            return;
        }

        final String value = String.join(" ", arguments.subList(3, arguments.size()));
        try {
            configEditor.setMenuValue(menu, option, value);
        } catch (final IOException exception) {
            plugin.printStacktrace("Failed to save menu edit.", exception);
            plugin.sms(sender, Component.text("Failed to save menu edit.", NamedTextColor.RED));
            return;
        }

        configEditor.reload(menu);
        plugin.sms(sender, Component.text("Updated " + option + " for " + menu.options().name() + ".", NamedTextColor.GREEN));
    }

    private void deleteItem(final @NotNull CommandSender sender, final @NotNull Menu menu, final @NotNull String slotInput) {
        final int slot = parseSlot(sender, slotInput);
        if (slot < 0) {
            return;
        }

        try {
            if (!configEditor.deleteItem(menu, slot)) {
                plugin.sms(sender, Component.text("No item config was found in slot " + slot + ".", NamedTextColor.RED));
                return;
            }
        } catch (final IOException exception) {
            plugin.printStacktrace("Failed to delete menu item.", exception);
            plugin.sms(sender, Component.text("Failed to delete menu item.", NamedTextColor.RED));
            return;
        }

        configEditor.reload(menu);
        plugin.sms(sender, Component.text("Deleted item config for slot " + slot + ".", NamedTextColor.GREEN));
    }

    private int parseSlot(final @NotNull CommandSender sender, final @NotNull String input) {
        try {
            return Integer.parseInt(input);
        } catch (final NumberFormatException exception) {
            plugin.sms(sender, Messages.WRONG_USAGE);
            return -1;
        }
    }

    private @NotNull List<String> editableOptions() {
        return List.of(
                "material",
                "amount",
                "priority",
                "display_name",
                "lore",
                "model_data",
                "item_flags",
                "update",
                "click_commands",
                "left_click_commands",
                "right_click_commands",
                "shift_left_click_commands",
                "shift_right_click_commands",
                "middle_click_commands"
        );
    }

    private @NotNull List<String> menuOptions() {
        return List.of("menu_title", "size");
    }

    private @NotNull Optional<Menu> findMenu(final @NotNull String menuName) {
        final Optional<Menu> menu = Menu.getMenuByName(menuName);
        if (menu.isPresent()) {
            return menu;
        }

        return Menu.getSubMenuByName(menuName);
    }

    private @NotNull Collection<String> menuNames() {
        return java.util.stream.Stream.concat(
                        Menu.getAllMenuNames().stream(),
                        Menu.getAllSubMenus().stream().map(menu -> menu.options().name())
                )
                .collect(Collectors.toList());
    }

    private @NotNull List<String> complete(final @NotNull Collection<String> values, final @NotNull String argument) {
        final String lowerArgument = argument.toLowerCase(Locale.ROOT);
        return values.stream()
                .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(lowerArgument))
                .collect(Collectors.toList());
    }
}
