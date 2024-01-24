package com.extendedclip.deluxemenus.requirement;

import com.extendedclip.deluxemenus.menu.MenuHolder;
import java.util.regex.Pattern;

public class RegexMatchesRequirement extends Requirement {

  private final Pattern pattern;
  private final String input;
  private final boolean invert;

  public RegexMatchesRequirement(Pattern pattern, String input, boolean invert) {
    this.pattern = pattern;
    this.input = input;
    this.invert = invert;
  }

  @Override
  public boolean evaluate(MenuHolder holder, int slot) {
    String toCheck = holder.setPlaceholders(input, slot);
    if (invert) {
      return !pattern.matcher(holder.setPlaceholders(toCheck, slot)).find();
    } else {
      return pattern.matcher(holder.setPlaceholders(toCheck, slot)).find();
    }
  }
}
