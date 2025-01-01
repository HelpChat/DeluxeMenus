package com.extendedclip.deluxemenus.command.subcommand;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.utils.Messages;
import com.google.common.primitives.Ints;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;

public class ListCommand extends SubCommand {

    public ListCommand(final @NotNull DeluxeMenus plugin) {
        super(plugin);
    }

    @Override
    public void execute(final @NotNull CommandSender sender, final @NotNull List<String> args) {
        if (!sender.hasPermission("deluxemenus.list")) {
            plugin.sms(sender, Messages.NO_PERMISSION);
            return;
        }

        if (!args.isEmpty() && args.get(0).equalsIgnoreCase("all")) {
            final Collection<Menu> menus = Menu.getAllMenus();
            if (menus.isEmpty()) {
                plugin.sms(sender, Messages.MENUS_LOADED.message().replaceText(AMOUNT_REPLACER_BUILDER.replacement("There are no").build()));
                return;
            }

            sendSimpleMenuList(sender, Menu.getAllMenus());
            return;
        }

        final Map<String, List<Menu>> menus = Menu.getPathSortedMenus();
        if (menus.isEmpty() || menus.values().stream().allMatch(List::isEmpty)) {
            plugin.sms(sender, Messages.MENUS_LOADED.message().replaceText(AMOUNT_REPLACER_BUILDER.replacement("There are no").build()));
            return;
        }

        final List<Menu> configMenus = menus.remove("config");
        sendPaginatedMenuList(sender, menus, configMenus == null ? Collections.emptyList() : configMenus, args);
    }

    private void sendSimpleMenuList(final @NotNull CommandSender sender, final @NotNull Collection<Menu> menus) {
        final TextComponent.Builder list = text();
        list.append(text("The following " + menus.size() + " menus are loaded on the server:", NamedTextColor.GOLD).append(newline()));

        if (sender instanceof ConsoleCommandSender) {
            list.append(newline());

            final Component menusList = menus.stream().map(menu -> {
                final String menuCommand = getMenuDisplayCommand(menu);
                return menuCommand == null
                    ? text(menu.options().name(), NamedTextColor.DARK_AQUA).append(text(" - ", NamedTextColor.GRAY)).append(text("No menu command", NamedTextColor.RED))
                    : text(menu.options().name(), NamedTextColor.DARK_AQUA).append(text(" - ", NamedTextColor.GRAY)).append(text(menuCommand, NamedTextColor.GREEN));
            }).collect(Component.toComponent(text(" | ", NamedTextColor.WHITE)));

            plugin.sms(sender, list.append(menusList).build());
            return;
        }

        list.append(text("**Hover menu name for more info**", NamedTextColor.GRAY)).append(newline()).append(newline());

        final var menusList = menus.stream().map(menu -> {
            final String menuCommand = getMenuDisplayCommand(menu);

            return menuCommand == null
                    ? text(menu.options().name(), NamedTextColor.DARK_AQUA).hoverEvent(HoverEvent.showText(text("No open command", NamedTextColor.GOLD)))
                    : text(menu.options().name(), NamedTextColor.DARK_AQUA).hoverEvent(HoverEvent.showText(text("Open Command: ", NamedTextColor.GOLD).append(text(menuCommand, NamedTextColor.YELLOW)))).clickEvent(ClickEvent.suggestCommand(menuCommand));
        }).collect(Component.toComponent(text(", ", NamedTextColor.WHITE)));

        list.append(menusList);
        plugin.sms(sender, list.build());
    }

    private void sendPaginatedMenuList(final @NotNull CommandSender sender, final @NotNull Map<String, List<Menu>> menus,
                                       final @NotNull List<Menu> configMenus, final @NotNull List<String> args) {
        final int totalMenusCount = configMenus.size() + menus.values().stream().mapToInt(List::size).sum();

        Integer page = null;
        if (totalMenusCount > plugin.getGeneralConfig().menusListPageSize() && !args.isEmpty()) {
            page = Ints.tryParse(args.get(0));
        }

        final int maxPages = (int) Math.ceil((double) totalMenusCount / plugin.getGeneralConfig().menusListPageSize());

        if (page == null || page < 1) {
            page = 1;
        }

        if (page > maxPages) {
            page = maxPages;
        }

        final Map<String, List<Menu>> paginatedMenus = getPaginatedMenus(
                menus,
                configMenus.stream().collect(TreeMap::new, (map, menu) -> map.put(menu.options().name(), menu), TreeMap::putAll),
                page,
                plugin.getGeneralConfig().menusListPageSize()
        );

        final int pageMenusCount = paginatedMenus.values().stream().mapToInt(List::size).sum();
        final Map<String, Object> pageMenusTree = convertMenusToTree(paginatedMenus);

        final TextComponent.Builder list = text();
        list.append(text("Page " + page + "/" + maxPages + " - " + pageMenusCount + " menus:", NamedTextColor.GOLD).append(newline()));

        if (sender instanceof ConsoleCommandSender) {
            final var menuList = createMenuListForConsole(pageMenusTree, 0);

            list.append(newline()).append(menuList).append(newline()).append(text("Use /dm list <page> to view more menus", NamedTextColor.GRAY));
            plugin.sms(sender, list.build());
            return;
        }

        list.append(text("**Hover menu name for more info**", NamedTextColor.GRAY));
        list.append(newline()).append(newline());

        final var menuList = createMenuListForPlayer(pageMenusTree, 0);

        list.append(menuList);

        if (page > 1 || page < maxPages) {
            list.append(newline());

            if (page > 1) {
                list.append(text("<< Previous", NamedTextColor.GOLD)
                        .hoverEvent(HoverEvent.showText(
                                text("Click to go to the previous page", NamedTextColor.GRAY)
                                        .append(newline()).append(newline())
                                        .append(text("Executes: /dm list " + (page - 1), NamedTextColor.GRAY))
                        ))
                        .clickEvent(ClickEvent.runCommand("/dm list " + (page - 1))));
                if (page < maxPages) {
                    list.append(text(" | ", NamedTextColor.GREEN));
                }
            }

            if (page < maxPages) {
                list.append(text("Next >>", NamedTextColor.GOLD)
                        .hoverEvent(HoverEvent.showText(
                                text("Click to go to the next page", NamedTextColor.GRAY)
                                        .append(newline()).append(newline())
                                        .append(text("Executes: /dm list " + (page + 1), NamedTextColor.GRAY))
                        ))
                        .clickEvent(ClickEvent.runCommand("/dm list " + (page + 1))));
            }
        }

        plugin.sms(sender, list.build());
    }

    private Map<String, List<Menu>> getPaginatedMenus(final Map<String, List<Menu>> menus,
                                                      final @NotNull Map<String, Menu> configMenus,
                                                      final int page,
                                                      final int pageSize
    ) {
        final Map<String, List<Menu>> paginatedMenus = new LinkedHashMap<>();
        final int start = (page - 1) * pageSize;
        final int end = start + pageSize;

        int count = 0;
        int i = 0;
        for (final Map.Entry<String, Menu> entry : configMenus.entrySet()) {
            if (count >= pageSize || i >= end) {
                break;
            }

            if (i < start) {
                i++;
                continue;
            }

            paginatedMenus.computeIfAbsent("config", k -> new ArrayList<>()).add(entry.getValue());
            count++;
            i++;
        }

        for (final Map.Entry<String, List<Menu>> entry : menus.entrySet()) {
            if (count >= pageSize || i >= end) {
                break;
            }

            for (final Menu menu : entry.getValue()) {
                if (count >= pageSize || i >= end) {
                    break;
                }

                if (i < start) {
                    i++;
                    continue;
                }

                paginatedMenus.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(menu);
                count++;
                i++;
            }
        }

        return paginatedMenus;
    }

    @SuppressWarnings("unchecked")
    private Component createMenuListForConsole(final Map<String, Object> tree, int tabs) {
        final TextComponent.Builder list = text();

        for (final Map.Entry<String, Object> entry : tree.entrySet()) {
            if (entry.getValue() instanceof List) {
                for (final Menu menu : (List<Menu>) entry.getValue()) {
                    list.append(text("  ".repeat(tabs) + "- " + entry.getKey(), NamedTextColor.DARK_AQUA).append(text(" - ", NamedTextColor.GRAY)).append(text(menu.options().name(), NamedTextColor.GREEN)).append(newline()));
                }
            } else {
                list.append(text("  ".repeat(tabs) + "|- " + entry.getKey(), NamedTextColor.DARK_AQUA)).append(newline());
                list.append(createMenuListForConsole((Map<String, Object>) entry.getValue(), tabs + 1));
            }
        }

        return list.build();
    }

    @SuppressWarnings("unchecked")
    private Component createMenuListForPlayer(final Map<String, Object> tree, int tabs) {
        final TextComponent.Builder list = text();

        for (final Map.Entry<String, Object> entry : tree.entrySet()) {
            if (!(entry.getValue() instanceof List)) {
                list.append(text("  ".repeat(tabs) + "|-" + entry.getKey(), NamedTextColor.DARK_AQUA)).append(newline());
                list.append(createMenuListForPlayer((Map<String, Object>) entry.getValue(), tabs + 1));
                continue;
            }

            for (final Menu menu : (List<Menu>) entry.getValue()) {
                final String menuCommand = getMenuDisplayCommand(menu);

                list.append(
                        text("  ".repeat(tabs) + "- " + entry.getKey(), NamedTextColor.DARK_AQUA).append(text(" - ", NamedTextColor.GRAY)).append(text(menu.options().name(), NamedTextColor.GREEN))
                        .hoverEvent(HoverEvent.showText(menuCommand != null ? text("Open Command: ", NamedTextColor.GOLD).append(text(menuCommand, NamedTextColor.YELLOW)) : text("No open command", NamedTextColor.GOLD)))
                        .clickEvent(ClickEvent.suggestCommand((menuCommand != null ? menuCommand : "No open command")))
                ).append(newline());
            }
        }

        return list.build();
    }

    private Map<String, Object> convertMenusToTree(final Map<String, List<Menu>> menus) {
        final Map<String, Object> tree = new LinkedHashMap<>();

        for (final Map.Entry<String, List<Menu>> entry : menus.entrySet()) {
            final String[] path = entry.getKey().split("/");
            addMenuToTreeRecursively(tree, List.of(path), entry.getValue(), 0);
        }

        return tree;
    }

    @SuppressWarnings("unchecked")
    private void addMenuToTreeRecursively(final Map<String, Object> tree, final List<String> path, final List<Menu> menus, final int step) {
        if (step < 0 || step >= path.size()) {
            return;
        }

        if (step == path.size() - 1) {
            if (!tree.containsKey(path.get(step))) {
                tree.put(path.get(step), new ArrayList<>(menus));
            } else {
                final List<Menu> list = (List<Menu>) tree.get(path.get(step));
                list.addAll(menus);
            }
            return;
        }

        final String value = path.get(step);
        if (!tree.containsKey(value)) {
            tree.put(value, new TreeMap<>());
        }

        addMenuToTreeRecursively((Map<String, Object>) tree.get(value), path, menus, step + 1);
    }

    /**
     * Get the command that can be used to open this menu.
     * The response will be the first command in the list of commands for this menu.
     * If the config option to use admin commands in menus list is enabled, the admin "/dm open" command will be returned.
     * @return The command that can be used to open this menu.
     */
    public @Nullable String getMenuDisplayCommand(final @NotNull Menu menu) {
        final boolean useAdminCommand = this.plugin.getGeneralConfig().useAdminCommandsInMenusList();

        if (useAdminCommand) {
            return "/deluxemenus open " + menu.options().name();
        }

        if (menu.options().commands().isEmpty()) {
            return null;
        }

        return "/" + menu.options().commands().get(0);
    }
}
