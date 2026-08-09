package com.extendedclip.deluxemenus.placeholder.internal;

import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.menu.MenuHolder;
import com.extendedclip.deluxemenus.menu.MenuItem;
import com.extendedclip.deluxemenus.menu.options.MenuOptions;
import com.extendedclip.deluxemenus.utils.VersionHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The data internal placeholders are resolved against.
 * <p>
 * A context holds <b>only snapshotted values</b> - never a {@code MenuHolder}, a {@code MenuItem},
 * an {@code ItemStack} or an event. Nothing in it can go stale and it does not keep a closed menu
 * alive, which is why the same context can be built on the async item-building thread, evaluated on
 * the main thread during a click, and stored inside a {@code ClickActionTask} that runs several
 * ticks later.
 * <p>
 * A null group means "this context is not available here", and its placeholders are left in the
 * text untouched.
 */
public final class PlaceholderContext {

    public static final PlaceholderContext EMPTY = new PlaceholderContext(null, null, null, null);

    private final MenuSnapshot menu;
    private final ViewerSnapshot viewer;
    private final ItemSnapshot item;
    private final ClickSnapshot click;

    private PlaceholderContext(final @Nullable MenuSnapshot menu, final @Nullable ViewerSnapshot viewer,
                               final @Nullable ItemSnapshot item, final @Nullable ClickSnapshot click) {
        this.menu = menu;
        this.viewer = viewer;
        this.item = item;
        this.click = click;
    }

    /**
     * Snapshots the menu and the viewer of the given holder.
     */
    public static @NotNull PlaceholderContext of(final @Nullable MenuHolder holder) {
        if (holder == null) {
            return EMPTY;
        }

        final String menuName = holder.getMenuName();
        final MenuOptions options = menuName == null
                ? null
                : Menu.getMenuByName(menuName).map(Menu::options).orElse(null);

        // No menu resolved - for example /dm execute against a player who is not in a menu. The
        // group stays null so %menu.*% is left literal rather than silently rendering as empty.
        final MenuSnapshot menuSnapshot;
        if (options == null) {
            menuSnapshot = null;
        } else {
            final List<String> commands = options.commands();
            menuSnapshot = new MenuSnapshot(
                    menuName,
                    options.title(),
                    options.type().name(),
                    // Menu.openMenu only passes `size` to Bukkit.createInventory for CHEST menus;
                    // every other type gets its own fixed size, so `size:` would be misleading.
                    options.type() == InventoryType.CHEST
                            ? options.size()
                            : options.type().getDefaultSize(),
                    holder.getActiveItems() == null ? null : holder.getActiveItems().size(),
                    commands.isEmpty() ? "" : commands.get(0),
                    holder.getPlaceholderPlayer() != null
            );
        }

        final Player viewer = holder.getViewer();
        final ViewerSnapshot viewerSnapshot = viewer == null ? null : new ViewerSnapshot(
                viewer.getName(),
                viewer.getUniqueId().toString(),
                viewer.getDisplayName()
        );

        return new PlaceholderContext(menuSnapshot, viewerSnapshot, null, null);
    }

    /**
     * Returns a copy of this context carrying the config side values of the given item.
     */
    public @NotNull PlaceholderContext withItem(final @Nullable MenuItem item) {
        if (item == null) {
            return this;
        }

        return new PlaceholderContext(menu, viewer, new ItemSnapshot(
                item.options().slot(),
                item.options().priority(),
                item.options().updatePlaceholders()
        ), click);
    }

    /**
     * Returns a copy of this context whose item snapshot also carries the values of the built
     * {@link ItemStack}. The stack and its meta are read here and then dropped.
     * <p>
     * Does nothing when no item is in context yet - call {@link #withItem(MenuItem)} first.
     */
    public @NotNull PlaceholderContext withItemStack(final @Nullable ItemStack itemStack,
                                                     final @Nullable ItemMeta itemMeta) {
        if (item == null || itemStack == null) {
            return this;
        }

        String modelData = null;
        String displayName = null;

        if (itemMeta != null) {
            if (VersionHelper.IS_CUSTOM_MODEL_DATA && itemMeta.hasCustomModelData()) {
                modelData = String.valueOf(itemMeta.getCustomModelData());
            }
            if (itemMeta.hasDisplayName()) {
                displayName = itemMeta.getDisplayName();
            }
        }

        return new PlaceholderContext(menu, viewer, item.withStack(
                itemStack.getType().name(),
                itemStack.getAmount(),
                modelData,
                displayName
        ), click);
    }

    /**
     * Returns a copy of this context carrying a snapshot of the given click. The event is read here
     * and then dropped.
     */
    public @NotNull PlaceholderContext withClick(final @Nullable InventoryClickEvent event) {
        if (event == null) {
            return this;
        }

        final ItemStack cursor = event.getCursor();

        return new PlaceholderContext(menu, viewer, item, new ClickSnapshot(
                event.getClick().name(),
                event.getAction().name(),
                event.getSlot(),
                event.getRawSlot(),
                event.getHotbarButton(),
                cursor == null ? "AIR" : cursor.getType().name(),
                event.isLeftClick(),
                event.isRightClick(),
                event.isShiftClick()
        ));
    }

    public @Nullable MenuSnapshot menu() {
        return menu;
    }

    public @Nullable ViewerSnapshot viewer() {
        return viewer;
    }

    public @Nullable ItemSnapshot item() {
        return item;
    }

    public @Nullable ClickSnapshot click() {
        return click;
    }
}
