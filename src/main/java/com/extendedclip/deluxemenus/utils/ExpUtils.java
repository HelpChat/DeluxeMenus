package com.extendedclip.deluxemenus.utils;

import java.util.Locale;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * This is a utility class for handling and calculating experience points and levels.
 */
public final class ExpUtils {
    private ExpUtils() {
        throw new AssertionError("Util classes should not be initialized");
    }

    /**
     * Set the player's experience to the given value.
     * @param target The player to set the experience of.
     * @param stringAmount The amount of experience to set. Can end with an 'l' to set the experience levels.
     * @throws NumberFormatException If the stringAmount is not a valid number.
     */
    public static void setExp(
        @NotNull final Player target,
        @NotNull final String stringAmount
    ) throws NumberFormatException {
        long amount;
        final String lowerCase = stringAmount.toLowerCase(Locale.ENGLISH);

        if (stringAmount.contains("l")) {
            final int neededLevel = Integer.parseInt(lowerCase.replaceAll("l", "")) + target.getLevel();
            amount = getExpToLevel(neededLevel) + (getTotalExperience(target) - getExpToLevel(target.getLevel()));
            setTotalExperience(target, 0);
        }
        else {
            amount = Long.parseLong(lowerCase);
        }
        amount += getTotalExperience(target);
        if (amount > Integer.MAX_VALUE) {
            amount = Integer.MAX_VALUE;
        }
        if (amount < 0L) {
            amount = 0L;
        }
        setTotalExperience(target, (int) amount);
    }

    /**
     * Set the player's experience to the given value.
     * <br>
     * This method updates both the record total experience and displayed total experience.
     *
     * @param player The player to set the total experience for.
     * @param exp The amount of experience to set.
     * @throws IllegalArgumentException If the exp amount is less than 0.
     */
    public static void setTotalExperience(
        @NotNull final Player player,
        final int exp
    ) throws IllegalArgumentException {
        if (exp < 0) {
            throw new IllegalArgumentException("Experience is negative!");
        }

        player.setExp(0);
        player.setLevel(0);
        player.setTotalExperience(0);

        int amount = exp;
        while (amount > 0) {
            final int expToLevel = getExpAtLevel(player);
            amount -= expToLevel;
            if (amount >= 0) {
                player.giveExp(expToLevel);
            } else {
                amount += expToLevel;
                player.giveExp(amount);
                amount = 0;
            }
        }
    }

    /**
     * Get the amount of experience required to reach the given level.
     * @param player The player to get the experience for.
     * @return The amount of experience required to reach the given level.
     */
    private static int getExpAtLevel(@NotNull final Player player) {
        return getExpAtLevel(player.getLevel());
    }

    /**
     * Get the amount of experience points required to reach the next level from the given level.
     * @param level The level to calculate the required experience points to level up for.
     * @return The amount of experience required to reach the next level from the given one.
     */
    public static int getExpAtLevel(final int level) {
        if (level <= 15) {
            return (2 * level) + 7;
        }
        if (level <= 30) {
            return (5 * level) - 38;
        }
        return (9 * level) - 158;
    }

    /**
     * Translates the given amount of levels to the amount of experience points required to get to this level from 0.
     * @param level The amount of levels to translate.
     * @return The amount of experience points required to reach the given level from 0.
     */
    public static int getExpToLevel(final int level) {
        int currentLevel = 0;
        int exp = 0;

        while (currentLevel < level) {
            exp += getExpAtLevel(currentLevel);
            currentLevel++;
        }
        if (exp < 0) {
            exp = Integer.MAX_VALUE;
        }
        return exp;
    }

    /**
     * Get the total amount of experience points that the given player has right now.
     * @param player The player to get the experience for.
     * @return The total amount of experience points that the given player has.
     */
    public static int getTotalExperience(@NotNull final Player player) {
        int exp = Math.round(getExpAtLevel(player) * player.getExp());
        int currentLevel = player.getLevel();

        while (currentLevel > 0) {
            currentLevel--;
            exp += getExpAtLevel(currentLevel);
        }
        if (exp < 0) {
            exp = Integer.MAX_VALUE;
        }
        return exp;
    }
}
