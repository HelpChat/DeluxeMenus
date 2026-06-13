package com.extendedclip.deluxemenus.command.subcommand;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.hooks.ItemHook;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.menu.MenuHolder;
import com.extendedclip.deluxemenus.menu.MenuItem;
import com.extendedclip.deluxemenus.requirement.RequirementList;
import com.extendedclip.deluxemenus.utils.Messages;
import com.extendedclip.deluxemenus.utils.StringUtils;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;

public class DebugCommand extends SubCommand {

    private static final String DEBUG_COMMAND = "deluxemenus.debug";

    public DebugCommand(final @NotNull DeluxeMenus plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getName() {
        return "debug";
    }

    @Override
    public void execute(final @NotNull CommandSender sender, final @NotNull List<String> arguments) {
        if (!sender.hasPermission(DEBUG_COMMAND)) {
            plugin.sms(sender, Messages.NO_PERMISSION);
            return;
        }

        if (arguments.size() < 2) {
            sendUsage(sender);
            return;
        }

        switch (arguments.get(0).toLowerCase(Locale.ROOT)) {
            case "placeholder":
            case "placeholders":
                debugPlaceholder(sender, arguments);
                break;
            case "hook":
            case "itemhook":
                debugHook(sender, arguments);
                break;
            case "item":
                debugMenuItem(sender, arguments);
                break;
            default:
                sendUsage(sender);
                break;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull List<String> arguments) {
        if (!sender.hasPermission(DEBUG_COMMAND)) {
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
            return complete(List.of("placeholder", "hook", "item"), arguments.get(1));
        }

        final String mode = arguments.get(1).toLowerCase(Locale.ROOT);
        if (arguments.size() == 3) {
            if ("placeholder".equals(mode) || "placeholders".equals(mode)) {
                return getPlayerNameCompletion(arguments.get(2));
            }
            if ("hook".equals(mode) || "itemhook".equals(mode)) {
                return completeHookPrefixes(arguments.get(2));
            }
            if ("item".equals(mode)) {
                return complete(Menu.getAllMenuNames(), arguments.get(2));
            }
        }

        if (arguments.size() == 5 && "item".equals(mode)) {
            return getPlayerNameCompletion(arguments.get(4));
        }

        if (arguments.size() == 4 && ("hook".equals(mode) || "itemhook".equals(mode))) {
            return getPlayerNameCompletion(arguments.get(3));
        }

        return null;
    }

    private void debugPlaceholder(final @NotNull CommandSender sender, final @NotNull List<String> arguments) {
        if (arguments.size() < 3) {
            plugin.sms(sender, usage("/dm debug placeholder <player> <text>"));
            return;
        }

        final Player player = Bukkit.getPlayerExact(arguments.get(1));
        if (player == null) {
            plugin.sms(sender, Messages.PLAYER_IS_NOT_ONLINE.message().replaceText(PLAYER_REPLACER_BUILDER.replacement(arguments.get(1)).build()));
            return;
        }

        final String input = String.join(" ", arguments.subList(2, arguments.size()));
        final String parsed = StringUtils.replacePlaceholders(input, player);

        plugin.sms(sender, header("Placeholder debug")
                .append(row("Player", player.getName()))
                .append(row("Input", input))
                .append(row("Parsed", parsed)));
    }

    private void debugHook(final @NotNull CommandSender sender, final @NotNull List<String> arguments) {
        final String material = arguments.get(1);
        final Optional<ItemHook> optionalHook = findHook(material);
        if (optionalHook.isEmpty()) {
            plugin.sms(sender, header("Hook debug")
                    .append(row("Material", material))
                    .append(row("Result", "No matching item hook prefix")));
            return;
        }

        final Player player = arguments.size() >= 3 ? Bukkit.getPlayerExact(arguments.get(2)) : sender instanceof Player ? (Player) sender : null;
        if (arguments.size() >= 3 && player == null) {
            plugin.sms(sender, Messages.PLAYER_IS_NOT_ONLINE.message().replaceText(PLAYER_REPLACER_BUILDER.replacement(arguments.get(2)).build()));
            return;
        }

        final ItemHook hook = optionalHook.get();
        final String hookArgument = parseHookArgument(material, hook, player);
        final ItemStack itemStack = player == null ? hook.getItem(hookArgument) : hook.getItem(player, hookArgument);

        plugin.sms(sender, header("Hook debug")
                .append(row("Hook", hook.getClass().getSimpleName()))
                .append(row("Prefix", hook.getPrefix()))
                .append(row("Argument", hookArgument))
                .append(row("Item", describeItem(itemStack))));
    }

    private void debugMenuItem(final @NotNull CommandSender sender, final @NotNull List<String> arguments) {
        if (arguments.size() < 4) {
            plugin.sms(sender, usage("/dm debug item <menu> <slot> <player>"));
            return;
        }

        final Optional<Menu> optionalMenu = Menu.getMenuByName(arguments.get(1));
        if (optionalMenu.isEmpty()) {
            plugin.sms(sender, Messages.INVALID_MENU.message().replaceText(MENU_REPLACER_BUILDER.replacement(arguments.get(1)).build()));
            return;
        }

        final int slot;
        try {
            slot = Integer.parseInt(arguments.get(2));
        } catch (final NumberFormatException exception) {
            plugin.sms(sender, usage("/dm debug item <menu> <slot> <player>"));
            return;
        }

        final Player player = Bukkit.getPlayerExact(arguments.get(3));
        if (player == null) {
            plugin.sms(sender, Messages.PLAYER_IS_NOT_ONLINE.message().replaceText(PLAYER_REPLACER_BUILDER.replacement(arguments.get(3)).build()));
            return;
        }

        final Menu menu = optionalMenu.get();
        final TreeMap<Integer, MenuItem> slotItems = menu.getMenuItems().get(slot);
        if (slotItems == null || slotItems.isEmpty()) {
            plugin.sms(sender, header("Menu item debug")
                    .append(row("Menu", menu.options().name()))
                    .append(row("Slot", String.valueOf(slot)))
                    .append(row("Result", "No configured item in this slot")));
            return;
        }

        final MenuItem item = slotItems.firstEntry().getValue();
        final MenuHolder holder = new MenuHolder(plugin, player);
        holder.setMenuName(menu.options().name());
        holder.setPlaceholderPlayer(player);
        holder.parsePlaceholdersInArguments(menu.options().parsePlaceholdersInArguments());
        holder.parsePlaceholdersAfterArguments(menu.options().parsePlaceholdersAfterArguments());

        final ItemStack rendered = item.getItemStack(holder);

        plugin.sms(sender, header("Menu item debug")
                .append(row("Menu", menu.options().name()))
                .append(row("Player", player.getName()))
                .append(row("Slot", String.valueOf(slot)))
                .append(row("Priority", String.valueOf(item.options().priority())))
                .append(row("Material", item.options().material()))
                .append(row("Rendered", describeItem(rendered)))
                .append(row("View requirements", describeRequirementList(item.options().viewRequirements()))));
    }

    private @NotNull Optional<ItemHook> findHook(final @NotNull String material) {
        final String lowerMaterial = material.toLowerCase(Locale.ROOT);
        return plugin.getItemHooks().values()
                .stream()
                .filter(hook -> lowerMaterial.startsWith(hook.getPrefix()))
                .findFirst();
    }

    private @NotNull String parseHookArgument(final @NotNull String material, final @NotNull ItemHook hook, final @Nullable Player player) {
        final String argument = material.substring(hook.getPrefix().length());
        return player == null ? argument : StringUtils.replacePlaceholders(argument, player);
    }

    private @NotNull String describeItem(final @Nullable ItemStack itemStack) {
        if (itemStack == null) {
            return "null";
        }

        final StringBuilder builder = new StringBuilder(itemStack.getType().name())
                .append(" x")
                .append(itemStack.getAmount());

        if (itemStack.getType() == Material.STONE) {
            builder.append(" (fallback possible)");
        }

        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null && itemMeta.hasDisplayName()) {
            builder.append(" | ").append(itemMeta.getDisplayName());
        }

        return builder.toString();
    }

    private @NotNull String describeRequirementList(final @NotNull Optional<RequirementList> optionalRequirementList) {
        if (optionalRequirementList.isEmpty()) {
            return "none";
        }

        final RequirementList requirementList = optionalRequirementList.get();
        return requirementList.getRequirements().size()
                + " configured, minimum "
                + requirementList.getMinimumRequirements();
    }

    private void sendUsage(final @NotNull CommandSender sender) {
        plugin.sms(sender, header("Debug usage")
                .append(row("Placeholder", "/dm debug placeholder <player> <text>"))
                .append(row("Hook", "/dm debug hook <material> [player]"))
                .append(row("Item", "/dm debug item <menu> <slot> <player>")));
    }

    private @NotNull Component usage(final @NotNull String usage) {
        return text("Incorrect Usage! Use ", NamedTextColor.RED)
                .append(text(usage, NamedTextColor.GRAY));
    }

    private @NotNull Component header(final @NotNull String title) {
        return text(title, NamedTextColor.GOLD);
    }

    private @NotNull Component row(final @NotNull String label, final @NotNull String value) {
        return newline()
                .append(text("> ", NamedTextColor.AQUA))
                .append(text(label, NamedTextColor.GRAY))
                .append(text(": ", NamedTextColor.DARK_GRAY))
                .append(text(value, NamedTextColor.WHITE));
    }

    private @NotNull List<String> completeHookPrefixes(final @NotNull String argument) {
        final List<String> prefixes = plugin.getItemHooks().values()
                .stream()
                .map(ItemHook::getPrefix)
                .collect(Collectors.toList());
        return complete(prefixes, argument);
    }

    private @NotNull List<String> complete(final @NotNull Collection<String> values, final @NotNull String argument) {
        final String lowerArgument = argument.toLowerCase(Locale.ROOT);
        return values.stream()
                .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(lowerArgument))
                .collect(Collectors.toList());
    }
}
