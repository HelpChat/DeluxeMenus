package com.extendedclip.deluxemenus.placeholder.internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemContextResolver implements ContextResolver {

    @Override
    public @Nullable String resolve(final @NotNull String key, final @NotNull PlaceholderContext context) {
        final ItemSnapshot item = context.item();

        if (item == null) {
            return null;
        }

        switch (key) {
            case "slot":
                return String.valueOf(item.slot());
            case "row":
                return String.valueOf(item.slot() / 9 + 1);
            case "column":
                return String.valueOf(item.slot() % 9 + 1);
            case "priority":
                return String.valueOf(item.priority());
            case "update":
                return InternalPlaceholderResolver.bool(item.update());
            // The values below only exist once the ItemStack has been built. Before that they are
            // left in the text untouched, which is what happens inside a view_requirement.
            case "material":
                return item.material();
            case "amount":
                return item.amount() == null ? null : String.valueOf(item.amount());
            case "model_data":
                return item.material() == null ? null : (item.modelData() == null ? "" : item.modelData());
            case "display_name":
                return item.material() == null ? null : (item.displayName() == null ? "" : item.displayName());
            default:
                return null;
        }
    }
}
