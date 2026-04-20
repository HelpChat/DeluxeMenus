package com.extendedclip.deluxemenus.action;

import com.extendedclip.deluxemenus.menu.MenuHolder;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Longs;
import java.util.concurrent.ThreadLocalRandom;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClickAction {

  private ActionType type;
  private String executable;
  private String delay;
  private String chance;

  public ClickAction(@NotNull final ActionType type, @NotNull final String executable) {
    this.type = type;
    this.executable = executable;

    this.delay = null;
    this.chance = null;
  }

  /**
   * Get the {@link ActionType} of this action.
   *
   * @return the type
   */
  public @NotNull ActionType getType() {
    return type;
  }

  /**
   * Set the {@link ActionType} of this action.
   *
   * @param type the type to set
   */
  public void setType(@NotNull final ActionType type) {
    this.type = type;
  }

  /**
   * Get the executable of this action.
   *
   * @return the executable
   */
  public @NotNull String getExecutable() {
    return executable;
  }

  /**
   * Set the executable of this action.
   *
   * @param executable the executable to set
   */
  public void setExecutable(@NotNull String executable) {
    this.executable = executable;
  }

  /**
   * Checks if this action has a delay. This does not check if the delay is valid or not!
   *
   * @return true if there is a delay, false otherwise
   */
  public boolean hasDelay() {
    return delay != null;
  }

  /**
   * Set the delay of this action. If the delay is null or can't be parsed to a {@link Long}, there will be no delay.
   *
   * @param delay the delay to set
   */
  public void setDelay(@Nullable final String delay) {
    this.delay = delay;
  }

  /**
   * Get the unparsed chance of this action.
   *
   * @return the chance
   */
  public @Nullable String getChance() {
    return chance;
  }

  /**
   * Set the chance of this action. If the chance is null or can't be parsed to a {@link Double}, it will be considered
   * as 100%.
   *
   * @param chance the chance to set
   */
  public void setChance(@Nullable final String chance) {
    this.chance = chance;
  }

  /**
   * Get the parsed delay of this action. If the delay is null or can't be parsed to a {@link Long}, the delay will be 0.
   *
   * @param holder the holder to parse placeholders in the delay for.
   * @return the parsed delay
   */
  @SuppressWarnings("UnstableApiUsage")
  public long getDelay(@NotNull final MenuHolder holder) {
    if (delay == null || delay.isEmpty()) {
      return 0;
    }

    final var parsed = Longs.tryParse(holder.setPlaceholdersAndArguments(delay));
    return parsed == null ? 0 : parsed;
  }

  /**
   * Parses the chance of this action and tries it. If {@link #getChance()} is null this will return true but if it
   * can't be parsed to a {@link Double}, this will return false.
   *
   * @param holder the holder to parse placeholders in the chance for.
   * @return true if the chance has passed, false otherwise
   */
  @SuppressWarnings("UnstableApiUsage")
  public boolean checkChance(@NotNull final MenuHolder holder) {
    if (chance == null) {
      return true;
    }

    final Double parsedChance = Doubles.tryParse(holder.setPlaceholdersAndArguments(this.chance));
    if (parsedChance == null) {
      return false;
    }

    if (parsedChance >= 100.0) {
      return true;
    }

    // Generate a random number with a maximum of 2 decimals.
    final double random = ThreadLocalRandom.current().nextInt(10000) / 100.0;

    return random <= parsedChance;
  }
}
