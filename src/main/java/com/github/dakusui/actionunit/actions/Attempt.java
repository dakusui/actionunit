package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.generators.ActionGenerator;
import com.github.dakusui.actionunit.helpers.Checks;

import java.util.Objects;

public interface Attempt<E extends Throwable> extends Action {
  Action attempt();

  Class<E> exceptionClass();

  Action recover(ValueHolder<E> exception);

  Action ensure();

  static <E extends Throwable> Attempt.Builder<E> builder(int id, Action attempt) {
    return new Builder<>(id, attempt);
  }

  class Builder<E extends Throwable> {
    private final Action             attempt;
    private final int                id;
    @SuppressWarnings("unchecked")
    private       Class<? extends E> exceptionClass                   = (Class<? extends E>) Exception.class;
    private       ActionGenerator<E> exceptionHandlingActionGenerator = null;
    private       ActionGenerator<?> ensuredActionGenerator           = ActionGenerator.of(v -> Context::nop);

    public Builder(int id, Action attempt) {
      this.id = id;
      this.attempt = Objects.requireNonNull(attempt);
    }

    public Builder<E> recover(Class<? extends E> exceptionClass, ActionGenerator<E> exceptionHandlingActionGenerator) {
      this.exceptionClass = Objects.requireNonNull(exceptionClass);
      this.exceptionHandlingActionGenerator = Objects.requireNonNull(exceptionHandlingActionGenerator);
      return this;
    }

    public Attempt<E> ensure(ActionGenerator<?> ensureHandlerGenerator) {
      this.ensuredActionGenerator = Objects.requireNonNull(ensureHandlerGenerator);
      return this.build();
    }

    @SuppressWarnings("unchecked")
    public Attempt<E> build() {
      Checks.checkState(exceptionClass != null, "Exception class isn't set yet.");
      return new Impl<>(id, attempt, (Class<E>) exceptionClass, exceptionHandlingActionGenerator, ensuredActionGenerator);
    }
  }

  class Impl<E extends Throwable> extends ActionBase implements Attempt<E> {
    private final Action             attempt;
    private final Class<E>           exceptionClass;
    private final ActionGenerator<E> exceptionHandlingActionGenerator;
    private final ActionGenerator<?> ensuredActionGenerator;

    protected Impl(int id, Action attempt, Class<E> exceptionClass, ActionGenerator<E> exceptionHandlingActionGenerator, ActionGenerator<?> ensuredActionGenerator) {
      super(id);
      this.attempt = Objects.requireNonNull(attempt);
      this.exceptionClass = Objects.requireNonNull(exceptionClass);
      this.exceptionHandlingActionGenerator = Objects.requireNonNull(exceptionHandlingActionGenerator);
      this.ensuredActionGenerator = Objects.requireNonNull(ensuredActionGenerator);
    }

    @Override
    public Action attempt() {
      return Context.Internal.named(
          0,
          "Target",
          this.attempt
      );
    }

    @Override
    public Class<E> exceptionClass() {
      return this.exceptionClass;
    }

    @Override
    public Action recover(ValueHolder<E> exception) {
      return Context.Internal.named(
          1,
          String.format("Recover(%s)", exceptionClass.getSimpleName()),
          exceptionHandlingActionGenerator.apply(exception, Context.create())
      );
    }

    @Override
    public Action ensure() {
      return Context.Internal.named(
          2,
          "Ensure",
          this.ensuredActionGenerator.apply(ValueHolder.empty(), Context.create())
      );
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }
  }
}
