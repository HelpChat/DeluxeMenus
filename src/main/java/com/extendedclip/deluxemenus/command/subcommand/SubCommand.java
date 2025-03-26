package com.extendedclip.deluxemenus.command.subcommand;

import com.extendedclip.deluxemenus.DeluxeMenus;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public abstract class SubCommand {

    protected static final TextReplacementConfig.Builder PLAYER_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<player>");
    protected static final TextReplacementConfig.Builder AMOUNT_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<amount>");
    protected static final TextReplacementConfig.Builder MENU_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<menu>");
    protected static final TextReplacementConfig.Builder KEY_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<key>");
    protected static final TextReplacementConfig.Builder VALUE_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<value>");
    protected static final TextReplacementConfig.Builder NEW_VALUE_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<new-value>");
    protected static final TextReplacementConfig.Builder TYPE_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<type>");

    protected final DeluxeMenus plugin;

    public SubCommand(final @NotNull DeluxeMenus plugin) {
        this.plugin = plugin;
    }

    public abstract @NotNull String getName();

    public abstract void execute(final @NotNull CommandSender sender, final @NotNull List<String> arguments);

    public abstract @Nullable List<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull List<String> arguments);

    protected @Nullable List<@NotNull String> getPlayerNameCompletion(final @Nullable String argument) {
        final List<String> onlinePlayerNames = Bukkit.getOnlinePlayers()
                .stream()
                .map(Player::getName)
                .collect(Collectors.toList());

        if (onlinePlayerNames.isEmpty()) {
            return null;
        }

        if (argument == null || argument.isEmpty()) {
            return onlinePlayerNames;
        }

        return onlinePlayerNames.stream()
                .filter(playerName -> playerName.toLowerCase().startsWith(argument.toLowerCase()))
                .collect(Collectors.toList());
    }
}
