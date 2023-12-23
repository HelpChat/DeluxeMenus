package com.extendedclip.deluxemenus.requirement;

import com.extendedclip.deluxemenus.menu.MenuHolder;
import org.bukkit.Location;

public class IsNearRequirement extends Requirement {

  private final Location location;
  private final int distance;
  private final boolean invert;

  public IsNearRequirement(Location location, int distance, boolean invert) {
    this.location = location;
    this.distance = distance;
    this.invert = invert;
  }

  @Override
  public boolean evaluate(MenuHolder holder) {
    if (holder.getViewer() == null) {
      return false;
    }
    boolean withinRange = false;
    if (holder.getViewer().getWorld().getName().equals(location.getWorld().getName())) {
      withinRange = holder.getViewer().getLocation().distance(location) < distance;
    }
    return invert ? !withinRange : withinRange;
  }
}
