package com.extendedclip.deluxemenus.command.subcommand;

import com.extendedclip.deluxemenus.DeluxeMenus;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class SubCommand {

    protected static final TextReplacementConfig.Builder PLAYER_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<player>");
    protected static final TextReplacementConfig.Builder AMOUNT_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<amount>");
    protected static final TextReplacementConfig.Builder MENU_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<menu>");

    protected final DeluxeMenus plugin;

    public SubCommand(final @NotNull DeluxeMenus plugin) {
        this.plugin = plugin;
    }

    public abstract @NotNull String getName();

    public abstract void execute(final @NotNull CommandSender sender, final @NotNull List<String> arguments);

    public abstract @Nullable List<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull List<String> arguments);
}
