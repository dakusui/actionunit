package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.core.context.ContextPredicate;

import java.util.Formatter;

import static java.util.Objects.requireNonNull;

public interface When extends Action<When> {
  ContextPredicate cond();

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
    private final ContextPredicate cond;

    private Action otherwise = Named.of("else", ActionSupport.nop());
    private Action perform;

    public Builder(ContextPredicate cond) {
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
