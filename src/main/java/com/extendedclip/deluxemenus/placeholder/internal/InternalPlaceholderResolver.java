package com.extendedclip.deluxemenus.placeholder.internal;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves DeluxeMenus' own {@code %<context>.<key>%} placeholders.
 * <p>
 * The dot is what keeps this from colliding with PlaceholderAPI: PAPI's own pattern requires
 * {@code %} followed by {@code [a-zA-Z0-9]+} followed by {@code _}, and no context name contains an
 * underscore, so PAPI can never match one of these. Placeholders belonging to another plugin that
 * happen to contain a dot are left alone too, because only the registered context names below are
 * ever substituted.
 * <p>
 * This is a static utility because the ordering of the whole parsing pipeline lives in
 * {@link com.extendedclip.deluxemenus.utils.StringUtils}, which has no plugin instance.
 */
public class InternalPlaceholderResolver {

    private static final Pattern PATTERN = Pattern
            .compile("%(?<context>[a-zA-Z0-9]+)\\.(?<key>[a-zA-Z0-9_.]+)%");

    private static final Map<String, ContextResolver> RESOLVERS = new HashMap<>();

    static {
        RESOLVERS.put("menu", new MenuContextResolver());
        RESOLVERS.put("viewer", new ViewerContextResolver());
        RESOLVERS.put("item", new ItemContextResolver());
        RESOLVERS.put("click", new ClickContextResolver());
    }

    private static volatile String trueValue = "true";
    private static volatile String falseValue = "false";

    private InternalPlaceholderResolver() {
    }

    /**
     * Sets the strings booleans are rendered as. Read from {@code config.yml} on load and reload.
     */
    public static void setBooleanValues(final @NotNull String trueValue, final @NotNull String falseValue) {
        InternalPlaceholderResolver.trueValue = trueValue;
        InternalPlaceholderResolver.falseValue = falseValue;
    }

    public static @NotNull String bool(final boolean value) {
        return value ? trueValue : falseValue;
    }

    /**
     * Replaces every known internal placeholder in the input.
     * <p>
     * Unknown contexts and unknown keys are left in the text untouched, and replacement values are
     * never scanned again, so a display name containing a {@code %} cannot cause a loop.
     */
    public static @NotNull String resolve(final @NotNull String input, final @NotNull PlaceholderContext context) {
        if (input.indexOf('%') == -1) {
            return input;
        }

        final Matcher matcher = PATTERN.matcher(input);

        if (!matcher.find()) {
            return input;
        }

        final StringBuilder builder = new StringBuilder();

        do {
            final ContextResolver resolver = RESOLVERS.get(matcher.group("context").toLowerCase());
            final String value = resolver == null
                    ? null
                    : resolver.resolve(matcher.group("key").toLowerCase(), context);

            matcher.appendReplacement(builder, Matcher.quoteReplacement(value == null ? matcher.group() : value));
        } while (matcher.find());

        matcher.appendTail(builder);

        return builder.toString();
    }
}
