package com.extendedclip.deluxemenus.requirement;

import com.extendedclip.deluxemenus.action.ClickHandler;
import com.extendedclip.deluxemenus.menu.MenuHolder;

public abstract class Requirement {

  private ClickHandler successHandler;
  private ClickHandler denyHandler;
  private boolean optional;

  public Requirement() {
    this.setOptional(false);
  }

  public Requirement(boolean optional) {
    this.setOptional(optional);
  }

  public abstract boolean evaluate(MenuHolder holder);

  public ClickHandler getDenyHandler() {
    return denyHandler;
  }

  public void setDenyHandler(ClickHandler denyHandler) {
    this.denyHandler = denyHandler;
  }

  public boolean hasDenyHandler() {
    return this.denyHandler != null;
  }

  public boolean isOptional() {
    return optional;
  }

  public void setOptional(boolean optional) {
    this.optional = optional;
  }

  public ClickHandler getSuccessHandler() {
    return successHandler;
  }

  public void setSuccessHandler(ClickHandler successHandler) {
    this.successHandler = successHandler;
  }
}
