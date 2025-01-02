package com.extendedclip.deluxemenus.requirement;

import com.extendedclip.deluxemenus.menu.MenuHolder;
import com.extendedclip.deluxemenus.utils.DebugLevel;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import org.bukkit.Bukkit;

import java.util.UUID;
import java.util.logging.Level;

public class IsObjectRequirement extends Requirement {

    private final String input;
    private final String object;

    public IsObjectRequirement(String input, String object) {
        this.input = input;
        this.object = object;
    }

    @Override
    public boolean evaluate(MenuHolder holder) {
        String toCheck = holder.setPlaceholdersAndArguments(input);

        switch (object) {
            case "int":
                return Ints.tryParse(toCheck) != null;
            case "double":
                return Doubles.tryParse(toCheck) != null;
            case "player":
                try {
                    UUID id = UUID.fromString(toCheck);
                    return Bukkit.getPlayer(id) != null;
                } catch (IllegalArgumentException e) {
                    return Bukkit.getPlayerExact(toCheck) != null;
                }
            case "uuid":
                try {
                    UUID.fromString(toCheck);
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            default:
                holder.getPlugin().debug(DebugLevel.HIGHEST, Level.INFO, "Invalid object: " + object + " in \"is object\" check.");
                return false;
        }
    }
}
