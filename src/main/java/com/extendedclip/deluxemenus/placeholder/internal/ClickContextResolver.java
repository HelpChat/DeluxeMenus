package com.extendedclip.deluxemenus.placeholder.internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClickContextResolver implements ContextResolver {

    @Override
    public @Nullable String resolve(final @NotNull String key, final @NotNull PlaceholderContext context) {
        final ClickSnapshot click = context.click();

        if (click == null) {
            return null;
        }

        switch (key) {
            case "type":
                return click.type();
            case "action":
                return click.action();
            case "slot":
                return String.valueOf(click.slot());
            case "raw_slot":
                return String.valueOf(click.rawSlot());
            case "hotbar_button":
                return String.valueOf(click.hotbarButton());
            case "cursor_material":
                return click.cursorMaterial();
            case "is_left":
                return InternalPlaceholderResolver.bool(click.left());
            case "is_right":
                return InternalPlaceholderResolver.bool(click.right());
            case "is_shift":
                return InternalPlaceholderResolver.bool(click.shift());
            default:
                return null;
        }
    }
}
