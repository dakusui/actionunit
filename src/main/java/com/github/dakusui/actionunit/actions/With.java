package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.actionunit.core.context.FormattableConsumer;
import com.github.dakusui.actionunit.utils.InternalUtils;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.dakusui.actionunit.core.context.ContextConsumer.NOP_CONSUMER;
import static com.github.dakusui.actionunit.core.context.FormattableConsumer.nopConsumer;
import static java.util.Objects.requireNonNull;

public interface With<V> extends Action {
  @Override
  default void accept(Visitor visitor) {
    visitor.visit(this);
  }

  Action begin();

  Action perform();

  Action end();

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format("attempt");
  }

  String internalVariableName();

  class Builder<V> extends Action.Builder<With<V>> {

    private final Contextful<V> sourceAction;
    private final List<Action>  actions    = new LinkedList<>();
    private       boolean       isParallel = false;

    public Builder(Function<Context, V> function) {
      this.sourceAction = new Contextful.Impl<>(requireNonNull(function));
      this.sequential();
    }

    public Builder<V> parallel() {
      this.isParallel = true;
      return this;
    }

    public Builder<V> sequential() {
      this.isParallel = false;
      return this;
    }

    public Builder<V> thenConsumeBy(Consumer<V> consumer) {
      return this.add(this.sourceAction.thenConsumeWith(consumer));
    }

    public <W> Builder<W> thenApply(Function<V, W> function) {
      Builder<W> ret = new Builder<>(context -> function.apply(context.valueOf(sourceAction.internalVariableName())));
      this.add(this.sourceAction.thenApply(function));
      return ret;
    }


    public Builder<V> add(Action action) {
      this.actions.add(action);
      return this;
    }

    public With<V> build() {
      return build(nopConsumer());
    }

    public With<V> build(Consumer<V> finisher) {
      final Contextful<V> begin = Builder.this.sourceAction;
      final Action actions = Builder.this.isParallel ?
          ActionSupport.parallel(Builder.this.actions) :
          ActionSupport.sequential(Builder.this.actions);
      return new With<V>() {
        @Override
        public Action begin() {
          return begin;
        }

        @Override
        public Action perform() {
          return actions;
        }

        @Override
        public Action end() {
          return ActionSupport.simple("", ContextConsumer.of(() -> "", new FormattableConsumer<Context>() {
            @Override
            public void accept(Context context) {
              V variable = context.valueOf(begin.internalVariableName());
              context.unassign(begin.internalVariableName()); // Unassign first. Otherwise, finisher may fail.
              finisher.accept(variable);
            }

            @Override
            public void formatTo(Formatter formatter, int i, int i1, int i2) {
              formatter.format("%s", InternalUtils.toStringIfOverriddenOrNoname(finisher));
            }

          }));
        }

        @Override
        public String internalVariableName() {
          return begin.internalVariableName();
        }
      };
    }
  }
}
