package com.extendedclip.deluxemenus.command.subcommand;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.action.ActionType;
import com.extendedclip.deluxemenus.action.ClickAction;
import com.extendedclip.deluxemenus.action.ClickActionTask;
import com.extendedclip.deluxemenus.config.DeluxeMenusConfig;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.menu.MenuHolder;
import com.extendedclip.deluxemenus.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ExecuteCommand extends SubCommand {

    public ExecuteCommand(final @NotNull DeluxeMenus plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getName() {
        return "execute";
    }

    @Override
    public void execute(final @NotNull CommandSender sender, final @NotNull List<String> arguments) {
        if (!sender.isOp()) {
            plugin.sms(sender, Messages.NO_PERMISSION);
            return;
        }

        if (arguments.size() < 2) {
            plugin.sms(sender, Messages.WRONG_USAGE_EXECUTE_COMMAND);
            return;
        }

        Player target = Bukkit.getPlayerExact(arguments.get(0));
        if (target == null) {
            plugin.sms(sender, Messages.PLAYER_IS_NOT_ONLINE.message().replaceText(PLAYER_REPLACER_BUILDER.replacement(arguments.get(1)).build()));
            return;
        }

        String executable = String.join(" ", arguments.subList(1, arguments.size()));

        ActionType type = ActionType.getByStart(executable);

        if (type == null) {
            plugin.sms(sender, Messages.WRONG_ACTION_TYPE);
            return;
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

        MenuHolder holder = Menu.getMenuHolder(target).orElse(new MenuHolder(plugin, target));

        if (!action.checkChance(holder)) {
            plugin.sms(sender, Messages.CHANCE_FAIL);
            return;
        }

        final ClickActionTask actionTask = new ClickActionTask(plugin, target.getUniqueId(), action.getType(), action.getExecutable(), holder.getTypedArgs(), true, true);

        if (action.hasDelay()) {
            actionTask.runTaskLater(plugin, action.getDelay(holder));

            plugin.sms(sender, Messages.ACTION_TO_BE_EXECUTED.message().replaceText(AMOUNT_REPLACER_BUILDER.replacement(String.valueOf(action.getDelay(holder))).build()));
            return;
        }

        actionTask.runTask(plugin);

        plugin.sms(sender, Messages.ACTION_EXECUTED_FOR.message().replaceText(PLAYER_REPLACER_BUILDER.replacement(target.getName()).build()));

    }

    @Override
    public @Nullable List<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull List<String> arguments) {
        if (!sender.isOp()) {
            return null;
        }

        if (arguments.isEmpty()) {
            return List.of(getName());
        }

        if (arguments.size() > 2) {
            return null;
        }

        if (arguments.size() == 1) {
            if (arguments.get(0).isEmpty()) {
                return List.of(getName());
            }

            final String firstArgument = arguments.get(0).toLowerCase();

            if (getName().startsWith(firstArgument)) {
                return List.of(getName());
            }

            return null;
        }

        final String firstArgument = arguments.get(0).toLowerCase();

        if (!getName().equals(firstArgument)) {
            return null;
        }

        final List<String> onlinePlayerNames = Bukkit.getOnlinePlayers()
                .stream()
                .map(Player::getName)
                .collect(Collectors.toList());

        if (onlinePlayerNames.isEmpty()) {
            return null;
        }

        final String secondArgument = arguments.get(1).toLowerCase();

        if (secondArgument.isEmpty()) {
            return onlinePlayerNames;
        }

        return onlinePlayerNames.stream()
                .filter(playerName -> playerName.toLowerCase().startsWith(secondArgument))
                .collect(Collectors.toList());
    }
}
