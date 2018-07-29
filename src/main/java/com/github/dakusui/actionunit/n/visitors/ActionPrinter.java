package com.github.dakusui.actionunit.n.visitors;

import com.github.dakusui.actionunit.n.core.Action;
import com.github.dakusui.actionunit.n.io.Writer;

public class ActionPrinter extends ActionScanner {
  private final Writer writer;

  public ActionPrinter(Writer writer) {
    this.writer = writer;
  }

  @Override
  protected void handleAction(Action action) {
    writer.writeLine(String.format("%s%s", indent(), action));
  }
}
