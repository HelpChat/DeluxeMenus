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
    public boolean evaluate(MenuHolder holder) {
        int amount;
        int has = level ? holder.getViewer().getLevel() : ExpUtils.getTotalExperience(holder.getViewer());
        String expected = holder.setPlaceholders(amt);
        try {
            amount = Integer.parseInt(expected);
        } catch (final Exception e) {
            DeluxeMenus.printStacktrace("Invalid amount found for has exp requirement: " + expected, e);
            return false;
        }
        return has < amount == invert;
    }
}
