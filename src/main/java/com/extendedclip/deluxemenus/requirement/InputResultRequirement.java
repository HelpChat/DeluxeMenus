package com.extendedclip.deluxemenus.requirement;

import com.extendedclip.deluxemenus.menu.MenuHolder;

public class InputResultRequirement extends Requirement {

  private final String input;
  private final String result;
  private final RequirementType type;

  public InputResultRequirement(RequirementType type, String input, String result) {
    this.input = input;
    this.result = result;
    this.type = type;
  }

  @Override
  public boolean evaluate(MenuHolder holder) {

    String parsedInput = holder.setPlaceholdersAndArguments(this.input);
    String parsedResult = holder.setPlaceholdersAndArguments(this.result);

    switch (type) {
      case STRING_CONTAINS:
        return parsedInput.contains(parsedResult);
      case STRING_EQUALS:
        return parsedInput.equals(parsedResult);
      case STRING_EQUALS_IGNORECASE:
        return parsedInput.equalsIgnoreCase(parsedResult);
      case STRING_DOES_NOT_CONTAIN:
        return !parsedInput.contains(parsedResult);
      case STRING_DOES_NOT_EQUAL:
        return !parsedInput.equals(parsedResult);
      case STRING_DOES_NOT_EQUAL_IGNORECASE:
        return !parsedInput.equalsIgnoreCase(parsedResult);
      case STRING_CONTAINS_IGNORECASE:
        return parsedInput.toLowerCase().contains(parsedResult.toLowerCase());
      case STRING_DOES_NOT_CONTAIN_IGNORECASE:
        return !parsedInput.toLowerCase().contains(parsedResult.toLowerCase());
      default:
        break;
    }

    double in;
    double res;

    try {
      in = Double.parseDouble(parsedInput);
    } catch (final NumberFormatException exception) {
      holder.getPlugin().printStacktrace(
         "Input for comparison requirement is an invalid number: " + parsedInput,
          exception
      );
      return false;
    }

    try {
      res = Double.parseDouble(parsedResult);
    } catch (final NumberFormatException exception) {
      holder.getPlugin().printStacktrace(
          "Output for comparison requirement is an invalid number: " + parsedResult,
          exception
      );
      return false;
    }

    switch (type) {
      case GREATER_THAN:
        return in > res;
      case GREATER_THAN_EQUAL_TO:
        return in >= res;
      case EQUAL_TO:
        return in == res;
      case NOT_EQUAL_TO:
        return in != res;
      case LESS_THAN_EQUAL_TO:
        return in <= res;
      case LESS_THAN:
        return in < res;
      default:
        break;
    }
    return false;
  }
}

