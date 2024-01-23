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
  public boolean evaluate(MenuHolder holder, int slot) {
    if (getInstance().getVault() == null) {
      return false;
    }

    if (placeholder != null) {
      try {
        String expected = holder.setPlaceholders(placeholder, slot);
        amount = Double.parseDouble(expected);
      } catch (final NumberFormatException exception) {
        DeluxeMenus.printStacktrace(
            "Invalid amount found for has money requirement: " + holder.setPlaceholders(placeholder, slot),
            exception
        );
      }
    }
    if (invert) {
      return !getInstance().getVault().hasEnough(holder.getViewer(), amount);
    } else {
      return getInstance().getVault().hasEnough(holder.getViewer(), amount);
    }
  }
}
