package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionFactory;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.helpers.Checks;

import java.util.Objects;
import java.util.function.Supplier;

public interface Attempt<E extends Throwable> extends Action, ActionFactory {
  Action attempt();

  Class<E> exceptionClass();

  Action recover(Supplier<E> exception);

  Action ensure();

  static <E extends Throwable> Attempt.Builder<E> builder(int id, Action attempt) {
    return new Builder<>(id, attempt);
  }

  class Builder<E extends Throwable> {
    private final Action attempt;
    private final int    id;
    @SuppressWarnings("unchecked")
    private Class<? extends E>     exceptionClass          = (Class<? extends E>) Exception.class;
    private HandlerFactory<E>      exceptionHandlerFactory = ($, data) -> {
      throw Checks.propagate(data.get());
    };
    private HandlerFactory<Object> ensureHandlerFactory    = ($, _void) -> $.nop();

    public Builder(int id, Action attempt) {
      this.id = id;
      this.attempt = Objects.requireNonNull(attempt);
    }

    public Builder<E> recover(Class<? extends E> exceptionClass, HandlerFactory<E> exceptionHandlerFactory) {
      this.exceptionClass = Objects.requireNonNull(exceptionClass);
      this.exceptionHandlerFactory = Objects.requireNonNull(exceptionHandlerFactory);
      return this;
    }

    public Attempt<E> ensure(HandlerFactory<Object> ensureHandlerFactory) {
      this.ensureHandlerFactory = Objects.requireNonNull(ensureHandlerFactory);
      return this.build();
    }

    @SuppressWarnings("unchecked")
    public Attempt<E> build() {
      Checks.checkState(exceptionClass != null, "Exception class isn't set yet.");
      return new Impl<>(id, attempt, (Class<E>) exceptionClass, exceptionHandlerFactory, ensureHandlerFactory);
    }
  }

  class Impl<E extends Throwable> extends ActionBase implements Attempt<E> {
    private final Action                 attempt;
    private final Class<E>               exceptionClass;
    private final HandlerFactory<E>      exceptionHandlerFactory;
    private final HandlerFactory<Object> ensureHandlerFactory;

    public Impl(int id, Action attempt, Class<E> exceptionClass, HandlerFactory<E> exceptionHandlerFactory, HandlerFactory<Object> ensureHandlerFactory) {
      super(id);
      this.attempt = Objects.requireNonNull(attempt);
      this.exceptionClass = Objects.requireNonNull(exceptionClass);
      this.exceptionHandlerFactory = Objects.requireNonNull(exceptionHandlerFactory);
      this.ensureHandlerFactory = Objects.requireNonNull(ensureHandlerFactory);
    }

    @Override
    public Action attempt() {
      return ActionSupport.Internal.named(0, "Target", this.attempt);
    }

    @Override
    public Class<E> exceptionClass() {
      return this.exceptionClass;
    }

    @Override
    public Action recover(Supplier<E> exception) {
      return ActionSupport.Internal.named(1, String.format("Recover(%s)", exceptionClass.getSimpleName()), exceptionHandlerFactory.apply(exception));
    }

    @Override
    public Action ensure() {
      return ActionSupport.Internal.named(2, "Ensure", this.ensureHandlerFactory.apply(() -> {
        throw new RuntimeException("This method mustn't be called.");
      }));
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }
  }
}
