package com.extendedclip.deluxemenus.commandpanels;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandPanelsConversionResult {

    private int filesRead;
    private int menusConverted;
    private int menusSkipped;
    private final List<String> warnings = new ArrayList<>();

    public void fileRead() {
        filesRead++;
    }

    public void menuConverted() {
        menusConverted++;
    }

    public void menuSkipped() {
        menusSkipped++;
    }

    public void warn(final @NotNull String warning) {
        warnings.add(warning);
    }

    public int filesRead() {
        return filesRead;
    }

    public int menusConverted() {
        return menusConverted;
    }

    public int menusSkipped() {
        return menusSkipped;
    }

    public @NotNull List<String> warnings() {
        return Collections.unmodifiableList(warnings);
    }
}
