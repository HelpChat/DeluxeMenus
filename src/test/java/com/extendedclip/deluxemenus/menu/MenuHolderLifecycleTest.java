package com.extendedclip.deluxemenus.menu;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.options.MenuItemOptions;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MenuHolderLifecycleTest {
    private DeluxeMenus plugin;
    private Player viewer;
    private Inventory inventory;
    private BukkitScheduler scheduler;
    private MenuHolder holder;

    @BeforeEach
    void setUp() {
        plugin = mock(DeluxeMenus.class);
        viewer = mock(Player.class);
        inventory = mock(Inventory.class);
        scheduler = mock(BukkitScheduler.class);
        when(viewer.isOnline()).thenReturn(true);
        holder = new MenuHolder(plugin, viewer, "test", Set.of(), inventory);
    }

    @Test
    void closedHolderCannotStartTasksOrRefresh() {
        holder.close();
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            holder.startRefreshTask();
            holder.startUpdatePlaceholdersTask();
            holder.refreshMenu();
            bukkit.verifyNoInteractions();
        }
        verifyNoInteractions(inventory);
    }

    @Test
    void offlineHolderCannotStartTasksOrRefresh() {
        when(viewer.isOnline()).thenReturn(false);
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            holder.startRefreshTask();
            holder.startUpdatePlaceholdersTask();
            holder.refreshMenu();
            bukkit.verifyNoInteractions();
        }
        verifyNoInteractions(inventory);
    }

    @Test
    void closeCancelsBothTasksAndIsIdempotent() {
        BukkitTask refresh = mock(BukkitTask.class);
        BukkitTask update = mock(BukkitTask.class);
        when(scheduler.runTaskTimerAsynchronously(eq(plugin), any(Runnable.class), anyLong(), anyLong()))
                .thenReturn(refresh, update);
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
             MockedStatic<Menu> menus = mockStatic(Menu.class)) {
            bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
            menus.when(() -> Menu.getMenuByName("test")).thenReturn(Optional.empty());
            holder.startRefreshTask();
            holder.startUpdatePlaceholdersTask();
            assertSame(update, holder.getUpdateTask());
            holder.close();
            holder.close();
            verify(refresh).cancel();
            verify(update).cancel();
            assertNull(holder.getUpdateTask());
        }
    }

    @Test
    void queuedRefreshCannotResumeAfterClose() {
        for (boolean closeBeforeSelection : new boolean[]{true, false}) {
            holder = new MenuHolder(plugin, viewer, "test", Set.of(), inventory);
            Menu menu = mock(Menu.class);
            MenuItem item = mock(MenuItem.class);
            MenuItemOptions options = mock(MenuItemOptions.class);
            when(item.options()).thenReturn(options);
            when(options.viewRequirements()).thenReturn(Optional.empty());
            when(menu.getMenuItems()).thenReturn(Map.of(0, new TreeMap<>(Map.of(0, item))));
            when(inventory.getSize()).thenReturn(1);
            List<Runnable> async = new ArrayList<>();
            List<Runnable> sync = new ArrayList<>();
            when(scheduler.runTaskAsynchronously(eq(plugin), any(Runnable.class))).thenAnswer(invocation -> {
                async.add(invocation.getArgument(1));
                return mock(BukkitTask.class);
            });
            when(scheduler.runTask(eq(plugin), any(Runnable.class))).thenAnswer(invocation -> {
                sync.add(invocation.getArgument(1));
                return mock(BukkitTask.class);
            });
            try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
                 MockedStatic<Menu> menus = mockStatic(Menu.class)) {
                bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
                menus.when(() -> Menu.getMenuByName("test")).thenReturn(Optional.of(menu));
                holder.refreshMenu();
                assertEquals(1, async.size());
                if (closeBeforeSelection) holder.close();
                async.get(0).run();
                if (!closeBeforeSelection) {
                    assertEquals(1, sync.size());
                    holder.close();
                    sync.get(0).run();
                } else {
                    assertTrue(sync.isEmpty());
                }
                verify(item, never()).getItemStack(any());
                assertNull(holder.getUpdateTask());
                verify(scheduler, never()).runTaskTimerAsynchronously(
                        eq(plugin), any(Runnable.class), anyLong(), anyLong());
            }
        }
    }

    @Test
    void alreadyQueuedPlaceholderTickStopsAfterDisconnect() {
        BukkitTask task = mock(BukkitTask.class);
        List<Runnable> ticks = new ArrayList<>();
        when(scheduler.runTaskTimerAsynchronously(eq(plugin), any(Runnable.class), anyLong(), anyLong()))
                .thenAnswer(invocation -> {
                    ticks.add(invocation.getArgument(1));
                    return task;
                });
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
             MockedStatic<Menu> menus = mockStatic(Menu.class)) {
            bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
            menus.when(() -> Menu.getMenuByName("test")).thenReturn(Optional.empty());
            holder.startUpdatePlaceholdersTask();
            when(viewer.isOnline()).thenReturn(false);
            ticks.get(0).run();
            verify(task).cancel();
            assertNull(holder.getUpdateTask());
            verifyNoInteractions(inventory);
        }
    }

    @Test
    void onlinePlaceholderTargetStillResolves() {
        Player target = mock(Player.class);
        when(target.isOnline()).thenReturn(true);
        holder.setPlaceholderPlayer(target);
        holder.setTypedArgs(Map.of("target", "%example_value%"));
        holder.parsePlaceholdersInArguments(true);
        try (MockedStatic<PlaceholderAPI> placeholders = mockStatic(PlaceholderAPI.class)) {
            placeholders.when(() -> PlaceholderAPI.setPlaceholders(target, "%example_value%"))
                    .thenReturn("resolved");
            assertEquals("resolved", holder.setPlaceholders("%example_value%"));
            assertEquals("resolved", holder.setArguments("{target}"));
            placeholders.verify(() -> PlaceholderAPI.setPlaceholders(target, "%example_value%"), times(2));
        }
    }

    @Test
    void onlinePlaceholderTickStillUpdatesItems() {
        BukkitTask task = mock(BukkitTask.class);
        List<Runnable> ticks = new ArrayList<>();
        MenuItem item = mock(MenuItem.class);
        MenuItemOptions options = mock(MenuItemOptions.class);
        when(item.options()).thenReturn(options);
        when(options.updatePlaceholders()).thenReturn(true);
        ItemStack stack = mock(ItemStack.class);
        ItemMeta meta = mock(ItemMeta.class);
        when(inventory.getItem(0)).thenReturn(stack);
        when(stack.getItemMeta()).thenReturn(meta);
        when(stack.getAmount()).thenReturn(1);
        holder.setActiveItems(Set.of(item));
        when(scheduler.runTaskTimerAsynchronously(eq(plugin), any(Runnable.class), anyLong(), anyLong()))
                .thenAnswer(invocation -> {
                    ticks.add(invocation.getArgument(1));
                    return task;
                });
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
             MockedStatic<Menu> menus = mockStatic(Menu.class)) {
            bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
            menus.when(() -> Menu.getMenuByName("test")).thenReturn(Optional.empty());
            holder.startUpdatePlaceholdersTask();
            ticks.get(0).run();
            verify(inventory).getItem(0);
            verify(stack).setItemMeta(meta);
            verify(stack).setAmount(1);
            verify(task, never()).cancel();
            assertSame(task, holder.getUpdateTask());
        }
    }

    @Test
    void offlinePlaceholderTargetIsNotPassedToPlayerExpansions() {
        Player target = mock(Player.class);
        holder.setPlaceholderPlayer(target);
        holder.setTypedArgs(Map.of("target", "%example_value%"));
        holder.parsePlaceholdersInArguments(true);
        assertEquals("%example_value%", holder.setPlaceholders("%example_value%"));
        assertEquals("%example_value%", holder.setArguments("{target}"));
    }
}
