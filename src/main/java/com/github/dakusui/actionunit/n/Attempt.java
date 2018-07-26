package com.github.dakusui.actionunit.n;

import com.github.dakusui.actionunit.exceptions.ActionException;

import static com.github.dakusui.actionunit.helpers.Checks.requireArgument;
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

  class Builder {
    private       Class<? extends Throwable> targetExceptionClass = Exception.class;
    private final Action                     perform;
    private       Action                     recover              = Leaf.create(context -> () -> {
      if (context.thrownException().isPresent()) {
        Throwable exception = context.thrownException().get();
        if (exception instanceof RuntimeException)
          throw (RuntimeException) exception;
        throw new ActionException(exception);
      }
    });
    private       Action                     ensure               = Actions.nop();

    public Builder(Action perform) {
      this.perform = requireNonNull(perform);
    }

    public Builder recover(Class<? extends Throwable> targetExceptionClass, Action recover) {
      this.recover = requireNonNull(recover);
      this.targetExceptionClass = requireArgument(
          t -> Exception.class.isAssignableFrom(t.getClass()),
          requireNonNull(targetExceptionClass)
      );
      return this;
    }

    public Builder ensure(Action ensure) {
      this.ensure = requireNonNull(ensure);
      return this;
    }

    Attempt build() {
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

    Attempt $() {
      return build();
    }
  }
}
