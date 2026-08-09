package com.extendedclip.deluxemenus.action;

import com.extendedclip.deluxemenus.menu.MenuHolder;
import com.extendedclip.deluxemenus.placeholder.internal.PlaceholderContext;
import org.jetbrains.annotations.NotNull;

public interface ClickHandler {

  void onClick(@NotNull final MenuHolder menuHolder, @NotNull final PlaceholderContext context);

  /**
   * Runs the handler with menu context only. Used by the open and close handlers, where there is no
   * item and no click to report.
   */
  default void onClick(@NotNull final MenuHolder menuHolder) {
    onClick(menuHolder, PlaceholderContext.of(menuHolder));
  }
}
