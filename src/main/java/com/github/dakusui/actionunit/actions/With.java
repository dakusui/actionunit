package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.actionunit.utils.InternalUtils;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

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

    private final Function<Context, V>            function;
    private final List<Function<With<V>, Action>> actions    = new LinkedList<>();
    private       boolean                         isParallel = false;

    public Builder(Function<Context, V> function) {
      this.function = requireNonNull(function);
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

    public Builder<V> add(Consumer<V> consumer) {
      this.actions.add(w -> ActionSupport.leaf(
          ContextConsumer.of(
              () -> InternalUtils.toStringIfOverriddenOrNoname(consumer),
              (ContextConsumer) context -> consumer.accept(context.valueOf(w.internalVariableName())))));
      return this;
    }

    public Builder<V> add(Function<Builder<V>, Action> action) {
      return this;
    }

    public static void main(String... args) {

    }


    public With<V> build() {
      return null;
    }
  }
}
