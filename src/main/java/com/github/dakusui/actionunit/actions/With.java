package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.actionunit.core.context.FormattableConsumer;
import com.github.dakusui.pcond.forms.Printables;

import java.util.Formatter;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.core.context.FormattableConsumer.nopConsumer;
import static com.github.dakusui.actionunit.utils.InternalUtils.toStringIfOverriddenOrNoname;
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

  class Builder<V> extends Action.Builder<With<V>> {

    private final Contextful<V> sourceAction;
    private       Action        mainAction;
    private       String        name;

    public Builder(Function<Context, V> function) {
      this.sourceAction = new Contextful.Impl<>(requireNonNull(function));
    }

    public Builder<V> name(String name) {
      this.name = requireNonNull(name);
      return this;
    }

    public Builder<V> perform(Consumer<V> consumer) {
      this.mainAction = ActionSupport.leaf(consumer(consumer));
      return this;
    }

    public <W> Builder<W> andThen(Function<V, W> function) {
      return new Builder<>(function(function));
    }

    public <W> Function<Context, W> function(Function<V, W> function) {
      return Printables.function(
          () -> name() + ":" + toStringIfOverriddenOrNoname(function),
          context -> function.apply(context.valueOf(sourceAction.internalVariableName())));
    }

    public ContextConsumer consumer(Consumer<V> consumer) {
      return context -> consumer.accept(context.valueOf(sourceAction.internalVariableName()));
    }

    public Predicate<Context> predicate(Predicate<V> predicate) {
      return Printables.predicate(
          () -> name() + ":" + toStringIfOverriddenOrNoname(predicate),
          (Context context) -> predicate.test(context.valueOf(sourceAction.internalVariableName())));
    }

    private String name() {
      return this.name == null ? "(noname)" : this.name;
    }


    public Builder<V> action(Action action) {
      this.mainAction = requireNonNull(action);
      return this;
    }

    public Builder<V> action(Function<Builder<V>, Action> action) {
      return this.action(action.apply(this));
    }


    public With<V> build() {
      return build(nopConsumer());
    }

    public With<V> build(Consumer<V> finisher) {
      final Contextful<V> begin = Builder.this.sourceAction;
      final Action mainAction = Builder.this.mainAction;
      return new With<V>() {
        @Override
        public Action begin() {
          return begin;
        }

        @Override
        public Action perform() {
          return mainAction;
        }

        @Override
        public Action end() {
          return ActionSupport.simple(String.format("done:%s", finisher), ContextConsumer.of(() -> "***", new FormattableConsumer<Context>() {
            @Override
            public void accept(Context context) {
              V variable = context.valueOf(begin.internalVariableName());
              context.unassign(begin.internalVariableName()); // Unassign first. Otherwise, finisher may fail.
              finisher.accept(variable);
            }

            @Override
            public void formatTo(Formatter formatter, int i, int i1, int i2) {
              formatter.format("%s", toStringIfOverriddenOrNoname(finisher));
            }
          }));
        }
      };
    }
  }
}
