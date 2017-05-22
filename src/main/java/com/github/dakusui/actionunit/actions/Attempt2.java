package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.actionunit.Checks;

import java.util.Objects;
import java.util.function.Supplier;

public interface Attempt2<E extends Throwable> extends Action {
  Action attempt();

  Class<E> exceptionClass();

  Action recover(Supplier<E> exception);

  Action ensure();

  class Builder<E extends Throwable> {
    private final Action attempt;
    private Action              ensure           = Actions.nop();
    private Class<E>            exceptionClass   = null;
    private ProcessorFactory<E> processorFactory = e -> {
      throw Checks.propagate(e.get());
    };

    public Builder(Action attempt) {
      this.attempt = Actions.named("Attempt:", Objects.requireNonNull(attempt));
    }

    public Builder<E> recover(Class<E> exceptionClass, ProcessorFactory<E> processorFactory) {
      this.exceptionClass = Objects.requireNonNull(exceptionClass);
      this.processorFactory = Objects.requireNonNull(processorFactory);
      return this;
    }

    public Attempt2<E> ensure(Action action) {
      this.ensure = Actions.named("Ensure:", Objects.requireNonNull(action));
      return this.build();
    }

    public Attempt2<E> build() {
      Checks.checkState(exceptionClass != null, "Exception class isn't set yet.");
      return new Impl<>(attempt, exceptionClass, processorFactory, ensure);
    }
  }

  class Impl<E extends Throwable> extends ActionBase implements Attempt2<E> {
    private final Action              attempt;
    private final Action              ensure;
    private final Class<E>            exceptionClass;
    private final ProcessorFactory<E> processorFactory;

    public Impl(Action attempt, Class<E> exceptionClass, ProcessorFactory<E> processorFactory, Action ensure) {
      this.attempt = Objects.requireNonNull(attempt);
      this.exceptionClass = Objects.requireNonNull(exceptionClass);
      this.processorFactory = Objects.requireNonNull(processorFactory);
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
      return Actions.named(String.format("Recover(%s):", exceptionClass.getSimpleName()), processorFactory.apply(exception));
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
