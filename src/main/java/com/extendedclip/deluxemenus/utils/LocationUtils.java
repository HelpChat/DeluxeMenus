package com.extendedclip.deluxemenus.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LocationUtils {

  /**
   * Serialize a location into a string
   *
   * @param loc Location to serialize
   * @return Serialized location string
   */
  @NotNull
  public static String serializeLocation(@NotNull final Location loc) {
    final World world = loc.getWorld();
    if (world == null) {
      return loc.getX() + "," +
          loc.getY() + "," +
          loc.getZ();
    }

    return loc.getWorld().getName() + "," +
        loc.getX() + "," +
        loc.getY() + "," +
        loc.getZ();
  }

  /**
   * Deserialize a location from a string
   *
   * @param loc Serialized location string
   * @return Location
   * @throws NumberFormatException If the coordinates are not valid doubles
   */
  @Nullable
  public static Location deserializeLocation(@NotNull final String loc) throws NumberFormatException {
    if (!loc.contains(",")) {
      return null;
    }

    String[] data = loc.split(",", 4);

    if (data.length < 3 || data.length > 4) {
      return null;
    }

    if (data.length == 3) {
      return new Location(
          null,
          Double.parseDouble(data[0]),
          Double.parseDouble(data[1]),
          Double.parseDouble(data[2]));
    }

    return new Location(
        Bukkit.getServer().getWorld(data[0]),
        Double.parseDouble(data[1]),
        Double.parseDouble(data[2]),
        Double.parseDouble(data[3]));
  }
}
