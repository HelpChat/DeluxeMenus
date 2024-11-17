package com.extendedclip.deluxemenus.requirement;

import com.extendedclip.deluxemenus.menu.MenuHolder;

import java.util.List;

public class HasPermissionsRequirement extends Requirement {

    private final List<String> permissions;
    private final int minimum;
    private final boolean invert;

    public HasPermissionsRequirement(List<String> permissions, int minimum, boolean invert) {
        this.permissions = permissions;
        this.minimum = minimum;
        this.invert = invert;
    }

    @Override
    public boolean evaluate(MenuHolder holder) {
        int amount = 0;
        for (String permission : permissions) {
            String check = holder.setPlaceholdersAndArguments(permission);
            if (holder.getViewer().hasPermission(check)) {
                ++amount;
                continue;
            }
            if (minimum == -1) return invert;
        }
        if (invert) return permissions.size()-amount >= minimum;
        return amount >= minimum;
    }

}
