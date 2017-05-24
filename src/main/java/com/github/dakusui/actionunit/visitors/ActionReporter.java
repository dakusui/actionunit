package com.github.dakusui.actionunit.visitors;

class ActionReporter extends ActionPrinter.Impl {
  /**
   * Creates an object of this class.
   *
   * @param writer A writer through which this object's output is printed.
   * @see Writer
   */
  ActionReporter(Writer writer) {
    super(writer);
  }
}
