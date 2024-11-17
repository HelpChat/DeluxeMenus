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
        final int count = permissions.stream()
                .map(holder::setPlaceholdersAndArguments)
                .map(holder.getViewer()::hasPermission)
                .mapToInt(hasPermission -> hasPermission ? 1 : 0)
                .sum();
        return invert
                ? count + minimum <= permissions.size()
                : count >= minimum;
    }


}
