package com.extendedclip.deluxemenus.requirement;

import com.extendedclip.deluxemenus.DeluxeMenus;
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
        String parsedInput = holder.setPlaceholders(input);
        String parsedResult = holder.setPlaceholders(result);

        switch (type) {
            case STRING_CONTAINS: return parsedInput.contains(parsedResult);
            case STRING_EQUALS: return parsedInput.equals(parsedResult);
            case STRING_EQUALS_IGNORECASE: return parsedInput.equalsIgnoreCase(parsedResult);
            case STRING_DOES_NOT_CONTAIN: return !parsedInput.contains(parsedResult);
            case STRING_DOES_NOT_EQUAL: return !parsedInput.equals(parsedResult);
            case STRING_DOES_NOT_EQUAL_IGNORECASE: return !parsedInput.equalsIgnoreCase(parsedResult);
        }

        double in;
        double res;

        try {
            in = Double.parseDouble(parsedInput);
        } catch (final NumberFormatException e) {
            DeluxeMenus.printStacktrace("Input for comparison requirement is an invalid number: " + parsedInput, e);
            return false;
        }

        try {
            res = Double.parseDouble(parsedResult);
        } catch (final NumberFormatException e) {
            DeluxeMenus.printStacktrace("Output for comparison requirement is an invalid number: " + parsedResult, e);
            return false;
        }

        switch (type) {
            case GREATER_THAN: return in > res;
            case GREATER_THAN_EQUAL_TO: return in >= res;
            case EQUAL_TO: return in == res;
            case NOT_EQUAL_TO: return in != res;
            case LESS_THAN_EQUAL_TO: return in <= res;
            case LESS_THAN: return in < res;
        }
        return false;
    }
}
