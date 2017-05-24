package com.github.dakusui.actionunit.compat.visitors;

import com.github.dakusui.actionunit.compat.actions.CompatAttempt;
import com.github.dakusui.actionunit.compat.actions.CompatForEach;
import com.github.dakusui.actionunit.compat.actions.CompatWith;
import com.github.dakusui.actionunit.compat.actions.Piped;
import com.github.dakusui.actionunit.visitors.ActionPrinter;

import static com.github.dakusui.actionunit.helpers.Checks.checkNotNull;

public abstract class CompatActionPrinter extends ActionPrinter {

  public CompatActionPrinter(Writer writer) {
    super(writer);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> void visit(CompatWith<T> action) {
    writeLine(describeAction(action));
    if (!(action instanceof Piped)) {
      enter(action);
      try {
        action.getAction().accept(this);
      } finally {
        leave(action);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(CompatForEach action) {
    writeLine(describeAction(action));
    enter(action);
    try {
      action.getAction().accept(this);
    } finally {
      leave(action);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(CompatAttempt action) {
    writeLine(describeAction(action));
    enter(action);
    try {
      action.attempt.accept(this);
      action.recover.accept(this);
      action.ensure.accept(this);
    } finally {
      leave(action);
    }
  }

}
