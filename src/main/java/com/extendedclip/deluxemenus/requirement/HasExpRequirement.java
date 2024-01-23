package com.extendedclip.deluxemenus.requirement;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.MenuHolder;
import com.extendedclip.deluxemenus.utils.ExpUtils;

public class HasExpRequirement extends Requirement {

    private final boolean invert;
    private final boolean level;
    private final String amt;

    public HasExpRequirement(String amt, boolean invert, boolean level) {
        this.amt = amt;
        this.invert = invert;
        this.level = level;
    }

    @Override
    public boolean evaluate(MenuHolder holder, int slot) {
        int amount;
        int has = level ? holder.getViewer().getLevel() : ExpUtils.getTotalExperience(holder.getViewer());
        try {
            amount = Integer.parseInt(holder.setPlaceholders(amt, slot));
        } catch (final Exception exception) {
            DeluxeMenus.printStacktrace(
                "Invalid amount found for has exp requirement: " + holder.setPlaceholders(amt, slot),
                exception
            );
            return false;
        }
        if (has < amount) return invert;
        else return !invert;
    }
}
