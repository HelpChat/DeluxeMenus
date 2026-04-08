package com.extendedclip.deluxemenus.menu;

import com.extendedclip.deluxemenus.menu.options.MenuItemOptions;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class MenuItemTemplate {

    private final String templateKey;
    // PlaceholderAPI placeholder expected to return a comma-separated list at runtime (e.g. "%homes%")
    private final String basedOnPlaceholder;
    private final List<Integer> slots;
    private final MenuItemOptions baseOptions;

    // Raw command strings stored separately so <template:name> can be replaced before parsing into ClickHandlers
    private final List<String> clickCommands;
    private final List<String> leftClickCommands;
    private final List<String> rightClickCommands;
    private final List<String> shiftLeftClickCommands;
    private final List<String> shiftRightClickCommands;
    private final List<String> middleClickCommands;

    public MenuItemTemplate(
            final @NotNull String templateKey,
            final @NotNull String basedOnPlaceholder,
            final @NotNull List<Integer> slots,
            final @NotNull MenuItemOptions baseOptions,
            final @NotNull List<String> clickCommands,
            final @NotNull List<String> leftClickCommands,
            final @NotNull List<String> rightClickCommands,
            final @NotNull List<String> shiftLeftClickCommands,
            final @NotNull List<String> shiftRightClickCommands,
            final @NotNull List<String> middleClickCommands
    ) {
        this.templateKey = templateKey;
        this.basedOnPlaceholder = basedOnPlaceholder;
        this.slots = Collections.unmodifiableList(slots);
        this.baseOptions = baseOptions;
        this.clickCommands = Collections.unmodifiableList(clickCommands);
        this.leftClickCommands = Collections.unmodifiableList(leftClickCommands);
        this.rightClickCommands = Collections.unmodifiableList(rightClickCommands);
        this.shiftLeftClickCommands = Collections.unmodifiableList(shiftLeftClickCommands);
        this.shiftRightClickCommands = Collections.unmodifiableList(shiftRightClickCommands);
        this.middleClickCommands = Collections.unmodifiableList(middleClickCommands);
    }

    public @NotNull String getTemplateKey() {
        return templateKey;
    }

    public @NotNull String getBasedOnPlaceholder() {
        return basedOnPlaceholder;
    }

    public @NotNull List<Integer> getSlots() {
        return slots;
    }

    public @NotNull MenuItemOptions getBaseOptions() {
        return baseOptions;
    }

    public @NotNull List<String> getClickCommands() {
        return clickCommands;
    }

    public @NotNull List<String> getLeftClickCommands() {
        return leftClickCommands;
    }

    public @NotNull List<String> getRightClickCommands() {
        return rightClickCommands;
    }

    public @NotNull List<String> getShiftLeftClickCommands() {
        return shiftLeftClickCommands;
    }

    public @NotNull List<String> getShiftRightClickCommands() {
        return shiftRightClickCommands;
    }

    public @NotNull List<String> getMiddleClickCommands() {
        return middleClickCommands;
    }
}
