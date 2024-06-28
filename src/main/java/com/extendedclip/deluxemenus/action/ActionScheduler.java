package com.extendedclip.deluxemenus.action;

import com.extendedclip.deluxemenus.DeluxeMenus;
import io.github.projectunified.minelib.scheduler.async.AsyncScheduler;
import io.github.projectunified.minelib.scheduler.common.scheduler.Scheduler;
import io.github.projectunified.minelib.scheduler.entity.EntityScheduler;
import io.github.projectunified.minelib.scheduler.global.GlobalScheduler;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public enum ActionScheduler {
    GLOBAL(player -> GlobalScheduler.get(DeluxeMenus.getInstance())),
    ASYNC(player -> AsyncScheduler.get(DeluxeMenus.getInstance())),
    PLAYER(player -> {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null for player scheduler");
        }
        return EntityScheduler.get(DeluxeMenus.getInstance(), player);
    })
    ;
    private final Function<Player, Scheduler> schedulerFunction;

    ActionScheduler(Function<Player, Scheduler> schedulerFunction) {
        this.schedulerFunction = schedulerFunction;
    }

    public Scheduler getScheduler(@Nullable Player player) {
        return schedulerFunction.apply(player);
    }
}
