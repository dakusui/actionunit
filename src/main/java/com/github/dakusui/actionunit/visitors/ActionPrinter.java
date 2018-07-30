package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.io.Writer;

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
