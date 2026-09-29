package com.extendedclip.deluxemenus.dupe;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.dupe.marker.ItemMarker;
import com.extendedclip.deluxemenus.dupe.marker.impl.PDCMenuItemMarker;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Mark inventory items created by DeluxeMenus to prevent duplication. Marked items will be removed from all inventories
 * except the one they were created in.
 */
public class MenuItemMarker implements ItemMarker {

    private final static String DEFAULT_MARK = "DM";
    private final static Pattern MARK_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");
    private final ItemMarker marker;
    private final String mark;

    public MenuItemMarker(@NotNull final DeluxeMenus plugin) {
        this(plugin, DEFAULT_MARK);
    }

    public MenuItemMarker(@NotNull final DeluxeMenus plugin, @NotNull final String mark) {
        this.mark = DEFAULT_MARK.equals(mark) || MARK_PATTERN.matcher(mark).matches() ? mark : DEFAULT_MARK;
        marker = new PDCMenuItemMarker(plugin, this.mark);
    }

    @Override
    public @NotNull ItemStack mark(@NotNull final ItemStack itemStack) {
        return marker.mark(itemStack);
    }

    @Override
    public @NotNull ItemStack unmark(@NotNull final ItemStack itemStack) {
        return marker.unmark(itemStack);
    }

    @Override
    public boolean isMarked(@NotNull final ItemStack itemStack) {
        return marker.isMarked(itemStack);
    }

    public @NotNull String getMark() {
        return mark;
    }
}
