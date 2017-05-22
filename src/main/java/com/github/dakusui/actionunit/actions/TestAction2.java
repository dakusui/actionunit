package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Action;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface TestAction2<I, O> extends Action {
  Action given(Supplier<I> given);

  Action when(Function<I, O> given);

  Action then(Predicate<O> then);

  class Builder<I, O> {
    Function<Supplier<I>, Action>    given;
    Function<Function<I, O>, Action> when;
    Function<Predicate<O>, Action>   then;

    public Builder() {
    }

    public Builder<I, O> given(Function<Supplier<I>, Action> given) {
      this.given = Objects.requireNonNull(given);
      return this;
    }

    public Builder<I, O> when(Function<Function<I, O>, Action> when) {
      this.when = Objects.requireNonNull(when);
      return this;
    }

    public Builder<I, O> then(Function<Predicate<O>, Action> then) {
      this.then = Objects.requireNonNull(then);
      return this;
    }

    public TestAction2<I, O> build() {
      return new TestAction2<I, O>() {
        @Override
        public Action given(Supplier<I> given) {
          return Builder.this.given.apply(given);
        }

        @Override
        public Action when(Function<I, O> when) {
          return Builder.this.when.apply(when);
        }

        @Override
        public Action then(Predicate<O> then) {
          return Builder.this.then.apply(then);
        }

        @Override
        public void accept(Visitor visitor) {
          visitor.visit(this);
        }
      };
    }
  }
}
