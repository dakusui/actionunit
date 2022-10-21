package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.actionunit.core.context.FormattableConsumer;
import com.github.dakusui.printables.PrintableFunctionals;

import java.util.Formatter;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.core.context.FormattableConsumer.nopConsumer;
import static com.github.dakusui.actionunit.utils.InternalUtils.toStringIfOverriddenOrNoname;
import static java.util.Objects.requireNonNull;

public interface With extends Action {
  @Override
  default void accept(Visitor visitor) {
    visitor.visit(this);
  }

  Contextful<?> begin();

  Action perform();

  Action end();

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format("with:" + begin().variableName());
  }

  class Builder<V> extends Action.Builder<With> {

    private final Contextful<V> sourceAction;
    private       Action        mainAction;
    private final String        internalVariableName;
    private final String        variableName;

    public Builder(String name, Function<Context, V> function) {
      this.sourceAction = new Contextful.Impl<>(name, requireNonNull(function));
      this.internalVariableName = sourceAction.internalVariableName();
      this.variableName = sourceAction.variableName();
    }

    public Builder<V> perform(Consumer<V> consumer) {
      this.mainAction = variableReferencingAction(consumer);
      return this;
    }

    public Action variableUpdatingAction(Function<V, V> function) {
      return ActionSupport.simple(
          toStringIfOverriddenOrNoname(function) + ":" + variableName + "*",
          (Context c) -> variableUpdateFunction(function).apply(c));
    }

    public Action variableReferencingAction(Consumer<V> consumer) {
      return ActionSupport.simple(
          toStringIfOverriddenOrNoname(consumer) + ":" + variableName,
          (Context c) -> variableReferenceConsumer(consumer).accept(c));
    }

    public <W> Builder<W> andThen(Function<V, W> function) {
      return new Builder<>(nextVariableName(variableName), function(function));
    }

    private static String nextVariableName(String variableName) {
      requireNonNull(variableName);
      if (variableName.length() == 1 && 'a' <= variableName.charAt(0) && variableName.charAt(0) <= 'z')
        return Character.toString((char) (variableName.charAt(0) + 1));
      if (variableName.matches(".*_[1-9][0-9]*$")) {
        int index = Integer.parseInt(variableName.replaceAll(".*_", "")) + 1;
        return variableName.replaceAll("_[1-9][0-9]*$", "_" + index);
      }
      return variableName + "_1";
    }


    public Function<Context, V> variableUpdateFunction(Function<V, V> function) {
      return PrintableFunctionals.printableFunction(
              (Context context) -> {
                V ret = function.apply(context.valueOf(internalVariableName));
                context.assignTo(internalVariableName, ret);
                return ret;
              })
          .describe("XYZ");
    }

    public Consumer<Context> variableReferenceConsumer(Consumer<V> consumer) {
      return PrintableFunctionals.printableConsumer(
              (Context context) -> consumer.accept(context.valueOf(internalVariableName)))
          .describe("XYZ");
    }


    public <W> Function<Context, W> function(Function<V, W> function) {
      return PrintableFunctionals.printableFunction(
              (Context context) -> function.apply(context.valueOf(internalVariableName)))
          .describe(() -> variableName + ":" + toStringIfOverriddenOrNoname(function));
    }

    public ContextConsumer consumer(Consumer<V> consumer) {
      return context -> consumer.accept(context.valueOf(internalVariableName));
    }

    public Predicate<Context> predicate(Predicate<V> predicate) {
      return PrintableFunctionals.printablePredicate(
              (Context context) -> predicate.test(context.valueOf(internalVariableName)))
          .describe(() -> variableName + ":" + toStringIfOverriddenOrNoname(predicate));
    }


    public Builder<V> action(Action action) {
      this.mainAction = requireNonNull(action);
      return this;
    }

    public Builder<V> action(Function<Builder<V>, Action> action) {
      return this.action(action.apply(this));
    }

    public With build() {
      return build(nopConsumer());
    }

    public With build(Consumer<V> finisher) {
      final Contextful<V> begin = Builder.this.sourceAction;
      final Action mainAction = Builder.this.mainAction;
      return new With() {

        private final Action end = ActionSupport.simple(String.format("done:%s", finisher), ContextConsumer.of(() -> String.format("cleanUp:%s", begin.variableName()), new FormattableConsumer<Context>() {
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

        @Override
        public Contextful<V> begin() {
          return begin;
        }

        @Override
        public Action perform() {
          return mainAction;
        }

        @Override
        public Action end() {
          return end;
        }
      };
    }
  }
}
