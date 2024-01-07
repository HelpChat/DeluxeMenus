package com.extendedclip.deluxemenus.requirement;

import com.extendedclip.deluxemenus.menu.MenuHolder;

public class StringLengthRequirement extends Requirement {

    private final String input;
    private final int min;
    private final Integer max;

    public StringLengthRequirement(String input, int min, Integer max) {
        this.input = input;
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean evaluate(MenuHolder holder) {
        String toCheck = holder.setPlaceholdersAndArguments(input);
        if (max == null) {
            return toCheck.length() >= min;
        } else {
            return toCheck.length() >= min && toCheck.length() <= max;
        }
    }
}
