package com.github.dakusui.actionunit.utils;

import com.github.dakusui.actionunit.actions.Retry;

/**
 * An exception on which retries will not be attempted by {@link com.github.dakusui.actionunit.visitors.ActionRunner}
 * even if it is executing {@link Retry}.
 * If you give this class to {@link Retry} class's constructor as its first argument,
 * an {@link IllegalArgumentException} will be thrown.
 *
 * @see Retry
 */
public class Abort extends RuntimeException {
  /**
   * Creates an instance of this class.
   *
   * @param message A message that describes this object.
   * @param t       A cause of the failure that this object represents.
   */
  public Abort(String message, Throwable t) {
    super(message, t);
  }

  /**
   * Creates and throws an {@code Abort} object.
   */
  public static Abort abort() {
    throw abort((String) null);
  }

  /**
   * Creates and throws an {@code Abort} object with given {@code message}.
   *
   * @param message A message for the exception to be created and thrown.
   */
  public static Abort abort(String message) {
    throw abort(message, null);
  }

  /**
   * Creates and throws an {@code Abort} object with given {@code message} and {@code cause}.
   *
   * @param cause A cause for the exception to be created and thrown.
   */
  public static Abort abort(Throwable cause) {
    throw abort(null, cause);
  }

  /**
   * Creates and throws an {@code Abort} object with given {@code message} and {@code cause}.
   *
   * @param message A message for the exception to be created and thrown.
   * @param cause   A cause for the exception to be created and thrown.
   */
  public static Abort abort(String message, Throwable cause) {
    throw new Abort(message, cause);
  }
}
