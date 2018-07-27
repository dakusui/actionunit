package com.github.dakusui.actionunit.n.actions;

import com.github.dakusui.actionunit.n.core.Action;
import com.github.dakusui.actionunit.n.core.context.ContextPredicate;

import static java.util.Objects.requireNonNull;

public interface When extends Action {
  ContextPredicate cond();

  Action perform();

  Action otherwise();

  @Override
  default void accept(Visitor visitor) {
    visitor.visit(this);
  }

  class Builder extends Action.Builder<When> {
    private final ContextPredicate cond;

    private Action otherwise = Leaf.NOP;
    private Action perform;

    public Builder(ContextPredicate cond) {
      this.cond = requireNonNull(cond);
    }

    public Builder perform(Action perform) {
      this.perform = requireNonNull(perform);
      return this;
    }

    public Builder otherwise(Action otherwise) {
      this.otherwise = requireNonNull(otherwise);
      return this;
    }

    @Override
    public When build() {
      requireNonNull(this.perform);
      return new When() {
        @Override
        public ContextPredicate cond() {
          return Builder.this.cond;
        }

        @Override
        public Action perform() {
          return Builder.this.perform;
        }

        @Override
        public Action otherwise() {
          return Builder.this.otherwise;
        }
      };
    }
  }
}
