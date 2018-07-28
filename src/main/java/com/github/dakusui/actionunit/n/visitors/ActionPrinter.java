package com.github.dakusui.actionunit.n.visitors;

import com.github.dakusui.actionunit.n.core.Action;

public class ActionPrinter extends ActionScanner {
  public ActionPrinter() {
  }
  @Override
  protected void handleAction(Action action) {
    System.out.println(String.format("%s%s", indent(), action));
  }
}
