package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.core.Action;

import static com.github.dakusui.actionunit.helpers.Utils.describe;

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

  protected String describeAction(Action action) {
    return describe(action) + ":" + System.identityHashCode(action);
  }
}

