package com.extendedclip.deluxemenus.placeholder.internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MenuContextResolver implements ContextResolver {

    @Override
    public @Nullable String resolve(final @NotNull String key, final @NotNull PlaceholderContext context) {
        final MenuSnapshot menu = context.menu();

        if (menu == null) {
            return null;
        }

        switch (key) {
            case "name":
                return menu.name();
            case "title":
                return menu.title();
            case "type":
                return menu.type();
            case "size":
                return String.valueOf(menu.size());
            case "rows":
                return String.valueOf(menu.size() / 9);
            case "item_count":
                return menu.itemCount() == null ? null : String.valueOf(menu.itemCount());
            case "open_command":
                return menu.openCommand();
            case "has_placeholder_player":
                return InternalPlaceholderResolver.bool(menu.hasPlaceholderPlayer());
            default:
                return null;
        }
    }
}
