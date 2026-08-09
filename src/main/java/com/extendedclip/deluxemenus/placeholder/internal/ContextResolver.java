package com.extendedclip.deluxemenus.placeholder.internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves the keys of a single internal placeholder context.
 */
public interface ContextResolver {

    /**
     * @param key     the part after the dot, always lower case
     * @param context the context to resolve against
     * @return the replacement value, or null to leave the placeholder in the text untouched
     */
    @Nullable String resolve(final @NotNull String key, final @NotNull PlaceholderContext context);
}
