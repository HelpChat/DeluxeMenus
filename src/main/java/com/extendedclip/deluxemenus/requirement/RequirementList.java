package com.extendedclip.deluxemenus.requirement;

import com.extendedclip.deluxemenus.action.ClickHandler;
import com.extendedclip.deluxemenus.menu.MenuHolder;
import java.util.List;

public class RequirementList {

  private List<Requirement> requirements;
  private ClickHandler denyHandler;
  private int minimumRequirements;
  private boolean stopAtSuccess;

  public RequirementList(List<Requirement> requirements) {
    this.setRequirements(requirements);
  }

  public boolean evaluate(MenuHolder holder) {
    int successful = 0;
    for (Requirement r : getRequirements()) {
      if (r.evaluate(holder)) {
        successful = successful + 1;
        if (r.getSuccessHandler() != null) {
          r.getSuccessHandler().onClick(holder);
        }
        if (this.stopAtSuccess && successful >= minimumRequirements) {
          break;
        }
      } else {
        if (r.getDenyHandler() != null) {
          r.getDenyHandler().onClick(holder);
        }
        if (!r.isOptional()) {
          return false;
        }
      }
    }
    return successful >= minimumRequirements;
  }

  public List<Requirement> getRequirements() {
    return requirements;
  }

  public void setRequirements(List<Requirement> requirements) {
    this.requirements = requirements;
  }

  public ClickHandler getDenyHandler() {
    return denyHandler;
  }

  public void setDenyHandler(ClickHandler denyHandler) {
    this.denyHandler = denyHandler;
  }

  public int getMinimumRequirements() {
    return minimumRequirements;
  }

  public void setMinimumRequirements(int minimumRequirements) {
    this.minimumRequirements = minimumRequirements;
  }

  public boolean stopAtSuccess() {
    return this.stopAtSuccess;
  }

  public void setStopAtSuccess(boolean stop) {
    this.stopAtSuccess = stop;
  }
}
