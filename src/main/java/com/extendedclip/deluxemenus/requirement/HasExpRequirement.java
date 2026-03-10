package com.extendedclip.deluxemenus.requirement;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.MenuHolder;
import com.extendedclip.deluxemenus.utils.ExpUtils;
import org.jetbrains.annotations.NotNull;

public class HasExpRequirement extends Requirement {

    private final DeluxeMenus plugin;
    private final boolean invert;
    private final boolean level;
    private final String amt;

    public HasExpRequirement(@NotNull final DeluxeMenus plugin, String amt, boolean invert, boolean level) {
        this.plugin = plugin;
        this.amt = amt;
        this.invert = invert;
        this.level = level;
    }

    @Override
    public boolean evaluate(MenuHolder holder) {
        int amount;
        int has = level ? holder.getViewer().getLevel() : ExpUtils.getTotalExperience(holder.getViewer());
        try {
            amount = Integer.parseInt(holder.setPlaceholdersAndArguments(amt));
        } catch (final Exception exception) {
            plugin.printStacktrace(
                "Invalid amount found for has exp requirement: " + holder.setPlaceholdersAndArguments(amt),
                exception
            );
            return false;
        }
        if (has < amount) return invert;
        else return !invert;
    }
}
