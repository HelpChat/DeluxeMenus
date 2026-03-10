package com.extendedclip.deluxemenus.utils;

import com.extendedclip.deluxemenus.persistentmeta.DataType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;

public enum Messages {

    PLUGIN_TITLE(empty()
            .append(text("Deluxe", NamedTextColor.GOLD, TextDecoration.BOLD))
            .append(text("Menus", NamedTextColor.YELLOW))),

    PLUGIN_VERSION(PLUGIN_TITLE.message
            .append(space())
            .append(text("version", NamedTextColor.WHITE))
            .append(space())
            .append(text("<version>", NamedTextColor.YELLOW))
            .append(newline())
            .append(text("Created by", NamedTextColor.GRAY))
            .append(space())
            .append(text("<authors>", NamedTextColor.WHITE))),

    HELP(PLUGIN_TITLE.message
            .append(space())
            .append(text("help", NamedTextColor.WHITE))
            .append(newline())
            .append(text(">", NamedTextColor.AQUA))
            .append(space().append(space()))
            .append(text("/dm open <menu-name> [player]", NamedTextColor.WHITE))
            .append(newline())
            .append(text(">", NamedTextColor.AQUA))
            .append(space().append(space()))
            .append(text("/dm list [page/all]", NamedTextColor.WHITE))
            .append(newline())
            .append(text(">", NamedTextColor.AQUA))
            .append(space().append(space()))
            .append(text("/dm dump <menu-name/config>", NamedTextColor.WHITE))
            .append(newline())
            .append(text(">", NamedTextColor.AQUA))
            .append(space().append(space()))
            .append(text("/dm reload [menu-name]", NamedTextColor.WHITE))),

    HELP_OP(PLUGIN_TITLE.message
            .append(space())
            .append(text("help", NamedTextColor.WHITE))
            .append(newline())
            .append(text(">", NamedTextColor.AQUA))
            .append(space().append(space()))
            .append(text("/dm open <menu-name> [player]", NamedTextColor.WHITE))
            .append(newline())
            .append(text(">", NamedTextColor.AQUA))
            .append(space().append(space()))
            .append(text("/dm list [page/all]", NamedTextColor.WHITE))
            .append(newline())
            .append(text(">", NamedTextColor.AQUA))
            .append(space().append(space()))
            .append(text("/dm execute <player> <action>", NamedTextColor.WHITE))
            .append(newline())
            .append(text(">", NamedTextColor.AQUA))
            .append(space().append(space()))
            .append(text("/dm dump <menu-name/config>", NamedTextColor.WHITE))
            .append(newline())
            .append(text(">", NamedTextColor.AQUA))
            .append(space().append(space()))
            .append(text("/dm meta <player> <set/remove/add/subtract/list/show>", NamedTextColor.WHITE))
            .append(newline())
            .append(text(">", NamedTextColor.AQUA))
            .append(space().append(space()))
            .append(text("/dm reload [menu-name]", NamedTextColor.WHITE))),

    NO_PERMISSION(text("You don't have permission to do that!", NamedTextColor.RED)),
    NO_PERMISSION_PLAYER_ARGUMENT(text("You don't have permission to use the argument -p:<player>!", NamedTextColor.RED)),
    WRONG_USAGE_BASE(empty()
            .append(text("Incorrect Usage!", NamedTextColor.RED))
            .append(space())
            .append(text("Use"))
            .append(space())),
    WRONG_USAGE(WRONG_USAGE_BASE.message
            .append(text("/dm help", NamedTextColor.GRAY))),
    WRONG_USAGE_EXECUTE_COMMAND(WRONG_USAGE_BASE.message
            .append(text("/dm execute <player> <action>", NamedTextColor.GRAY))),
    WRONG_USAGE_DUMP_COMMAND(WRONG_USAGE_BASE.message
            .append(text("/dm dump <menu-name/config>", NamedTextColor.GRAY))),

    WRONG_USAGE_OPEN_COMMAND(WRONG_USAGE_BASE.message
        .append(text("/dm open <menu-name> [player]", NamedTextColor.GRAY))),
    WRONG_USAGE_REFRESH_COMMAND(WRONG_USAGE_BASE.message
        .append(text("/dm refresh <menu-name>", NamedTextColor.GRAY))),
    PLAYER_IS_NOT_ONLINE(empty()
            .append(text("Player:", NamedTextColor.RED))
            .append(space())
            .append(text("<player>", NamedTextColor.WHITE))
            .append(space())
            .append(text("is not online!", NamedTextColor.RED))
    ),
    PLAYER_IS_EXEMPT(
            text("<player>", NamedTextColor.WHITE)
                    .append(space())
                    .append(text("is exempt from placeholder target arguments.", NamedTextColor.GRAY))),

    MUST_SPECIFY_PLAYER(text("You must specify a player to open a menu for!", NamedTextColor.RED)),
    WRONG_ACTION_TYPE(text("Action type specified does not exist!", NamedTextColor.RED)),
    CHANCE_FAIL(text("The chance for this action determined the action should not execute!", NamedTextColor.RED)),

    ACTION_TO_BE_EXECUTED(text("Action set to be executed in", NamedTextColor.GREEN)
            .append(space())
            .append(text("<amount>"))
            .append(space())
            .append(text("ticks."))),
    ACTION_EXECUTED_FOR(text("Action executed for player:", NamedTextColor.GREEN)
            .append(space())
            .append(text("<player>"))),

    RELOAD_FAIL(text("Errors detected in config.yml. Failed to reload.", NamedTextColor.RED)),
    RELOAD_SUCCESS(PLUGIN_TITLE.message
            .append(space())
            .append(text("successfully reloaded!", NamedTextColor.GREEN))),

    INVALID_MENU(text("Could not find menu:", NamedTextColor.RED)
            .append(space())
            .append(text("<menu>", NamedTextColor.GOLD))
            .append(text(".", NamedTextColor.RED))),
    MENU_RELOADED(text("<menu>", NamedTextColor.GOLD)
            .append(space())
            .append(text("menu successfully reloaded!", NamedTextColor.GREEN))),
    MENU_NOT_RELOADED(text("<menu>", NamedTextColor.GOLD)
            .append(space())
            .append(text("menu could not be reloaded!", NamedTextColor.RED))),
    MENU_LOADED(text("<amount> menu loaded...", NamedTextColor.YELLOW)),
    MENUS_LOADED(text("<amount> menus loaded...", NamedTextColor.YELLOW)),
    MENU_REFRESHED(text("<menu>", NamedTextColor.GOLD)
        .append(space())
        .append(text("menu refreshed for <amount> players...", NamedTextColor.YELLOW))
    ),

    DUMP_FAILED(text("Failed to create and post dump!", NamedTextColor.RED)),

    DUMP_SUCCESS(text("Dump created successfully! Find it at: ", NamedTextColor.GREEN)),

    UPDATE_AVAILABLE(text("An update for", NamedTextColor.GREEN)
            .append(space())
            .append(Messages.PLUGIN_TITLE.message())
            .append(space())
            .append(text("is available. Version", NamedTextColor.GREEN))
            .append(space())
            .append(text("<latest-version>", NamedTextColor.WHITE))
            .append(text(", You are running", NamedTextColor.GREEN))
            .append(space())
            .append(text("<current-version>", NamedTextColor.WHITE))
            .append(newline())
            .append(text("Download the latest version at:", NamedTextColor.GREEN))
            .append(space())
            .append(text("https://www.spigotmc.org/resources/deluxemenus.11734/", NamedTextColor.WHITE))

    ),

    // Meta related messages
    WRONG_USAGE_META_COMMAND(WRONG_USAGE_BASE.message
            .append(text("/dm meta <player> <set/remove/add/subtract/switch/list/show>", NamedTextColor.GRAY))),

    WRONG_USAGE_META_LIST_COMMAND(WRONG_USAGE_BASE.message
            .append(text("/dm meta <player> list <type> [page]", NamedTextColor.GRAY))),
    WRONG_USAGE_META_SWITCH_COMMAND(WRONG_USAGE_BASE.message
            .append(text("/dm meta <player> switch <key>", NamedTextColor.GRAY))),
    WRONG_USAGE_META_SHOW_COMMAND(WRONG_USAGE_BASE.message
            .append(text("/dm meta <player> show <key> <type>", NamedTextColor.GRAY))),
    WRONG_USAGE_META_REMOVE_COMMAND(WRONG_USAGE_BASE.message
            .append(text("/dm meta <player> remove <key> <type>", NamedTextColor.GRAY))),
    WRONG_USAGE_META_SET_COMMAND(WRONG_USAGE_BASE.message
            .append(text("/dm meta <player> set <key> <type> <value>", NamedTextColor.GRAY))),
    WRONG_USAGE_META_ADD_COMMAND(WRONG_USAGE_BASE.message
            .append(text("/dm meta <player> add <key> <type> <value>", NamedTextColor.GRAY))),
    WRONG_USAGE_META_SUBTRACT_COMMAND(WRONG_USAGE_BASE.message
            .append(text("/dm meta <player> subtract <key> <type> <value>", NamedTextColor.GRAY))),

    META_NOT_SUPPORTED(text("Meta is not supported on this server version!", NamedTextColor.RED)),
    META_TYPE_UNSUPPORTED(text("Unsupported meta type ", NamedTextColor.RED)
            .append(text("<type>", NamedTextColor.GOLD))
            .append(text("! Supported values are: ", NamedTextColor.RED))
            .append(text(String.join(", ", DataType.getSupportedTypeNames()), NamedTextColor.GOLD))),
    META_KEY_INVALID(text("An invalid meta key was provided: '", NamedTextColor.RED)
            .append(text("<key>", NamedTextColor.GOLD))
            .append(text("'!", NamedTextColor.RED))),
    META_ADD_TYPE_MISMATCH(text("Only NUMBERS can be added, and '", NamedTextColor.RED)
            .append(text("<value>", NamedTextColor.GOLD))
            .append(text("' is not a number!", NamedTextColor.RED))),
    META_SUBTRACT_TYPE_MISMATCH(text("Only NUMBERS can be subtracted, and '", NamedTextColor.RED)
            .append(text("<value>", NamedTextColor.GOLD))
            .append(text("' is not a number!", NamedTextColor.RED))),
    META_VALUE_TYPE_MISMATCH(text("Given value '", NamedTextColor.RED)
            .append(text("<value>", NamedTextColor.GOLD))
            .append(text("' does not match the given type '", NamedTextColor.RED))
            .append(text("<type>", NamedTextColor.GOLD))
            .append(text("'!", NamedTextColor.RED))),
    META_EXISTENT_VALUE_WRONG_TYPE(text("Given key '", NamedTextColor.RED)
            .append(text("<key>", NamedTextColor.GOLD))
            .append(text("' stores a value with a different type!", NamedTextColor.RED))),
    NO_META_VALUE(text("Could not find a meta value with key ", NamedTextColor.RED)
            .append(text("<key>", NamedTextColor.GOLD))
            .append(text(" and type ", NamedTextColor.RED))
            .append(text("<type>", NamedTextColor.GOLD))
            .append(text(" for ", NamedTextColor.RED))
            .append(text("<player>", NamedTextColor.GOLD))),
    NO_META_VALUES(text("Could not find any meta values with type ", NamedTextColor.RED)
            .append(text("<type>", NamedTextColor.GOLD))
            .append(text(" for ", NamedTextColor.RED))
            .append(text("<player>", NamedTextColor.GOLD))),
    META_VALUE_FOUND(text("Meta value with key ", NamedTextColor.GRAY)
            .append(text("<key>", NamedTextColor.GREEN))
            .append(text(" and type ", NamedTextColor.GRAY))
            .append(text("<type>", NamedTextColor.GREEN))
            .append(text(" for ", NamedTextColor.GRAY))
            .append(text("<player>", NamedTextColor.GREEN))
            .append(text(": ", NamedTextColor.GRAY))
            .append(text("<value>", NamedTextColor.GREEN))),
    META_VALUE_SET(text("Meta value with key ", NamedTextColor.GRAY)
            .append(text("<key>", NamedTextColor.GREEN))
            .append(text(" and type ", NamedTextColor.GRAY))
            .append(text("<type>", NamedTextColor.GREEN))
            .append(text(" for ", NamedTextColor.GRAY))
            .append(text("<player>", NamedTextColor.GREEN))
            .append(text(" set to: ", NamedTextColor.GRAY))
            .append(text("<value>", NamedTextColor.GREEN))),
    META_VALUE_REMOVED(text("Meta value with key ", NamedTextColor.GRAY)
            .append(text("<key>", NamedTextColor.GREEN))
            .append(text(" and type ", NamedTextColor.GRAY))
            .append(text("<type>", NamedTextColor.GREEN))
            .append(text(" for ", NamedTextColor.GRAY))
            .append(text("<player>", NamedTextColor.GREEN))
            .append(text(" removed.", NamedTextColor.GRAY))),
    META_VALUE_ADDED(text("Added ", NamedTextColor.GRAY)
            .append(text("<value>", NamedTextColor.GREEN))
            .append(text(" to the meta value with key ", NamedTextColor.GRAY))
            .append(text("<key>", NamedTextColor.GREEN))
            .append(text(" and type ", NamedTextColor.GRAY))
            .append(text("<type>", NamedTextColor.GREEN))
            .append(text(" for ", NamedTextColor.GRAY))
            .append(text("<player>", NamedTextColor.GREEN))
            .append(text(". New value: ", NamedTextColor.GRAY))
            .append(text("<new-value>", NamedTextColor.GREEN))),
    META_VALUE_SUBTRACTED(text("Subtracted ", NamedTextColor.GRAY)
            .append(text("<value>", NamedTextColor.GREEN))
            .append(text(" from the meta value with key ", NamedTextColor.GRAY))
            .append(text("<key>", NamedTextColor.GREEN))
            .append(text(" and type ", NamedTextColor.GRAY))
            .append(text("<type>", NamedTextColor.GREEN))
            .append(text(" for ", NamedTextColor.GRAY))
            .append(text("<player>", NamedTextColor.GREEN))
            .append(text(". New value: ", NamedTextColor.GRAY))
            .append(text("<new-value>", NamedTextColor.GREEN))),
    META_VALUE_SWITCHED(text("Meta value with key ", NamedTextColor.GRAY)
            .append(text("<key>", NamedTextColor.GREEN))
            .append(text(" for ", NamedTextColor.GRAY))
            .append(text("<player>", NamedTextColor.GREEN))
            .append(text(" switched to: ", NamedTextColor.GRAY))
            .append(text("<new-value>", NamedTextColor.GREEN)));


    private static final Map<String, Messages> BY_NAME = new HashMap<>();

    static {
        for (Messages message : values()) {
            BY_NAME.put(message.name().toLowerCase(Locale.ROOT), message);
        }
    }

    private final Component message;

    Messages(final @NotNull Component message) {
        this.message = message;
    }

    public static @Nullable Messages getMessage(@NotNull final String name) {
        return BY_NAME.getOrDefault(name.toLowerCase(Locale.ROOT), null);
    }

    public @NotNull Component message() {
        return message;
    }
}
