package com.extendedclip.deluxemenus.command.subcommand;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.utils.DumpUtils;
import com.extendedclip.deluxemenus.utils.Messages;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.text;

public class DumpCommand extends SubCommand {

    private static final String ADMIN_PERMISSION = "deluxemenus.admin";

    public DumpCommand(final @NotNull DeluxeMenus plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getName() {
        return "dump";
    }

    @Override
    public void execute(final @NotNull CommandSender sender, final @NotNull List<String> arguments) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            plugin.sms(sender, Messages.NO_PERMISSION);
            return;
        }

        if (arguments.size() != 1) {
            plugin.sms(sender, Messages.WRONG_USAGE_DUMP_COMMAND);
            return;
        }

        String dump = "";
        try {
            dump = DumpUtils.createDump(plugin, arguments.get(0));
        } catch (final RuntimeException ignored) {
        }

        if (dump.isBlank()) {
            plugin.sms(sender, Messages.DUMP_FAILED);
            return;
        }

        DumpUtils.postDump(dump).whenComplete((result, error) -> {
            if (error != null) {
                plugin.printStacktrace("Something went wrong while trying to create and post a dump!", error);
                plugin.sms(sender, Messages.DUMP_FAILED);
                return;
            }

            final var link = text(DumpUtils.URL + result).clickEvent(ClickEvent.openUrl(DumpUtils.URL + result));

            plugin.sms(sender, Messages.DUMP_SUCCESS.message().append(link));
        });
    }

    @Override
    public @Nullable List<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull List<String> arguments) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
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

        final String secondArgument = arguments.get(1).toLowerCase();

        final List<String> completions = new ArrayList<>(Menu.getAllMenuNames());
        completions.add("config");

        if (secondArgument.isEmpty()) {
            return completions;
        }

        return completions.stream()
                .filter(completion -> completion.startsWith(secondArgument))
                .collect(Collectors.toList());
    }
}