package com.extendedclip.deluxemenus.requirement;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.MenuHolder;
import org.jetbrains.annotations.NotNull;

public class HasMoneyRequirement extends Requirement {

  private final DeluxeMenus plugin;
  private final boolean invert;
  private final String placeholder;
  private final double amount;

  public HasMoneyRequirement(@NotNull final DeluxeMenus plugin, double amount, boolean invert, String placeholder) {
    this.plugin = plugin;
    this.amount = amount;
    this.invert = invert;
    this.placeholder = placeholder;
  }

  @Override
  public boolean evaluate(MenuHolder holder) {
    if (plugin.getVault() == null) {
      return false;
    }

    double requiredAmount = amount;
    if (placeholder != null) {
      final String expected = holder.setPlaceholdersAndArguments(placeholder);
      try {
        requiredAmount = Double.parseDouble(expected);
      } catch (final NumberFormatException exception) {
        plugin.printStacktrace("Invalid amount found for has money requirement", exception);
        return false;
      }
    }

    if (!Double.isFinite(requiredAmount) || requiredAmount < 0) {
      return false;
    }

    if (invert) {
      return !plugin.getVault().hasEnough(holder.getViewer(), requiredAmount);
    } else {
      return plugin.getVault().hasEnough(holder.getViewer(), requiredAmount);
    }
  }
}
