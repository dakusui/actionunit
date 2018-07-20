package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.*;
import com.github.dakusui.actionunit.helpers.Checks;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Attempt<E extends Throwable> extends Action, Context {
  Action attempt();

  Class<E> exceptionClass();

  Action recover(ValueHolder<E> exception);

  Action ensure();

  static <E extends Throwable> Attempt.Builder<E> builder(int id, Action attempt) {
    return new Builder<>(id, attempt);
  }

  class Builder<E extends Throwable> {
    private final Action                       attempt;
    private final int                          id;
    @SuppressWarnings("unchecked")
    private       Class<? extends E>           exceptionClass          = (Class<? extends E>) Exception.class;
    private       ValueHandlerActionFactory<E> exceptionHandlerFactory = ($, data) -> {
      throw Checks.propagate(data.get());
    };
    private       ActionFactory                ensuredActionFactory    = Context::nop;

    public Builder(int id, Action attempt) {
      this.id = id;
      this.attempt = Objects.requireNonNull(attempt);
    }

    public Builder<E> recover(Class<? extends E> exceptionClass, Function<Supplier<E>, ActionGenerator> exceptionHandlerFactory) {
      return this.recover(exceptionClass, (c, f) -> exceptionHandlerFactory.apply(f).apply(c));
    }

    public Builder<E> recover(Class<? extends E> exceptionClass, ValueHandlerActionFactory<E> exceptionHandlerFactory) {
      this.exceptionClass = Objects.requireNonNull(exceptionClass);
      this.exceptionHandlerFactory = Objects.requireNonNull(exceptionHandlerFactory);
      return this;
    }

    public Attempt<E> ensure(ActionGenerator ensureHandlerFactory) {
      return this.ensure_(ensureHandlerFactory::apply);
    }

    public Attempt<E> ensure_(ActionFactory ensureHandlerFactory) {
      this.ensuredActionFactory = Objects.requireNonNull(ensureHandlerFactory);
      return this.build();
    }

    @SuppressWarnings("unchecked")
    public Attempt<E> build() {
      Checks.checkState(exceptionClass != null, "Exception class isn't set yet.");
      return new Impl<>(id, attempt, (Class<E>) exceptionClass, exceptionHandlerFactory, ensuredActionFactory);
    }
  }

  class Impl<E extends Throwable> extends ActionBase implements Attempt<E> {
    private final Action                       attempt;
    private final Class<E>                     exceptionClass;
    private final ValueHandlerActionFactory<E> exceptionHandlerFactory;
    private final ActionFactory                ensuredActionFactory;

    public Impl(int id, Action attempt, Class<E> exceptionClass, ValueHandlerActionFactory<E> exceptionHandlerFactory, ActionFactory ensuredActionFactory) {
      super(id);
      this.attempt = Objects.requireNonNull(attempt);
      this.exceptionClass = Objects.requireNonNull(exceptionClass);
      this.exceptionHandlerFactory = Objects.requireNonNull(exceptionHandlerFactory);
      this.ensuredActionFactory = Objects.requireNonNull(ensuredActionFactory);
    }

    @Override
    public Action attempt() {
      return Context.Internal.named(0, "Target", this.attempt);
    }

    @Override
    public Class<E> exceptionClass() {
      return this.exceptionClass;
    }

    @Override
    public Action recover(ValueHolder<E> exception) {
      return Context.Internal.named(1, String.format("Recover(%s)", exceptionClass.getSimpleName()), exceptionHandlerFactory.apply(exception));
    }

    @Override
    public Action ensure() {
      return Context.Internal.named(2, "Ensure", this.ensuredActionFactory.get());
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }
  }
}
