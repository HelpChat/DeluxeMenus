package com.extendedclip.deluxemenus.command.subcommand;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.utils.Messages;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.kyori.adventure.text.Component.text;

public class WebEditorCommand extends SubCommand {

    private static final String WEB_EDITOR_PERMISSION = "deluxemenus.webeditor";
    private static final int DEFAULT_PORT = 8765;
    private final String name;

    public WebEditorCommand(final @NotNull DeluxeMenus plugin) {
        this(plugin, "webeditor");
    }

    public WebEditorCommand(final @NotNull DeluxeMenus plugin, final @NotNull String name) {
        super(plugin);
        this.name = name;
    }

    @Override
    public @NotNull String getName() {
        return name;
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

        final Optional<Menu> optionalMenu = findMenu(arguments.get(0));
        if (optionalMenu.isEmpty()) {
            plugin.sms(sender, Messages.INVALID_MENU.message().replaceText(MENU_REPLACER_BUILDER.replacement(arguments.get(0)).build()));
            return;
        }

        final EditorEndpoint endpoint = parseEndpoint(sender, arguments);
        try {
            final String url = plugin.getWebEditorServer().createSession(optionalMenu.get(), endpoint.port, endpoint.host.orElse(null));
            plugin.sms(sender, text("Web editor link: ", NamedTextColor.GREEN)
                    .append(text(url, NamedTextColor.YELLOW).clickEvent(ClickEvent.openUrl(url))));
            if (url.contains("://localhost:")) {
                plugin.sms(sender, text("Remote hosting needs a public host and an open web editor port.", NamedTextColor.GRAY));
            } else {
                plugin.sms(sender, text("The web editor port must be open on your hosting panel. The Minecraft port is separate.", NamedTextColor.GRAY));
            }
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
            return complete(menuNames(), arguments.get(1));
        }

        return null;
    }

    private @NotNull EditorEndpoint parseEndpoint(final @NotNull CommandSender sender, final @NotNull List<String> arguments) {
        int port = DEFAULT_PORT;
        Optional<String> host = virtualHost(sender);

        for (int index = 1; index < arguments.size(); index++) {
            final String argument = arguments.get(index);
            final Optional<EditorEndpoint> optionalEndpoint = parseHostPort(argument);
            if (optionalEndpoint.isPresent()) {
                port = optionalEndpoint.get().port;
                host = optionalEndpoint.get().host;
                continue;
            }

            final Optional<Integer> optionalPort = parsePort(argument);
            if (optionalPort.isPresent()) {
                port = optionalPort.get();
                continue;
            }

            host = Optional.of(argument);
        }

        return new EditorEndpoint(port, host);
    }

    private @NotNull Optional<Integer> parsePort(final @NotNull String input) {
        try {
            final int port = Integer.parseInt(input);
            if (port < 1 || port > 65535) {
                return Optional.empty();
            }

            return Optional.of(port);
        } catch (final NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private @NotNull Optional<EditorEndpoint> parseHostPort(final @NotNull String input) {
        String endpoint = input.trim();
        final int schemeIndex = endpoint.indexOf("://");
        if (schemeIndex >= 0) {
            endpoint = endpoint.substring(schemeIndex + 3);
        }

        final int pathIndex = endpoint.indexOf('/');
        if (pathIndex >= 0) {
            endpoint = endpoint.substring(0, pathIndex);
        }

        if (endpoint.startsWith("[")) {
            final int endIndex = endpoint.indexOf(']');
            if (endIndex <= 0 || endIndex + 2 > endpoint.length() || endpoint.charAt(endIndex + 1) != ':') {
                return Optional.empty();
            }

            final Optional<Integer> port = parsePort(endpoint.substring(endIndex + 2));
            if (port.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(new EditorEndpoint(port.get(), Optional.of(endpoint.substring(0, endIndex + 1))));
        }

        final int colonIndex = endpoint.lastIndexOf(':');
        if (colonIndex <= 0 || colonIndex != endpoint.indexOf(':')) {
            return Optional.empty();
        }

        final Optional<Integer> port = parsePort(endpoint.substring(colonIndex + 1));
        if (port.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new EditorEndpoint(port.get(), Optional.of(endpoint.substring(0, colonIndex))));
    }

    private @NotNull Optional<String> virtualHost(final @NotNull CommandSender sender) {
        if (!(sender instanceof Player)) {
            return Optional.empty();
        }

        try {
            final Method method = sender.getClass().getMethod("getVirtualHost");
            final Object virtualHost = method.invoke(sender);
            if (virtualHost instanceof InetSocketAddress) {
                return Optional.ofNullable(((InetSocketAddress) virtualHost).getHostString());
            }

            return Optional.ofNullable(virtualHost).map(String::valueOf);
        } catch (final ReflectiveOperationException exception) {
            return Optional.empty();
        }
    }

    private @NotNull List<String> complete(final @NotNull Collection<String> values, final @NotNull String argument) {
        final String lowerArgument = argument.toLowerCase(Locale.ROOT);
        return values.stream()
                .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(lowerArgument))
                .collect(Collectors.toList());
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

    private static class EditorEndpoint {
        private final int port;
        private final Optional<String> host;

        private EditorEndpoint(final int port, final @NotNull Optional<String> host) {
            this.port = port;
            this.host = host;
        }
    }
}
