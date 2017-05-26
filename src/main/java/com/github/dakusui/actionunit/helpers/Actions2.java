package com.github.dakusui.actionunit.helpers;

import com.github.dakusui.actionunit.actions.Leaf;
import com.github.dakusui.actionunit.actions.Sequential;
import com.github.dakusui.actionunit.core.Action;

import java.util.concurrent.TimeUnit;

public interface Actions2 {
  /**
   * Creates a simple action object.
   *
   * @param description A string used by {@code describe()} method of a returned {@code Action} object.
   * @param runnable    An object whose {@code run()} method run by a returned {@code Action} object.
   * @see Leaf
   */
  default Action simple(final String description, final Runnable runnable) {
    return Actions.simple(description, runnable);
  }

  /**
   * Creates a named action object.
   *
   * @param name   name of the action
   * @param action action to be named.
   */
  default Action named(String name, Action action) {
    return Actions.named(name, action);
  }

  /**
   * Creates an action which runs given {@code actions} in a concurrent manner.
   *
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @see Sequential.Impl
   */
  default Action concurrent(Action... actions) {
    return Actions.concurrent(actions);
  }

  /**
   * Creates an action which runs given {@code actions} in a concurrent manner.
   *
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @see Sequential.Impl
   */
  default Action concurrent(Iterable<? extends Action> actions) {
    return Actions.concurrent(actions);
  }

  /**
   * Creates an action which runs given {@code actions} in a sequential manner.
   *
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @see Sequential.Impl
   */
  default Action sequential(Action... actions) {
    return Actions.sequential(actions);
  }

  /**
   * Creates an action which runs given {@code actions} in a sequential manner.
   *
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @see Sequential.Impl
   */
  default Action sequential(Iterable<? extends Action> actions) {
    return Actions.sequential(actions);
  }

  /**
   * Returns an action that does nothing.
   */
  default Action nop() {
    return Actions.nop();
  }

  /**
   * Returns an action that does nothing.
   *
   * @param description A string that describes returned action.
   */
  default Action nop(final String description) {
    return Actions.nop(description);
  }

  /**
   * Returns an action that waits for given amount of time.
   *
   * @param duration Duration to wait for.
   * @param timeUnit Time unit of the {@code duration}.
   */
  default Action sleep(final long duration, final TimeUnit timeUnit) {
    return Actions.sleep(duration, timeUnit);
  }
}
