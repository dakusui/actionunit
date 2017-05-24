package com.github.dakusui.actionunit.exceptions;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static com.github.dakusui.actionunit.helpers.Checks.checkNotNull;

/**
 * Encapsulate a general Action error or warning.
 */
public class ActionException extends RuntimeException {
  enum Mapping implements ExceptionMapping<ActionException> {
    @SuppressWarnings("unused")IO(IOException.class),
    @SuppressWarnings("unused")CLASS_CAST(ClassCastException.class),
    @SuppressWarnings("unused")ILLEGAL_ACCESS(IllegalAccessException.class),
    @SuppressWarnings("unused")TIMEOUT(TimeoutException.class),
    @SuppressWarnings("unused")INTERRUPTED(InterruptedException.class),
    @SuppressWarnings("unused")RUNTIME(RuntimeException.class),
    @SuppressWarnings("unused")NOSUCHMETHOD(NoSuchMethodException.class)
    ;

    private final Class<? extends Throwable>       from;
    private final Class<? extends ActionException> to;

    Mapping(Class<? extends Throwable> from) {
      this(from, ActionException.class);
    }

    Mapping(Class<? extends Throwable> from, Class<? extends ActionException> to) {
      this.from = checkNotNull(from);
      this.to = checkNotNull(to);
    }

    @Override
    public Class<? extends ActionException> getApplicationExceptionClass() {
      return this.to;
    }

    @Override
    public Class<? extends Throwable> getNativeExceptionClass() {
      return this.from;
    }
  }

  public static final ExceptionMapping.Resolver<ActionException> RESOLVER = ExceptionMapping.Resolver.Factory.create(Mapping.class);

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
    if (t instanceof Error) {
      throw (Error) t;
    }
    if (t instanceof RuntimeException) {
      throw (RuntimeException) t;
    }
    throw RESOLVER.resolve(t);
  }
}
