package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;

import java.util.Formatter;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

public interface While extends Action {
  Predicate<Context> condition();

  Action perform();

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format("while (%s)", this.condition());
  }

  default void accept(Visitor visitor) {
    visitor.visit(this);
  }

  class Impl implements While {

    private final Action             action;
    private final Predicate<Context> condition;

    public Impl(Predicate<Context> condition, Action action) {
      this.action = requireNonNull(action);
      this.condition = requireNonNull(condition);
    }

    @Override
    public Predicate<Context> condition() {
      return this.condition;
    }

    @Override
    public Action perform() {
      return this.action;
    }

    @Override
    public String toString() {
      return String.format("%s", this);
    }
  }

  class Builder {
    private final Predicate<Context> predicate;
    private       Action             action;

    public Builder(Predicate<Context> predicate) {
      this.predicate = requireNonNull(predicate);
    }

    public Builder action(Action action) {
      this.action = requireNonNull(action);
      return this;
    }

    public While perform(Action action) {
      return this.action(action).build();
    }

    public While build() {
      return new Impl(this.predicate, this.action);
    }
  }
}
