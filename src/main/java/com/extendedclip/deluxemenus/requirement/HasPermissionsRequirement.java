package com.extendedclip.deluxemenus.requirement;

import com.extendedclip.deluxemenus.menu.MenuHolder;
import com.extendedclip.deluxemenus.placeholder.internal.PlaceholderContext;

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
    public boolean evaluate(MenuHolder holder, PlaceholderContext context) {
        final int count = permissions.stream()
                .map(permission -> holder.setPlaceholdersAndArguments(permission, context))
                .map(holder.getViewer()::hasPermission)
                .mapToInt(hasPermission -> hasPermission ? 1 : 0)
                .sum();
        return invert
                ? count + minimum <= permissions.size()
                : count >= minimum;
    }


}
