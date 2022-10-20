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

    public Builder(String name, Function<Context, V> function) {
      this.sourceAction = new Contextful.Impl<>(name, requireNonNull(function));
    }

    public Builder<V> perform(Consumer<V> consumer) {
      this.mainAction = ActionSupport.leaf(consumer(consumer));
      return this;
    }

    public <W> Builder<W> andThen(Function<V, W> function) {
      return new Builder<>(next(sourceAction.variableName()), function(function));
    }

    private static String next(String variableName) {
      requireNonNull(variableName);
      if (variableName.length() == 1 && 'a' <= variableName.charAt(0) && variableName.charAt(0) <= 'z')
        return Character.toString((char) (variableName.charAt(0) + 1));
      if (variableName.matches(".*_[1-9][0-9]*$")) {
        int index = Integer.parseInt(variableName.replaceAll(".*_", "")) + 1;
        return variableName.replaceAll("_[1-9][0-9]*$", "_" + index);
      }
      return variableName + "_1";
    }


    public <W> Function<Context, W> function(Function<V, W> function) {
      return Printables.function(
          () -> sourceAction.variableName() + ":" + toStringIfOverriddenOrNoname(function),
          context -> function.apply(context.valueOf(sourceAction.internalVariableName())));
    }

    public ContextConsumer consumer(Consumer<V> consumer) {
      return context -> consumer.accept(context.valueOf(sourceAction.internalVariableName()));
    }

    public Predicate<Context> predicate(Predicate<V> predicate) {
      return Printables.predicate(
          () -> sourceAction.variableName() + ":" + toStringIfOverriddenOrNoname(predicate),
          (Context context) -> predicate.test(context.valueOf(sourceAction.internalVariableName())));
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
