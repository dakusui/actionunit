package com.github.dakusui.actionunit.exceptions;

/**
 * Encapsulate a general Action error or warning.
 */
public class ActionException extends RuntimeException {
  /**
   * Creates a new {@code ActionException} with a given message.
   *
   * @param message The detail message.
   */
  public ActionException(String message) {
    this(message, null);
  }

  /**
   * Creates a new {@code ActionException} from an existing exception.
   * The existing exception will be embedded in the new one,
   *
   * @param t The exception to be wrapped in a {@code ActionException}.
   */
  public ActionException(Throwable t) {
    this(null, t);
  }

  /**
   * Creates a new {@code ActionException} from an existing exception.
   * The existing exception will be embedded in the new one, but the new exception will have its own
   * message.
   *
   * @param message The detail message.
   * @param t       The exception to be wrapped in a {@code ActionException}.
   */
  public ActionException(String message, Throwable t) {
    super(message, t);
  }


  /**
   * Wraps a given exception if necessary.
   *
   * Note that this method throws an exception itself or that wraps the given throwable, not returns.
   * Therefore, this method never finishes.
   *
   * You can construct a throw statement using this method.
   * That is, you can construct a statement: `throw wrap(anException);`, which
   * doesn't confuse static code analysis.
   *
   * @param t An exception to be rethrown.
   * @return Will never be returned, but thrown.
   * @param <T> Type of exception to be thrown.
   */
  public static <T extends ActionException> T wrap(Throwable t) {
    if (t == null) {
      throw new ActionException(t);
    }
    if (t instanceof Error) {
      throw (Error) t;
    }
    if (t instanceof RuntimeException) {
      throw (RuntimeException) t;
    }
    throw new ActionException(t.getMessage(), t);
  }
}
