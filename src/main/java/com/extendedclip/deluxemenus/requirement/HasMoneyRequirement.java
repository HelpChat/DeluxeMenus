package com.extendedclip.deluxemenus.requirement;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.MenuHolder;
import com.extendedclip.deluxemenus.placeholder.internal.PlaceholderContext;
import org.jetbrains.annotations.NotNull;

public class HasMoneyRequirement extends Requirement {

  private final DeluxeMenus plugin;
  private final boolean invert;
  private final String placeholder;
  private double amount;

  public HasMoneyRequirement(@NotNull final DeluxeMenus plugin, double amount, boolean invert, String placeholder) {
    this.plugin = plugin;
    this.amount = amount;
    this.invert = invert;
    this.placeholder = placeholder;
  }

  @Override
  public boolean evaluate(MenuHolder holder, PlaceholderContext context) {
    if (plugin.getVault() == null) {
      return false;
    }

    if (placeholder != null) {
      try {
        String expected = holder.setPlaceholdersAndArguments(placeholder, context);
        amount = Double.parseDouble(expected);
      } catch (final NumberFormatException exception) {
        plugin.printStacktrace(
            "Invalid amount found for has money requirement: " + holder.setPlaceholdersAndArguments(placeholder, context),
            exception
        );
      }
    }
    if (invert) {
      return !plugin.getVault().hasEnough(holder.getViewer(), amount);
    } else {
      return plugin.getVault().hasEnough(holder.getViewer(), amount);
    }
  }
}
