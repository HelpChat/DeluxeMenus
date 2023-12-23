package com.extendedclip.deluxemenus.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
        .append(text("/dm list", NamedTextColor.WHITE))),

    HELP_ADMIN(HELP.message
        .append(newline())
        .append(text(">", NamedTextColor.AQUA))
        .append(space().append(space()))
        .append(text("/dm execute <player> <action>", NamedTextColor.WHITE))
        .append(newline())
        .append(text(">", NamedTextColor.AQUA))
        .append(space().append(space()))
        .append(text("/dm reload [menu]", NamedTextColor.WHITE))),

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

    );

    Messages(final @NotNull Component message) {
        this.message = message;
    }

    private final Component message;

    public @NotNull Component message() {
        return message;
    }

    private static final Map<String, Messages> BY_NAME = new HashMap<>();

    static {
        for (Messages message : values()) {
            BY_NAME.put(message.name().toLowerCase(Locale.getDefault()), message);
        }
    }

    public static @Nullable Messages getMessage(@NotNull final String name) {
        return BY_NAME.getOrDefault(name.toLowerCase(Locale.getDefault()), null);
    }
}
