package com.github.dakusui.actionunit.compat;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.actionunit.actions.ActionBase;
import com.github.dakusui.actionunit.actions.Named;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.exceptions.ActionException;

import static com.github.dakusui.actionunit.Actions.*;
import static com.github.dakusui.actionunit.Autocloseables.transform;
import static com.github.dakusui.actionunit.Checks.checkNotNull;
import static com.github.dakusui.actionunit.Utils.range;

/**
 * An action that corresponds to Java's try/catch mechanism.
 * In order to build an instance of this class, use {@link Builder} class.
 *
 * @param <T> Type of exception which is caught and handled by {@code recover} action.
 */
public class CompatAttempt<T extends Throwable> extends ActionBase {
  public final Action    attempt;
  public final Class<T>  exceptionClass;
  public final Action    recover;
  public final Sink<T>[] sinks;
  public final Action    ensure;

  /**
   * Creates an object of this class.
   *
   * @param attempt        Action initially attempted by this action.
   * @param exceptionClass Exception on which {@code recover} action is performed
   *                       if it is thrown during {@code attempt} action's execution.
   * @param recover        Action performed when an exception of {exceptionClass}
   *                       is thrown.
   * @param sinks          sink operations applied as a part of {@code recover}
   *                       action.
   * @param ensure         Action which will be performed regardless of {@code attempt}
   *                       action's behavior.
   */
  protected CompatAttempt(Action attempt, Class<? extends Throwable> exceptionClass, Action recover, Sink<? extends Throwable>[] sinks, Action ensure) {
    this.attempt = attempt;
    //noinspection unchecked
    this.exceptionClass = (Class<T>) checkNotNull(exceptionClass);
    this.recover = Named.Factory.create("Recover", recover);
    //noinspection unchecked
    this.sinks = (Sink<T>[]) sinks;
    this.ensure = Named.Factory.create("Ensure", ensure);
  }

  /**
   * {@inheritDoc}
   *
   * @param visitor the visitor operating on this element.
   */
  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  /**
   * A builder to construct an instance of {@link CompatAttempt} action.
   */
  public static class Builder {
    private final Action attempt;
    private Action                      recover        = nop();
    @SuppressWarnings("unchecked")
    private Sink<? extends Throwable>[] recoverWith    = new Sink[0];
    private Action                      ensure         = nop();
    private Class<? extends Throwable>  exceptionClass = ActionException.class;

    public Builder(Action attempt) {
      this.attempt = checkNotNull(attempt);
    }

    @SafeVarargs
    public final <T extends Throwable> Builder recover(Class<T> exceptionClass, Action action, Sink<T>... sinks) {
      this.exceptionClass = checkNotNull(exceptionClass);
      this.recover = checkNotNull(action);
      this.recoverWith = sinks;
      return this;
    }

    @SafeVarargs
    public final <T extends Throwable> Builder recover(Class<T> exceptionClass, Sink<T>... sinks) {
      return this.recover(
          exceptionClass,
          Actions.sequential(transform(range(0, sinks.length), Actions::tag)),
          sinks
      );
    }

    @SafeVarargs
    public final Builder recover(Action action, Sink<ActionException>... sinks) {
      return recover(ActionException.class, action, sinks);
    }

    @SafeVarargs
    public final Builder recover(Sink<ActionException>... sinks) {
      return recover(ActionException.class, sinks);
    }

    public Builder ensure(Action action) {
      this.ensure = checkNotNull(action);
      return this;
    }

    public Builder ensure(Runnable runnable) {
      return this.ensure(simple(runnable));
    }

    public <T extends Throwable> CompatAttempt<T> build() {
      return new CompatAttempt<>(this.attempt, this.exceptionClass, this.recover, this.recoverWith, this.ensure);
    }
  }
}
