package com.extendedclip.deluxemenus.menu.options;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CustomModelDataComponent {
    private List<String> colors = new ArrayList<>();
    private List<String> flags = new ArrayList<>();
    private List<String> floats = new ArrayList<>();
    private List<String> strings = new ArrayList<>();

    private CustomModelDataComponent() {
        // Private constructor to force builder usage
    }

    @NotNull
    public CustomModelDataComponent colors(@NotNull final List<@NotNull String> colors) {
        this.colors = colors;
        return this;
    }

    @NotNull
    public List<@NotNull String> colors() {
        return colors;
    }

    @NotNull
    public CustomModelDataComponent flags(@NotNull final List<@NotNull String> flags) {
        this.flags = flags;
        return this;
    }

    @NotNull
    public List<@NotNull String> flags() {
        return flags;
    }

    @NotNull
    public CustomModelDataComponent floats(@NotNull final List<@NotNull String> floats) {
        this.floats = floats;
        return this;
    }

    @NotNull
    public List<@NotNull String> floats() {
        return floats;
    }

    @NotNull
    public CustomModelDataComponent strings(@NotNull final List<@NotNull String> strings) {
        this.strings = strings;
        return this;
    }

    @NotNull
    public List<@NotNull String> strings() {
        return strings;
    }

    public static @NotNull CustomModelDataComponent builder() {
        return new CustomModelDataComponent();
    }
}
