package com.extendedclip.deluxemenus.commands;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.action.ActionType;
import com.extendedclip.deluxemenus.action.ClickAction;
import com.extendedclip.deluxemenus.action.ClickActionTask;
import com.extendedclip.deluxemenus.config.DeluxeMenusConfig;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.menu.MenuHolder;
import com.extendedclip.deluxemenus.utils.DumpUtils;
import com.extendedclip.deluxemenus.utils.Messages;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;

public class DeluxeMenusCommands implements CommandExecutor {

    private static final TextReplacementConfig.Builder PLAYER_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<player>");
    private static final TextReplacementConfig.Builder VERSION_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<version>");
    private static final TextReplacementConfig.Builder AUTHORS_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<authors>");
    private static final TextReplacementConfig.Builder AMOUNT_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<amount>");
    private static final TextReplacementConfig.Builder MENU_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<menu>");

    private final DeluxeMenus plugin;

    public DeluxeMenusCommands(final @NotNull DeluxeMenus plugin) {
        this.plugin = plugin;
        this.plugin.getCommand("deluxemenus").setExecutor(this);
    }

    @Override
    public boolean onCommand(
            final @NotNull CommandSender sender,
            final @NotNull Command command,
            final @NotNull String label,
            final @NotNull String[] args
    ) {

        if (args.length == 0) {
            plugin.sms(
                    sender,
                    Messages.PLUGIN_VERSION.message()
                            .replaceText(VERSION_REPLACER_BUILDER.replacement(plugin.getDescription().getVersion()).build())
                            .replaceText(AUTHORS_REPLACER_BUILDER.replacement(
                                    plugin.getDescription()
                                            .getAuthors()
                                            .stream()
                                            .map(author -> text(author, NamedTextColor.WHITE))
                                            .collect(Component.toComponent(text(", ", NamedTextColor.GRAY)))).build()
                            )
            );
            return true;
        }

        if (args[0].equalsIgnoreCase("help")) {
            if (sender.hasPermission("deluxemenus.admin")) {
                plugin.sms(sender, Messages.HELP_ADMIN);
                return true;
            }

            plugin.sms(sender, Messages.HELP);
            return true;

        } else if (args[0].equalsIgnoreCase("dump")) {
            if (!sender.hasPermission("deluxemenus.admin")) {
                plugin.sms(sender, Messages.NO_PERMISSION);
                return true;
            }

            if (args.length != 2) {
                plugin.sms(sender, Messages.WRONG_USAGE_DUMP_COMMAND);
                return true;
            }

            String dump = "";
            try {
                dump = DumpUtils.createDump(plugin, args[1]);
            } catch (final RuntimeException ignored) {
            }

            if (dump.isBlank()) {
                plugin.sms(sender, Messages.DUMP_FAILED);
                return true;
            }

            DumpUtils.postDump(dump).whenComplete((result, error) -> {
                if (error != null) {
                    DeluxeMenus.printStacktrace(
                            "Something went wrong while trying to create and post a dump!",
                            error
                    );
                    plugin.sms(sender, Messages.DUMP_FAILED);
                    return;
                }

                final var link = text(DumpUtils.URL + result)
                        .clickEvent(ClickEvent.openUrl(DumpUtils.URL + result));

                plugin.sms(sender, Messages.DUMP_SUCCESS.message().append(link));
            });

            return true;
        } else if (args[0].equalsIgnoreCase("execute")) {
            if (!sender.isOp()) {
                plugin.sms(sender, Messages.NO_PERMISSION);
                return true;
            }

            if (args.length < 3) {
                plugin.sms(sender, Messages.WRONG_USAGE_EXECUTE_COMMAND);
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                plugin.sms(
                        sender,
                        Messages.PLAYER_IS_NOT_ONLINE.message().replaceText(PLAYER_REPLACER_BUILDER.replacement(args[1]).build())
                );
                return true;
            }

            String executable = String.join(" ", Arrays.asList(args).subList(2, args.length));

            ActionType type = ActionType.getByStart(executable);

            if (type == null) {
                plugin.sms(sender, Messages.WRONG_ACTION_TYPE);
                return true;
            }

            executable = executable.replaceFirst(Pattern.quote(type.getIdentifier()), "").trim();

            ClickAction action = new ClickAction(type, executable);

            Matcher d = DeluxeMenusConfig.DELAY_MATCHER.matcher(executable);

            if (d.find()) {
                action.setDelay(d.group(1));
                executable = executable.replaceFirst(Pattern.quote(d.group()), "");
            }

            Matcher ch = DeluxeMenusConfig.CHANCE_MATCHER.matcher(executable);

            if (ch.find()) {
                action.setChance(ch.group(1));
                executable = executable.replaceFirst(Pattern.quote(ch.group()), "");
            }

            action.setExecutable(executable);

            MenuHolder holder = Menu.getMenuHolder(target).orElse(new MenuHolder(target));

            if (!action.checkChance(holder)) {
                plugin.sms(sender, Messages.CHANCE_FAIL);
                return true;
            }

            final ClickActionTask actionTask = new ClickActionTask(
                    plugin,
                    target.getUniqueId(),
                    action.getType(),
                    action.getExecutable(),
                    holder.getTypedArgs(),
                    true,
                    true
                );

            if (action.hasDelay()) {
                actionTask.runTaskLater(plugin, action.getDelay(holder));

                plugin.sms(
                        sender,
                        Messages.ACTION_TO_BE_EXECUTED.message().replaceText(
                                AMOUNT_REPLACER_BUILDER.replacement(String.valueOf(action.getDelay(holder))).build())
                );
                return true;
            }

            actionTask.runTask(plugin);

            plugin.sms(
                    sender,
                    Messages.ACTION_EXECUTED_FOR.message().replaceText(
                            PLAYER_REPLACER_BUILDER.replacement(target.getName()).build())
            );
            return true;

        } else if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("deluxemenus.reload")) {
                plugin.sms(sender, Messages.NO_PERMISSION);
                return true;
            }

            if (plugin.getConfiguration().checkConfig(null, "config.yml", false) == null) {
                plugin.sms(sender, Messages.RELOAD_FAIL);
                return true;
            }

            if (args.length > 1) {
                if (Menu.getMenuByName(args[1]).isEmpty()) {
                    plugin.sms(
                            sender,
                            Messages.INVALID_MENU.message().replaceText(
                                    MENU_REPLACER_BUILDER.replacement(args[1]).build())
                    );
                    return true;
                }

                Menu.unload(args[1]);

                if (plugin.getConfiguration().loadGUIMenu(args[1])) {
                    plugin.sms(
                            sender,
                            Messages.MENU_RELOADED.message().replaceText(
                                    MENU_REPLACER_BUILDER.replacement(args[1]).build())
                    );
                    return true;
                }


                plugin.sms(
                        sender,
                        Messages.MENU_NOT_RELOADED.message().replaceText(
                                MENU_REPLACER_BUILDER.replacement(args[1]).build())
                );
                return true;

            }

            plugin.clearCaches();
            plugin.reloadConfig();
            plugin.saveConfig();
            DeluxeMenus.debugLevel(plugin.getConfiguration().debugLevel());
            Menu.unload();
            plugin.getConfiguration().loadGUIMenus();
            plugin.sms(sender, Messages.RELOAD_SUCCESS);

            int gLoaded = Menu.getLoadedMenuSize();

            if (gLoaded == 1) {
                plugin.sms(
                        sender,
                        Messages.MENU_LOADED.message().replaceText(
                                AMOUNT_REPLACER_BUILDER.replacement(String.valueOf(gLoaded)).build())
                );
            } else {
                plugin.sms(
                        sender,
                        Messages.MENUS_LOADED.message().replaceText(
                                AMOUNT_REPLACER_BUILDER.replacement(String.valueOf(gLoaded)).build())
                );
            }
            return true;

        } else if (args[0].equalsIgnoreCase("list")) {

            if (!sender.hasPermission("deluxemenus.list")) {
                plugin.sms(sender, Messages.NO_PERMISSION);
                return true;
            }

            Collection<Menu> menus = Menu.getAllMenus();

            if (menus.isEmpty()) {
                plugin.sms(
                        sender,
                        Messages.MENUS_LOADED.message().replaceText(
                                AMOUNT_REPLACER_BUILDER.replacement("There are no").build())
                );
                return true;
            }

            // Component builder start
            final TextComponent.Builder list = text();

            // Header
            list.append(text("The following " + menus.size() + " menus are loaded on the server:", NamedTextColor.GOLD)
                    .append(newline()));

            if (sender instanceof ConsoleCommandSender) {
                list.append(newline());

                // Add all the menus using streams
                final var menusList = menus.stream().map(menu -> menu.options().commands().isEmpty()
                        ? text(menu.options().name(), NamedTextColor.DARK_AQUA)
                        .append(text(" - ", NamedTextColor.GRAY))
                        .append(text("No menu command", NamedTextColor.RED))

                        : text(menu.options().name(), NamedTextColor.DARK_AQUA)
                        .append(text(" - ", NamedTextColor.GRAY))
                        .append(text("/" + menu.options().commands().get(0), NamedTextColor.GREEN))
                ).collect(Component.toComponent(text(" | ", NamedTextColor.WHITE)));

                plugin.sms(sender, list.append(menusList).build());
                return true;
            }

            // Inform they can hover
            list.append(text("**Hover menu name for more info**", NamedTextColor.GRAY));

            // Extra space below
            list.append(newline()).append(newline());

            // Add all the menus using streams
            final var menusList = menus.stream().map(menu -> menu.options().commands().isEmpty()
                    ? text(menu.options().name(), NamedTextColor.DARK_AQUA).hoverEvent(
                    HoverEvent.showText(text("No open command", NamedTextColor.GOLD)))
                    : text(menu.options().name(), NamedTextColor.DARK_AQUA).hoverEvent(
                            HoverEvent.showText(text("Open Command: ", NamedTextColor.GOLD)
                                    .append(text("/" + menu.options().commands().get(0), NamedTextColor.YELLOW))))
                    .clickEvent(
                            ClickEvent.suggestCommand("/" + menu.options().commands().get(0)))
            ).collect(Component.toComponent(text(", ", NamedTextColor.WHITE)));

            list.append(menusList);

            plugin.sms(sender, list.build());
            return true;

        } else if (args[0].equalsIgnoreCase("open")) {

            if (!sender.hasPermission("deluxemenus.open")) {
                plugin.sms(sender, Messages.NO_PERMISSION);
                return true;
            }

            boolean player = (sender instanceof Player);

            if (args.length < 2) {
                plugin.sms(sender, Messages.WRONG_USAGE_OPEN_COMMAND);
                return true;
            }

            if (Menu.getAllMenus().isEmpty()) {
                plugin.sms(
                        sender,
                        Messages.MENUS_LOADED.message().replaceText(
                                AMOUNT_REPLACER_BUILDER.replacement("There are no").build())
                );
                return true;
            }

            Player viewer;
            String placeholderPlayer = null;

            if (args.length == 3 && args[2].startsWith("-p:")) {
                if (!sender.hasPermission("deluxemenus.placeholdersfor")) {
                    plugin.sms(sender, Messages.NO_PERMISSION_PLAYER_ARGUMENT);
                    return true;
                }

                placeholderPlayer = args[2].replace("-p:", "");

            } else if (args.length >= 4 && args[3].startsWith("-p:")) {
                if (!sender.hasPermission("deluxemenus.placeholdersfor")) {
                    plugin.sms(sender, Messages.NO_PERMISSION_PLAYER_ARGUMENT);
                    return true;
                }

                placeholderPlayer = args[3].replace("-p:", "");
            }

            if (args.length >= 3) {
                if (placeholderPlayer == null) {
                    if (player && !sender.hasPermission("deluxemenus.open.others")) {
                        plugin.sms(sender, Messages.NO_PERMISSION);
                        return true;
                    }

                    viewer = Bukkit.getPlayer(args[2]);

                } else {
                    if (args.length >= 4) {
                        if (!sender.hasPermission("deluxemenus.open.others")) {
                            plugin.sms(sender, Messages.NO_PERMISSION);
                            return true;
                        }

                        viewer = Bukkit.getPlayer(args[2]);

                    } else {
                        if (!player) {
                            plugin.sms(sender, Messages.MUST_SPECIFY_PLAYER);
                            return true;
                        }

                        viewer = (Player) sender;
                    }
                }

            } else {
                if (!player) {
                    plugin.sms(sender, Messages.MUST_SPECIFY_PLAYER);
                    return true;
                }

                viewer = (Player) sender;
            }

            if (viewer == null) {
                plugin.sms(
                        sender,
                        Messages.PLAYER_IS_NOT_ONLINE.message().replaceText(PLAYER_REPLACER_BUILDER.replacement(args[2]).build())
                );
                return true;
            }

            Player placeholder = null;

            if (placeholderPlayer != null) {
                placeholder = Bukkit.getPlayer(placeholderPlayer);

                if (placeholder == null) {
                    plugin.sms(
                            sender,
                            Messages.PLAYER_IS_NOT_ONLINE.message().replaceText(PLAYER_REPLACER_BUILDER.replacement(placeholderPlayer).build())
                    );
                    return true;

                } else {
                    if (placeholder.hasPermission("deluxemenus.placeholdersfor.exempt")) {
                        plugin.sms(
                                sender,
                                Messages.PLAYER_IS_EXEMPT.message().replaceText(PLAYER_REPLACER_BUILDER.replacement(placeholderPlayer).build())
                        );

                        return true;
                    }
                }
            }

            Optional<Menu> menu = Menu.getMenuByName(args[1]);

            if (menu.isEmpty()) {
                plugin.sms(
                        sender,
                        Messages.INVALID_MENU.message().replaceText(
                                MENU_REPLACER_BUILDER.replacement(args[1]).build())
                );
                return true;
            }

            menu.get().openMenu(viewer, null, placeholder);
            return true;

        } else {
            plugin.sms(sender, Messages.WRONG_USAGE);
        }
        return true;
    }
}
