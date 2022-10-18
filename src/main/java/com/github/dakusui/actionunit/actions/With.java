package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.core.Context;

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

    public Builder<V> thenAccept(Consumer<V> consumer) {
      return this.add(this.sourceAction.thenConsume(consumer));
    }


    public Builder<V> add(Action action) {
      this.actions.add(action);
      return this;
    }

    public static void main(String... args) {

    }


    public With<V> build() {
      return new With<V>() {
        @Override
        public Action begin() {
          return null;
        }

        @Override
        public Action perform() {
          return isParallel ?
              ActionSupport.parallel(actions) :
              ActionSupport.sequential(actions);
        }

        @Override
        public Action end() {
          return null;
        }

        @Override
        public String internalVariableName() {
          return null;
        }
      };
    }
  }
}
