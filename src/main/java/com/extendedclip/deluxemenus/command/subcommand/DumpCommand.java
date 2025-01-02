package com.extendedclip.deluxemenus.command.subcommand;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.utils.DumpUtils;
import com.extendedclip.deluxemenus.utils.Messages;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.kyori.adventure.text.Component.text;

public class DumpCommand extends SubCommand {

    public DumpCommand(final @NotNull DeluxeMenus plugin) {
        super(plugin);
    }

    @Override
    public void execute(final @NotNull CommandSender sender, final @NotNull List<String> args) {
        if (!sender.hasPermission("deluxemenus.admin")) {
            plugin.sms(sender, Messages.NO_PERMISSION);
            return;
        }

        if (args.size() != 1) {
            plugin.sms(sender, Messages.WRONG_USAGE_DUMP_COMMAND);
            return;
        }

        String dump = "";
        try {
            dump = DumpUtils.createDump(plugin, args.get(0));
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
}