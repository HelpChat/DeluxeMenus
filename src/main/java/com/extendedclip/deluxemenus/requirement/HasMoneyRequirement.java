package com.extendedclip.deluxemenus.requirement;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.MenuHolder;

public class HasMoneyRequirement extends Requirement {

    private final boolean invert;
    private final String placeholder;
    private double amount;

    public HasMoneyRequirement(double amount, boolean invert, String placeholder) {
        this.amount = amount;
        this.invert = invert;
        this.placeholder = placeholder;
    }

    @Override
    public boolean evaluate(MenuHolder holder) {
        if (getInstance().getVault() == null) return false;

        if (placeholder != null) {
            String expected = holder.setPlaceholders(placeholder);
            try {
                amount = Double.parseDouble(expected);
            } catch (final NumberFormatException e) {
                DeluxeMenus.printStacktrace("Invalid amount found for has money requirement: " + expected, e);
            }
        }
        return getInstance().getVault().hasEnough(holder.getViewer(), amount) != invert;
    }
}
