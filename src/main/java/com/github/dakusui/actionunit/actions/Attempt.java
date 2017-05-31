package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.helpers.Checks;

import java.util.Objects;
import java.util.function.Supplier;

public interface Attempt<E extends Throwable> extends Action {
  Action attempt();

  Class<E> exceptionClass();

  Action recover(Supplier<E> exception);

  Action ensure();

  static <E extends Throwable> Attempt.Builder<E> builder(Action attempt) {
    return new Builder<>(attempt);
  }

  class Builder<E extends Throwable> {
    private final Action attempt;
    private Action             ensure                  = ActionSupport.nop();
    private Class<? extends E> exceptionClass          = null;
    private HandlerFactory<E>  exceptionHandlerFactory = e -> {
      throw Checks.propagate(e.get());
    };

    public Builder(Action attempt) {
      this.attempt = Objects.requireNonNull(attempt);
    }

    public Builder<E> recover(Class<? extends E> exceptionClass, HandlerFactory<E> exceptionHandlerFactory) {
      this.exceptionClass = Objects.requireNonNull(exceptionClass);
      this.exceptionHandlerFactory = Objects.requireNonNull(exceptionHandlerFactory);
      return this;
    }

    public Attempt<E> ensure(Action action) {
      this.ensure = ActionSupport.named("Ensure", Objects.requireNonNull(action));
      return this.build();
    }

    @SuppressWarnings("unchecked")
    public Attempt<E> build() {
      Checks.checkState(exceptionClass != null, "Exception class isn't set yet.");
      return new Impl<>(attempt, (Class<E>) exceptionClass, exceptionHandlerFactory, ensure);
    }
  }

  class Impl<E extends Throwable> extends ActionBase implements Attempt<E> {
    private final Action            attempt;
    private final Action            ensure;
    private final Class<E>          exceptionClass;
    private final HandlerFactory<E> exceptionHandlerFactory;

    public Impl(Action attempt, Class<E> exceptionClass, HandlerFactory<E> exceptionHandlerFactory, Action ensure) {
      this.attempt = Objects.requireNonNull(attempt);
      this.exceptionClass = Objects.requireNonNull(exceptionClass);
      this.exceptionHandlerFactory = Objects.requireNonNull(exceptionHandlerFactory);
      this.ensure = Objects.requireNonNull(ensure);
    }

    @Override
    public Action attempt() {
      return this.attempt;
    }

    @Override
    public Class<E> exceptionClass() {
      return this.exceptionClass;
    }

    @Override
    public Action recover(Supplier<E> exception) {
      return ActionSupport.named(String.format("Recover(%s)", exceptionClass.getSimpleName()), exceptionHandlerFactory.apply(exception));
    }

    @Override
    public Action ensure() {
      return this.ensure;
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }
  }
}
