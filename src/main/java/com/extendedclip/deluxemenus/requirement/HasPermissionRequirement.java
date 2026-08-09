package com.extendedclip.deluxemenus.requirement;

import com.extendedclip.deluxemenus.menu.MenuHolder;
import com.extendedclip.deluxemenus.placeholder.internal.PlaceholderContext;

public class HasPermissionRequirement extends Requirement {

  private final String perm;
  private final boolean invert;

  public HasPermissionRequirement(String permission, boolean invert) {
    this.perm = permission;
    this.invert = invert;
  }

  @Override
  public boolean evaluate(MenuHolder holder, PlaceholderContext context) {
    String check = holder.setPlaceholdersAndArguments(perm, context);
    if (invert) {
      return !holder.getViewer().hasPermission(check);
    } else {
      return holder.getViewer().hasPermission(check);
    }
  }

}
