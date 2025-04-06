package com.extendedclip.deluxemenus.requirement;

import com.extendedclip.deluxemenus.menu.MenuHolder;

public class BooleanRequirement extends Requirement {

  private final String bool;
  private final boolean invert;

  public BooleanRequirement(String bool, boolean invert) {
    this.bool = bool;
    this.invert = invert;
  }

  @Override
  public boolean evaluate(MenuHolder holder) {
    String check = holder.setPlaceholdersAndArguments(bool);
    try {
      boolean bool = Boolean.parseBoolean(check);
      if (invert) {
        return !bool;
      } else {
        return bool;
      }
    } catch (Exception e) {
      return false;
    }
  }
}
