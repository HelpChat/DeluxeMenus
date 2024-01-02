package com.extendedclip.deluxemenus.requirement;

import com.extendedclip.deluxemenus.menu.MenuHolder;

public class HasPermissionRequirement extends Requirement {

    private final String perm;
    private final boolean invert;

    public HasPermissionRequirement(String permission, boolean invert) {
        this.perm = permission;
        this.invert = invert;
    }

    @Override
    public boolean evaluate(MenuHolder holder) {
        return holder.getViewer().hasPermission(holder.setPlaceholders(perm)) != invert;
    }

}
