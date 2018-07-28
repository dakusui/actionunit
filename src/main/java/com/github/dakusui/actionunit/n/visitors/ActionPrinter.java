package com.github.dakusui.actionunit.n.visitors;

import com.github.dakusui.actionunit.n.core.Action;
import org.slf4j.Logger;

public class ActionPrinter extends ActionScanner {
  private final Logger logger;

  public ActionPrinter(Logger logger) {
    this.logger = logger;
  }
  @Override
  protected void handleAction(Action action) {
    System.out.println(String.format("%s%s", indent(), action));
  }
}
