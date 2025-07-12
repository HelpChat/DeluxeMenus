package com.extendedclip.deluxemenus.events;

import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.menu.MenuHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class DeluxeMenusOpenMenuEvent extends PlayerEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final MenuHolder holder;

    public DeluxeMenusOpenMenuEvent(Player player, MenuHolder holder) {
        super(player);
        this.holder = holder;
    }

    public MenuHolder getHolder() {
        return holder;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
