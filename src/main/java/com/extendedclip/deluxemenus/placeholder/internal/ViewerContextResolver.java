package com.extendedclip.deluxemenus.placeholder.internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ViewerContextResolver implements ContextResolver {

    @Override
    public @Nullable String resolve(final @NotNull String key, final @NotNull PlaceholderContext context) {
        final ViewerSnapshot viewer = context.viewer();

        if (viewer == null) {
            return null;
        }

        switch (key) {
            case "name":
                return viewer.name();
            case "uuid":
                return viewer.uuid();
            case "display_name":
                return viewer.displayName();
            default:
                return null;
        }
    }
}
