package com.extendedclip.deluxemenus.requirement;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.MenuHolder;

public class HasEphemeralCooldownRequirement extends Requirement {

  private final DeluxeMenus plugin;
  private final String id;
  private final boolean invert;

  public HasEphemeralCooldownRequirement(DeluxeMenus plugin, String id, boolean invert) {
    this.plugin = plugin;
    this.id = id;
    this.invert = invert;
  }

  @Override
  public boolean evaluate(MenuHolder holder) {
    String check = holder.setPlaceholdersAndArguments(id);
    boolean onCooldown = plugin.getEphemeralCooldownManager()
        .isOnCooldown(holder.getViewer().getUniqueId(), check);
    return invert != onCooldown;
  }

}
