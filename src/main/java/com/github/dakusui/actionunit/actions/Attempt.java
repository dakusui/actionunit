package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.exceptions.ActionException;

import java.util.Formatter;
import java.util.function.Consumer;

import static com.github.dakusui.actionunit.core.ActionSupport.leaf;
import static com.github.dakusui.actionunit.core.ActionSupport.named;
import static java.util.Objects.requireNonNull;

public interface Attempt extends Action {
  @Override
  default void accept(Visitor visitor) {
    visitor.visit(this);
  }

  Action perform();

  Action recover();

  Action ensure();

  Class<? extends Throwable> targetExceptionClass();

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format("attempt");
  }

  class Builder extends Action.Builder<Attempt> {
    private Class<? extends Throwable> targetExceptionClass = Exception.class;
    private final Action perform;
    private Action recover = Named.of("recover",
        Leaf.of(
            (Context $) -> {
              if ($.wasExceptionThrown()) {
                throw ActionException.wrap($.thrownException());
              }
            }));
    private Action ensure = Named.of("ensure",
        ActionSupport.nop()
    );

    public Builder(Action perform) {
      this.perform = requireNonNull(perform);
    }

    public Builder recover(Class<? extends Throwable> targetExceptionClass, Consumer<Throwable> exceptionHandler) {
      this.recover = Named.of("recover", leaf(c -> {
        exceptionHandler.accept(c.thrownException());
      }));
      this.targetExceptionClass = requireNonNull(targetExceptionClass);
      return this;
    }

    public Builder recover(Class<? extends Throwable> targetExceptionClass, Action recover) {
      this.recover = Named.of("recover", requireNonNull(recover));
      this.targetExceptionClass = requireNonNull(targetExceptionClass);
      return this;
    }

    public Action ensure(Action ensure) {
      this.ensure = named("ensure", requireNonNull(ensure));
      return this.$();
    }

    public Attempt build() {
      return new Attempt() {

        @Override
        public Action perform() {
          return perform;
        }

        @Override
        public Action recover() {
          return recover;
        }

        public Class<? extends Throwable> targetExceptionClass() {
          return targetExceptionClass;
        }

        @Override
        public Action ensure() {
          return ensure;
        }
      };
    }
  }
}
