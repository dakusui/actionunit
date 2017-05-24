package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;

import static com.github.dakusui.actionunit.helpers.Checks.checkNotNull;

public interface Nested extends Action {
  Action getAction();

  abstract class Base extends ActionBase implements Nested {
    private final Action action;

    public Base(Action action) {
      this.action = checkNotNull(action);
    }

    @Override
    public Action getAction() {
      return this.action;
    }
  }
}
