package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.core.Context;

import java.util.Formatter;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

public interface When extends Action {
  Predicate<Context> cond();

  Action perform();

  Action otherwise();

  @Override
  default void accept(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format("if [%s]", cond());
  }

  class Builder extends Action.Builder<When> {
    private final Predicate<Context> cond;

    private Action otherwise = Named.of("else", ActionSupport.nop());
    private Action perform;

    public Builder(Predicate<Context> cond) {
      this.cond = requireNonNull(cond);
    }

    public Builder perform(Action perform) {
      this.perform = Named.of("then", requireNonNull(perform));
      return this;
    }

    public Action otherwise(Action otherwise) {
      this.otherwise = Named.of("else", requireNonNull(otherwise));
      return this.$();
    }

    @Override
    public When build() {
      requireNonNull(this.perform);
      return new When() {
        @Override
        public Predicate<Context> cond() {
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
