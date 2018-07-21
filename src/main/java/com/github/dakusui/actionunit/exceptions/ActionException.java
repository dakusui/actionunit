package com.github.dakusui.actionunit.exceptions;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static com.github.dakusui.actionunit.utils.Checks.checkNotNull;

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


  public static <T extends ActionException> T wrap(Throwable t) {
    if (t == null) {
      throw new ActionException(t);
    }
    if (t.getCause() == null) {
      if (t instanceof Error) {
        throw (Error) t;
      }
      if (t instanceof RuntimeException) {
        throw (RuntimeException) t;
      }
      if (t instanceof TimeoutException)
        throw new ActionTimeOutException(t.getMessage(), t);
      throw new ActionException(t.getMessage(), t);
    } else {
      throw wrap(t.getCause());
    }
  }
}
