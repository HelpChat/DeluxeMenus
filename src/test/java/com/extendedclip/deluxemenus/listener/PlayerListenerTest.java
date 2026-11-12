package com.extendedclip.deluxemenus.listener;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.action.ClickHandler;
import com.extendedclip.deluxemenus.config.GeneralConfig;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.menu.MenuHolder;
import com.extendedclip.deluxemenus.menu.MenuItem;
import com.extendedclip.deluxemenus.menu.options.MenuItemOptions;
import com.extendedclip.deluxemenus.menu.options.MenuOptions;
import com.extendedclip.deluxemenus.requirement.RequirementList;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.Mockito.*;

class PlayerListenerTest {
    private final DeluxeMenus plugin = mock(DeluxeMenus.class);
    private final Player player = mock(Player.class);
    private final ClickHandler actions = mock(ClickHandler.class);
    private final RequirementList requirements = mock(RequirementList.class);
    private final MenuItem item = mock(MenuItem.class);
    private final MenuItemOptions itemOptions = mock(MenuItemOptions.class);
    private final YamlConfiguration config = new YamlConfiguration();
    private final AtomicLong clock = new AtomicLong();
    private GeneralConfig general;
    private PlayerListener listener;
    private MenuHolder holder;
    private MockedStatic<Menu> menus;

    @BeforeEach
    void setUp() {
        when(plugin.getConfig()).thenReturn(config);
        general = new GeneralConfig(plugin);
        general.load();
        when(plugin.getGeneralConfig()).thenReturn(general);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(item.options()).thenReturn(itemOptions);
        when(itemOptions.clickHandler()).thenReturn(Optional.of(actions));
        when(itemOptions.clickRequirements()).thenReturn(Optional.of(requirements));
        when(requirements.evaluate(any())).thenReturn(true);
        menus = mockStatic(Menu.class);
        menus.when(() -> Menu.getMenuHolder(player)).thenAnswer(invocation -> Optional.of(holder));
        openMenu(null);
        listener = new PlayerListener(plugin, clock::get);
    }

    @AfterEach
    void tearDown() {
        if (menus != null) menus.close();
    }

    private void openMenu(Integer cooldown) {
        Menu menu = mock(Menu.class);
        MenuOptions options = mock(MenuOptions.class);
        when(options.clickCooldownMillis()).thenReturn(Optional.ofNullable(cooldown));
        when(menu.options()).thenReturn(options);
        holder = mock(MenuHolder.class);
        when(holder.getMenu()).thenReturn(Optional.of(menu));
        when(holder.getItem(0)).thenReturn(item);
    }

    private void at(long millis) {
        clock.set(TimeUnit.MILLISECONDS.toNanos(millis));
    }

    private void click() {
        InventoryClickEvent event = mock(InventoryClickEvent.class);
        when(event.getWhoClicked()).thenReturn(player);
        when(event.getClick()).thenReturn(ClickType.LEFT);
        listener.onClick(event);
        verify(event).setCancelled(true);
    }

    @Test
    void omittedSettingsKeepTheExistingSeventyFiveMillisecondWindow() {
        click();
        at(74);
        click();
        at(75);
        click();
        verify(actions, times(2)).onClick(holder);
    }

    @Test
    void inheritedGlobalCooldownBlocksActionsAndRequirementsWithoutExtendingTheWindow() {
        config.set("click_cooldown_ms", 200);
        general.load();
        click();
        at(90);
        click();
        at(199);
        click();
        at(200);
        click();
        verify(actions, times(2)).onClick(holder);
        verify(requirements, times(2)).evaluate(holder);
        menus.verify(() -> Menu.closeMenu(plugin, player, true), never());
    }

    @Test
    void menuOverrideReplacesTheGlobalDuration() {
        config.set("click_cooldown_ms", 75);
        general.load();
        openMenu(250);
        click();
        at(90);
        click();
        at(250);
        click();
        verify(actions, times(2)).onClick(holder);
    }

    @Test
    void zeroMenuOverrideDisablesTheGeneralDelay() {
        config.set("click_cooldown_ms", 200);
        general.load();
        openMenu(0);
        click();
        click();
        verify(actions, times(2)).onClick(holder);
    }

    @Test
    void rejectedRequirementsDoNotStartACooldown() {
        when(requirements.evaluate(holder)).thenReturn(false, true);
        click();
        click();
        click();
        verify(requirements, times(2)).evaluate(holder);
        verify(actions).onClick(holder);
    }

    @Test
    void switchingToAMenuWithNoDelayDoesNotBypassAnActiveCooldown() {
        openMenu(250);
        MenuHolder first = holder;
        click();
        at(90);
        openMenu(0);
        click();
        verify(actions).onClick(first);
        verify(actions, never()).onClick(holder);
        at(250);
        click();
        verify(actions).onClick(holder);
    }

    @Test
    void playersDoNotShareCooldowns() {
        click();
        Player other = mock(Player.class);
        when(other.getUniqueId()).thenReturn(UUID.randomUUID());
        menus.when(() -> Menu.getMenuHolder(other)).thenReturn(Optional.of(holder));
        InventoryClickEvent event = mock(InventoryClickEvent.class);
        when(event.getWhoClicked()).thenReturn(other);
        when(event.getClick()).thenReturn(ClickType.LEFT);
        listener.onClick(event);
        verify(actions, times(2)).onClick(holder);
    }

    @Test
    void quittingClearsThePlayersCooldown() {
        openMenu(250);
        click();
        PlayerQuitEvent quit = mock(PlayerQuitEvent.class);
        when(quit.getPlayer()).thenReturn(player);
        listener.onLeave(quit);
        at(90);
        click();
        verify(actions, times(2)).onClick(holder);
    }

    @Test
    void reloadingTheGlobalSettingAppliesToSubsequentAcceptedClicks() {
        config.set("click_cooldown_ms", 200);
        general.load();
        click();
        config.set("click_cooldown_ms", 300);
        general.load();
        at(200);
        click();
        at(450);
        click();
        at(500);
        click();
        verify(actions, times(3)).onClick(holder);
    }
}
