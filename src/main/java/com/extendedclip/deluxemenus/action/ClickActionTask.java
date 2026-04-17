package com.extendedclip.deluxemenus.action;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.menu.MenuHolder;
import com.extendedclip.deluxemenus.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ClickActionTask extends BukkitRunnable {

    private final DeluxeMenus plugin;
    private final UUID uuid;
    private final ActionType actionType;
    private final String executableTemplate;
    private final Map<String, String> arguments;
    private final boolean parsePlaceholdersInArguments;
    private final boolean parsePlaceholdersAfterArguments;

    public ClickActionTask(
            @NotNull final DeluxeMenus plugin,
            @NotNull final UUID uuid,
            @NotNull final ActionType actionType,
            @NotNull final String exec,
            @NotNull final Map<String, String> arguments,
            final boolean parsePlaceholdersInArguments,
            final boolean parsePlaceholdersAfterArguments
    ) {
        this.plugin = plugin;
        this.uuid = uuid;
        this.actionType = actionType;
        this.executableTemplate = exec;
        this.arguments = arguments;
        this.parsePlaceholdersInArguments = parsePlaceholdersInArguments;
        this.parsePlaceholdersAfterArguments = parsePlaceholdersAfterArguments;
    }

    @Override
    public void run() {
        final Player player = Bukkit.getPlayer(this.uuid);
        if (player == null) {
            return;
        }

        final Optional<MenuHolder> holder = Menu.getMenuHolder(player);
        final String executable = StringUtils.replacePlaceholdersAndArguments(
                this.executableTemplate,
                this.arguments,
                holder.isPresent() && holder.get().getPlaceholderPlayer() != null ? holder.get().getPlaceholderPlayer() : player,
                this.parsePlaceholdersInArguments,
                this.parsePlaceholdersAfterArguments);
        new ClickActionExecutor(this.plugin, player, holder, this.actionType, executable).execute();
    }

}