package com.extendedclip.deluxemenus.command.subcommand;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.utils.Messages;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.kyori.adventure.text.Component.text;

public class WebEditorCommand extends SubCommand {

    private static final String WEB_EDITOR_PERMISSION = "deluxemenus.webeditor";

    public WebEditorCommand(final @NotNull DeluxeMenus plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getName() {
        return "webeditor";
    }

    @Override
    public void execute(final @NotNull CommandSender sender, final @NotNull List<String> arguments) {
        if (!sender.hasPermission(WEB_EDITOR_PERMISSION)) {
            plugin.sms(sender, Messages.NO_PERMISSION);
            return;
        }

        if (arguments.isEmpty()) {
            plugin.sms(sender, Messages.WRONG_USAGE);
            return;
        }

        final Optional<Menu> optionalMenu = Menu.getMenuByName(arguments.get(0));
        if (optionalMenu.isEmpty()) {
            plugin.sms(sender, Messages.INVALID_MENU.message().replaceText(MENU_REPLACER_BUILDER.replacement(arguments.get(0)).build()));
            return;
        }

        final int port = arguments.size() >= 2 ? parsePort(arguments.get(1)) : 8765;
        try {
            final String url = plugin.getWebEditorServer().createSession(optionalMenu.get(), port);
            plugin.sms(sender, text("Web editor link: ", NamedTextColor.GREEN)
                    .append(text(url, NamedTextColor.YELLOW).clickEvent(ClickEvent.openUrl(url))));
        } catch (final IOException exception) {
            plugin.printStacktrace("Failed to start web editor.", exception);
            plugin.sms(sender, text("Failed to start web editor.", NamedTextColor.RED));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull List<String> arguments) {
        if (!sender.hasPermission(WEB_EDITOR_PERMISSION)) {
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
            return complete(Menu.getAllMenuNames(), arguments.get(1));
        }

        return null;
    }

    private int parsePort(final @NotNull String input) {
        try {
            return Integer.parseInt(input);
        } catch (final NumberFormatException exception) {
            return 8765;
        }
    }

    private @NotNull List<String> complete(final @NotNull Collection<String> values, final @NotNull String argument) {
        final String lowerArgument = argument.toLowerCase(Locale.ROOT);
        return values.stream()
                .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(lowerArgument))
                .collect(Collectors.toList());
    }
}
