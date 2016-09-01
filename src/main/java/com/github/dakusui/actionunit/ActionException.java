package com.github.dakusui.actionunit;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

/**
 * Encapsulate a general Action error or warning.
 */
public class ActionException extends RuntimeException {
  private static final List<Entry<Class<? extends Throwable>, Class<? extends ActionException>>> EXCEPTION_MAP = asList(
      createMapping(IOException.class, ActionException.class),
      createMapping(ClassCastException.class, ActionException.class)
  );


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
    //noinspection ThrowableResultOfMethodCallIgnored
    checkNotNull(t);
    if (t instanceof Error) {
      throw (Error) t;
    }
    if (t instanceof RuntimeException) {
      throw (RuntimeException) t;
    }
    ActionException applicationException = instantiate(figureOutExceptionClassToBeThrown(t), t);
    if (applicationException != null) {
      throw applicationException;
    }
    ////
    // For unknown type of checked exception. Once this line is executed, consider
    // adding new mapping to the list.
    throw new RuntimeException(t);
  }

  private static Class<? extends ActionException> figureOutExceptionClassToBeThrown(final Throwable t) {
    //noinspection ThrowableResultOfMethodCallIgnored
    checkNotNull(t);
    Iterator<Entry<Class<? extends Throwable>, Class<? extends ActionException>>> found = Iterables.filter(EXCEPTION_MAP, new Predicate<Entry<Class<? extends Throwable>, Class<? extends ActionException>>>() {
      @Override
      public boolean apply(Entry<Class<? extends Throwable>, Class<? extends ActionException>> input) {
        return input.getKey().isAssignableFrom(t.getClass());
      }
    }).iterator();
    if (found.hasNext()) {
      return found.next().getValue();
    }
    throw new RuntimeException(t);
  }

  private static <T extends ActionException> T instantiate(Class<T> exceptionClass, Throwable nested) {
    try {
      return exceptionClass.getConstructor(String.class, Throwable.class).newInstance(nested.getMessage(), nested);
    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
      throw ActionException.wrap(e);
    }
  }

  private static Entry<Class<? extends Throwable>, Class<? extends ActionException>> createMapping(
      Class<? extends Throwable> from,
      Class<? extends ActionException> to) {
    return new SimpleEntry<Class<? extends Throwable>, Class<? extends ActionException>>(checkNotNull(from), checkNotNull(to));
  }
}
