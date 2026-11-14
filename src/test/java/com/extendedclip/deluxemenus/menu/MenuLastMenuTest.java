package com.extendedclip.deluxemenus.menu;

import com.extendedclip.deluxemenus.menu.options.MenuOptions;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuLastMenuTest {

    @SuppressWarnings("unchecked")
    private static Set<MenuHolder> holders() throws Exception {
        Field f = Menu.class.getDeclaredField("menuHolders");
        f.setAccessible(true);
        return (Set<MenuHolder>) f.get(null);
    }

    @SuppressWarnings("unchecked")
    private static Map<UUID, Menu> lastMenus() throws Exception {
        Field f = Menu.class.getDeclaredField("lastOpenedMenus");
        f.setAccessible(true);
        return (Map<UUID, Menu>) f.get(null);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Menu> loadedMenus() throws Exception {
        Field f = Menu.class.getDeclaredField("menus");
        f.setAccessible(true);
        return (Map<String, Menu>) f.get(null);
    }

    private static Player mockPlayer(UUID uuid, String name) {
        return mockPlayer(uuid, name, true);
    }

    private static Player mockPlayer(UUID uuid, String name, boolean online) {
        PlayerInventory inv = (PlayerInventory) Proxy.newProxyInstance(
                PlayerInventory.class.getClassLoader(),
                new Class<?>[]{PlayerInventory.class},
                (proxy, method, args) -> {
                    if ("getContents".equals(method.getName())) {
                        return new ItemStack[0];
                    }
                    return null;
                }
        );
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUniqueId" -> uuid;
                    case "getName" -> name;
                    case "isOnline" -> online;
                    case "getInventory" -> inv;
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> uuid.hashCode();
                    default -> null;
                }
        );
    }

    private static Inventory mockInventory() {
        return (Inventory) Proxy.newProxyInstance(
                Inventory.class.getClassLoader(),
                new Class<?>[]{Inventory.class},
                (proxy, method, args) -> {
                    if ("getSize".equals(method.getName())) {
                        return 54;
                    }
                    return null;
                }
        );
    }

    @SuppressWarnings({"removal", "sunapi"})
    private static MenuOptions stubOptions(String name) throws Exception {
        Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
        MenuOptions options = (MenuOptions) unsafe.allocateInstance(MenuOptions.class);
        Field nameField = MenuOptions.class.getDeclaredField("name");
        nameField.setAccessible(true);
        nameField.set(options, name);
        return options;
    }

    @BeforeEach
    void resetStaticState() throws Exception {
        holders().clear();
        lastMenus().clear();
        loadedMenus().clear();
    }

    @Test
    void rapidDoubleOpenOfTargetMenuPreservesPreviousDistinctLastMenu() throws Exception {
        Menu overview = new Menu(null, stubOptions("profile_own_overview"), Map.of(), "menus/overview.yml");
        Menu backgrounds = new Menu(null, stubOptions("profile_backgrounds_1"), Map.of(), "menus/backgrounds.yml");
        Player player = mockPlayer(UUID.randomUUID(), "TestPlayer");
        Inventory inv = mockInventory();

        long req0 = Menu.beginMenuOpen(player.getUniqueId());
        MenuHolder holderOverview = new MenuHolder(null, player, overview.options().name(), Set.of(), inv);
        assertTrue(overview.completeMenuOpen(req0, holderOverview, inv, false));

        // First open of profile_backgrounds_1 transitions from profile_own_overview
        long req1 = Menu.beginMenuOpen(player.getUniqueId());
        MenuHolder holderBg1 = new MenuHolder(null, player, backgrounds.options().name(), Set.of(), inv);
        assertTrue(backgrounds.completeMenuOpen(req1, holderBg1, inv, false));
        assertEquals("profile_own_overview", Menu.getLastMenu(player).map(m -> m.options().name()).orElse(""));

        // Rapid second open of profile_backgrounds_1 must not overwrite last_menu with profile_backgrounds_1
        long req2 = Menu.beginMenuOpen(player.getUniqueId());
        MenuHolder holderBg2 = new MenuHolder(null, player, backgrounds.options().name(), Set.of(), inv);
        assertTrue(backgrounds.completeMenuOpen(req2, holderBg2, inv, false));

        assertEquals(
                "profile_own_overview",
                Menu.getLastMenu(player).map(m -> m.options().name()).orElse(""),
                "Re-opening the same menu during rapid clicks must not overwrite last_menu with the current menu"
        );
    }

    @Test
    void staleInFlightOpenRequestIsDiscardedAndDoesNotCorruptLastMenu() throws Exception {
        Menu overview = new Menu(null, stubOptions("profile_own_overview"), Map.of(), "menus/overview.yml");
        Menu backgrounds = new Menu(null, stubOptions("profile_backgrounds_1"), Map.of(), "menus/backgrounds.yml");
        Player player = mockPlayer(UUID.randomUUID(), "SpamClicker");
        Inventory inv = mockInventory();

        long initialReq = Menu.beginMenuOpen(player.getUniqueId());
        assertTrue(overview.completeMenuOpen(
                initialReq,
                new MenuHolder(null, player, overview.options().name(), Set.of(), inv),
                inv,
                false
        ));

        // Two overlapping async open requests start before either completes on the main thread
        long staleReq = Menu.beginMenuOpen(player.getUniqueId());
        long latestReq = Menu.beginMenuOpen(player.getUniqueId());

        assertFalse(Menu.isCurrentOpenRequest(player.getUniqueId(), staleReq));
        assertTrue(Menu.isCurrentOpenRequest(player.getUniqueId(), latestReq));
        assertTrue(Menu.isOpeningMenu(player));

        // Stale request finishes first: discarded, leaving current menu and pending flag intact
        assertFalse(backgrounds.completeMenuOpen(
                staleReq,
                new MenuHolder(null, player, backgrounds.options().name(), Set.of(), inv),
                inv,
                false
        ));
        assertEquals("profile_own_overview", Menu.getOpenMenu(player).map(m -> m.options().name()).orElse(""));
        assertTrue(Menu.isOpeningMenu(player));

        // Latest request finishes: transitions cleanly once and clears pending flag
        assertTrue(backgrounds.completeMenuOpen(
                latestReq,
                new MenuHolder(null, player, backgrounds.options().name(), Set.of(), inv),
                inv,
                false
        ));
        assertFalse(Menu.isOpeningMenu(player));
        assertEquals("profile_backgrounds_1", Menu.getOpenMenu(player).map(m -> m.options().name()).orElse(""));
        assertEquals("profile_own_overview", Menu.getLastMenu(player).map(m -> m.options().name()).orElse(""));
    }

    @Test
    void alternatingRapidSwitchesAlwaysTrackImmediatePreviousMenu() throws Exception {
        Menu overview = new Menu(null, stubOptions("profile_own_overview"), Map.of(), "menus/overview.yml");
        Menu backgrounds = new Menu(null, stubOptions("profile_backgrounds_1"), Map.of(), "menus/backgrounds.yml");
        Player player = mockPlayer(UUID.randomUUID(), "RapidSwitcher");
        Inventory inv = mockInventory();

        long r0 = Menu.beginMenuOpen(player.getUniqueId());
        assertTrue(overview.completeMenuOpen(r0, new MenuHolder(null, player, overview.options().name(), Set.of(), inv), inv, false));

        for (int i = 0; i < 20; i++) {
            long toBg = Menu.beginMenuOpen(player.getUniqueId());
            assertTrue(backgrounds.completeMenuOpen(toBg, new MenuHolder(null, player, backgrounds.options().name(), Set.of(), inv), inv, false));
            assertEquals("profile_own_overview", Menu.getLastMenu(player).map(m -> m.options().name()).orElse(""));

            // Simulate %deluxemenus_last_menu% resolving back to overview
            Menu backTarget = Menu.getLastMenu(player).orElseThrow();
            long toOverview = Menu.beginMenuOpen(player.getUniqueId());
            assertTrue(backTarget.completeMenuOpen(toOverview, new MenuHolder(null, player, backTarget.options().name(), Set.of(), inv), inv, false));
            assertEquals("profile_backgrounds_1", Menu.getLastMenu(player).map(m -> m.options().name()).orElse(""));
        }
    }

    @Test
    void offlinePlayerOrClearedPendingOpenAbortsCleanly() throws Exception {
        Menu overview = new Menu(null, stubOptions("profile_own_overview"), Map.of(), "menus/overview.yml");
        UUID playerId = UUID.randomUUID();
        Player offlinePlayer = mockPlayer(playerId, "Quitter", false);
        Inventory inv = mockInventory();

        long req = Menu.beginMenuOpen(playerId);
        assertFalse(overview.completeMenuOpen(req, new MenuHolder(null, offlinePlayer, overview.options().name(), Set.of(), inv), inv, false));
        assertFalse(Menu.isOpeningMenu(offlinePlayer));
        assertFalse(Menu.isInMenu(offlinePlayer));
    }

    @Test
    void concurrentLastMenuReadsDuringRapidTransitionsAreThreadSafe() throws Exception {
        Menu overview = new Menu(null, stubOptions("profile_own_overview"), Map.of(), "menus/overview.yml");
        Menu backgrounds = new Menu(null, stubOptions("profile_backgrounds_1"), Map.of(), "menus/backgrounds.yml");
        Player player = mockPlayer(UUID.randomUUID(), "ConcurrentPlayer");
        Inventory inv = mockInventory();

        long init = Menu.beginMenuOpen(player.getUniqueId());
        overview.completeMenuOpen(init, new MenuHolder(null, player, overview.options().name(), Set.of(), inv), inv, false);
        long firstBg = Menu.beginMenuOpen(player.getUniqueId());
        backgrounds.completeMenuOpen(firstBg, new MenuHolder(null, player, backgrounds.options().name(), Set.of(), inv), inv, false);

        AtomicBoolean running = new AtomicBoolean(true);
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            List<Future<Void>> readers = new ArrayList<>();
            for (int t = 0; t < 4; t++) {
                Callable<Void> reader = () -> {
                    while (running.get()) {
                        String last = Menu.getLastMenu(player).map(m -> m.options().name()).orElse("");
                        assertTrue(
                                "profile_own_overview".equals(last) || "profile_backgrounds_1".equals(last),
                                "Unexpected last_menu value during concurrent read: " + last
                        );
                    }
                    return null;
                };
                readers.add(pool.submit(reader));
            }

            for (int i = 0; i < 500; i++) {
                Menu target = (i % 2 == 0) ? overview : backgrounds;
                long req = Menu.beginMenuOpen(player.getUniqueId());
                assertTrue(target.completeMenuOpen(req, new MenuHolder(null, player, target.options().name(), Set.of(), inv), inv, false));
            }

            running.set(false);
            for (Future<Void> f : readers) {
                f.get();
            }
        } finally {
            running.set(false);
            pool.shutdownNow();
        }
    }
}
