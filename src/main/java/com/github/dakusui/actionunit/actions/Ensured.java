package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.exceptions.ActionException;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.core.ActionSupport.nop;
import static java.util.Objects.requireNonNull;

/**
 * An action whose success is ensured by the "ensurer" actions.
 *
 * How it works?::
 * Each action returned by {@code Ensured#ensurers()} is performed one by one.
 * After an entry from {@code ensurers()} is performed successfully, the target action
 * will be performed ({@code target()}.
 * If the target is performed successfully, this action finishes immediately and  successfully.
 * If this step is tried for all the ensurers but no attempt finishes successfully, the entire action will fail.
 *
 * If an exception thrown by a target or an ensurer is not "recoverable", the entire action will fail immediately.
 * Whether it is recoverable or not is determined by the return value of a method {@code Ensured#isRecoverable(Throwable)}.
 *
 * @see Ensured#ensurers()
 * @see Ensured#target()
 * @see Ensured#isRecoverable(Throwable)
 */
public interface Ensured extends Action {
  boolean isRecoverable(Throwable exception);

  <T> T rethrow(T exception);

  Action target();

  List<Action> ensurers();

  @Override
  default void accept(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format("ensure:%s using", this.target());
  }

  class Impl implements Ensured {
    private final List<Action>                   ensurers;
    private final Action                         target;
    private final Predicate<Throwable>           exceptionFilter;
    private final Function<Throwable, Throwable> rethrower;

    @SuppressWarnings("unchecked")
    public <T extends Throwable, R extends Throwable> Impl(Function<T, R> rethrower, Predicate<Throwable> exceptionFilter, Action target, List<Action> ensurers) {
      this.ensurers = requireNonNull(ensurers);
      this.target = requireNonNull(target);
      this.exceptionFilter = requireNonNull(exceptionFilter);
      this.rethrower = (Function<Throwable, Throwable>) rethrower;
    }

    @Override
    public boolean isRecoverable(Throwable exception) {
      return exceptionFilter.test(exception);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T rethrow(T exception) {
      return (T) rethrower.apply((Throwable) exception);
    }

    @Override
    public Action target() {
      return this.target;
    }

    @Override
    public List<Action> ensurers() {
      return this.ensurers;
    }
  }

  class Builder extends Action.Builder<Ensured> {
    private       Action               target;
    private final List<Action>         ensurers        = new LinkedList<>();
    private       Predicate<Throwable> exceptionFilter = Exception.class::isInstance;


    public Builder target(Action target) {
      this.target = requireNonNull(target);
      return this;
    }

    public Builder with(Action ensurer) {
      ensurers.add(requireNonNull(ensurer));
      return this;
    }

    public Builder withNop() {
      return this.with(nop());
    }

    public Builder recoverExceptions(Predicate<Throwable> exceptionFilter) {
      this.exceptionFilter = requireNonNull(exceptionFilter);
      return this;
    }

    public Builder recover(Class<? extends Throwable> exceptionType) {
      return this.recoverExceptions(requireNonNull(exceptionType)::isInstance);
    }

    public Ensured build() {
      return new Impl(ActionException::wrap, exceptionFilter, this.target, this.ensurers);
    }
  }
}
