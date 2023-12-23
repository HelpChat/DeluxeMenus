package com.extendedclip.deluxemenus.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

public class StringUtils {

  private final static Pattern HEX_PATTERN = Pattern
      .compile("&(#[a-f0-9]{6})", Pattern.CASE_INSENSITIVE);

  /**
   * Translates the ampersand color codes like '&7' to their section symbol counterparts like '§7'.
   * <br>
   * It also translates hex colors like '&#aaFF00' to their section symbol counterparts like '§x§a§a§F§F§0§0'.
   *
   * @param input The string in which to translate the color codes.
   * @return The string with the translated colors.
   */
  @NotNull
  public static String color(@NotNull String input) {
    //Hex Support for 1.16.1+
    Matcher m = HEX_PATTERN.matcher(input);
    if (VersionHelper.IS_HEX_VERSION) {
      while (m.find()) {
        input = input.replace(m.group(), ChatColor.of(m.group(1)).toString());
      }
    }

    return ChatColor.translateAlternateColorCodes('&', input);
  }

}
