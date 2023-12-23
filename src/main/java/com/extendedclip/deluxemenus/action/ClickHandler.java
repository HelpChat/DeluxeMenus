package com.extendedclip.deluxemenus.action;

import com.extendedclip.deluxemenus.menu.MenuHolder;
import org.jetbrains.annotations.NotNull;

public interface ClickHandler {

  void onClick(@NotNull final MenuHolder menuHolder);
}
