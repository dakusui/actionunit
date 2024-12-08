package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.exceptions.ActionException;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.actionunit.core.ActionSupport.nop;
import static com.github.dakusui.actionunit.exceptions.ActionException.wrap;
import static java.util.Objects.requireNonNull;

/**
 * An action whose success of the "target" is ensured by the "ensurer" actions.
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
  /**
   * An exception which tells the framework that it should not be recovered by the
   * {@code Ensured} action mechanism.
   */
  class Abort extends ActionException {
    public Abort(String message) {
      super(message);
    }
  }

  /**
   * An exception which tells the framework that a recovery requested by the
   * {@code Ensured} action mechanism.
   */
  class RequestRetry extends ActionException {
    public RequestRetry(String message) {
      super(message);
    }
  }

  /**
   * Checks if a given `exception` is recoverable by this object.
   *
   * @param exception An exception to be checked.
   * @return `true` - Can be recovered / `false` - Otherwise.
   */
  default boolean isRecoverable(Throwable exception) {
    if (exception instanceof RequestRetry)
      return true;
    if (exception instanceof Abort)
      return false;
    if ("IncompleteExecutionException".equals(exception.getClass().getSimpleName()))
      return false;
    if ("AssertionFailedError".equals(exception.getClass().getSimpleName()))
      return true;
    return !(exception instanceof Error);
  }

  /**
   * Rethrows a given `exception`, when it is not recoverable.
   *
   * @param exception An exception to be rethrown.
   * @param <T>       Type of the `exception`.
   * @return A type placeholder, never be returned.
   */
  default <T extends Throwable> T rethrow(T exception) {
    if (exception instanceof Error)
      throw (Error) exception;
    if (exception instanceof RuntimeException)
      throw (RuntimeException) exception;
    throw new ActionException(exception.getMessage(),exception);
  }

  /**
   * Returns a target action to be ensured its success.
   * This action will be performed repeatedly until it succeeds.
   *
   * @return A target action.
   * @see Ensured#ensurers()
   */
  Action target();

  /**
   * Returns a list of "ensurer" actions.
   *
   * An ensurer is an action that may make preconditions of the "target action".
   *
   * @return A list of "ensurer" actions.
   */
  List<Action> ensurers();

  @Override
  default void accept(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format("ensure:%s using", this.target());
  }

  /**
   * An implementation of `Ensured` action's interface.
   */
  class Impl implements Ensured {
    private final List<Action> ensurers;
    private final Action       target;

    @SuppressWarnings("unchecked")
    public <T extends Throwable, R extends Throwable> Impl(Action target, List<Action> ensurers) {
      this.target = requireNonNull(target);
      this.ensurers = requireNonNull(ensurers);
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

  /**
   * A builder class of `Ensured` action.
   *
   * @see Ensured
   */
  class Builder extends Action.Builder<Ensured> {
    private       Action       target;
    private final List<Action> ensurers = new LinkedList<>();

    /**
     * Sets `target` action to this builder.
     *
     * @param target A target action.
     * @return This object.
     */
    public Builder target(Action target) {
      this.target = requireNonNull(target);
      return this;
    }

    /**
     * Adds an `ensurer` action to this builder object.
     *
     * @param ensurer An ensurer action to be added.
     * @return This object.
     */
    public Builder with(Action ensurer) {
      ensurers.add(requireNonNull(ensurer));
      return this;
    }

    /**
     * Adds a "nop" (no operation) action to this builder object as an ensurer.
     * This method is useful, when the target action may succeed without any preparation.
     *
     * @return This object.
     */
    public Builder withNop() {
      return this.with(nop());
    }

    /**
     * Builds and returns an `Ensured` action object.
     *
     * @return An `Ensured` action.
     */
    public Ensured build() {
      return new Impl(this.target, this.ensurers);
    }
  }
}
