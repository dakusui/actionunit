package com.github.dakusui.actionunit.core;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.extras.cmd.Commander;
import com.github.dakusui.actionunit.helpers.InternalUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.dakusui.actionunit.helpers.Checks.checkArgument;
import static com.github.dakusui.actionunit.helpers.Checks.checkNotNull;
import static com.github.dakusui.actionunit.helpers.InternalUtils.nonameIfNull;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * An interface that defines methods to create various actions and builders of
 * actions.
 */
public interface Context {
  IdGeneratorManager ID_GENERATOR_MANAGER = new IdGeneratorManager();

  default int generateId() {
    return ID_GENERATOR_MANAGER.generateId(this);
  }

  /**
   * Creates a simple action object.
   *
   * @param description A string used by {@code describable()} method of a returned {@code Action} object.
   * @param runnable    An object whose {@code run()} method run by a returned {@code Action} object.
   * @return Created action
   * @see Leaf
   */
  default Action simple(final String description, final Runnable runnable) {
    return Internal.simple(generateId(), description, runnable);
  }

  /**
   * Creates a named action object.
   *
   * @param name   name of the action
   * @param action action to be named.
   * @return Created action
   */
  default Action named(String name, Action action) {
    return Internal.named(generateId(), name, action);
  }

  /**
   * Creates an action which runs given {@code actions} in a concurrent manner.
   *
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @return Created action
   * @see Concurrent
   */
  default Action concurrent(Action... actions) {
    return Internal.concurrent(generateId(), actions);
  }

  /**
   * Creates an action which runs given {@code actions} in a concurrent manner.
   *
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @return Created action
   * @see Concurrent
   */
  default Action concurrent(Iterable<? extends Action> actions) {
    return Internal.concurrent(generateId(), actions);
  }

  /**
   * Creates an action which runs given {@code actions} in a sequential manner.
   *
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @return Created action
   * @see Sequential
   */
  default Action sequential(Action... actions) {
    return Internal.sequential(generateId(), actions);
  }

  /**
   * Creates an action which runs given {@code actions} in a sequential manner.
   *
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @return Created action
   * @see Sequential
   */
  default Action sequential(Iterable<? extends Action> actions) {
    return Internal.sequential(generateId(), actions);
  }

  /**
   * Returns an action that does nothing.
   *
   * @return Created action
   */
  default Action nop() {
    return Internal.nop(generateId());
  }

  /**
   * Returns an action that does nothing.
   *
   * @param description A string that describes returned action.
   * @return Created action
   */
  default Action nop(final String description) {
    return Internal.nop(generateId(), description);
  }

  /**
   * Returns an action that waits for given amount of time.
   *
   * @param duration Duration to wait for.
   * @param timeUnit Time unit of the {@code duration}.
   * @return Created action
   */
  default Action sleep(final long duration, final TimeUnit timeUnit) {
    return Internal.sleep(generateId(), duration, timeUnit);
  }

  /**
   * Creates a builder for {@code ForEach} action.
   *
   * @param stream Supplier of stream operated by a {@code ForEach} object that returned
   *               builder will build.
   * @param <E>    Type of elements
   * @return Created builder
   * @see ForEach
   * @see ForEach.Builder
   */
  default <E> ForEach.Builder<E> forEachOf(Supplier<Stream<? extends E>> stream) {
    return Internal.forEachOf(generateId(), stream);
  }

  /**
   * Creates a builder for {@code ForEach} action.
   *
   * @param elements Elements iterated by a {@code ForEach} object that returned
   *                 builder will build.
   * @param <E>      Type of elements
   * @return Created builder
   * @see ForEach
   * @see ForEach.Builder
   */
  default <E> ForEach.Builder<E> forEachOf(Iterable<? extends E> elements) {
    return Internal.forEachOf(generateId(), elements);
  }


  /**
   * Creates a builder for {@code ForEach} action.
   *
   * @param elements Elements iterated by a {@code ForEach} object that returned
   *                 builder will build.
   * @param <E>      Type of elements
   * @return Created builder
   * @see ForEach
   * @see ForEach.Builder
   */
  @SuppressWarnings("unchecked")
  default <E> ForEach.Builder<E> forEachOf(E... elements) {
    return Internal.forEachOf(generateId(), elements);
  }

  /**
   * Creates a builder for {@code While} action. This method was named {@code whilst}
   * just because {@code while} is a reserved word in Java and it can't be used
   * for a method name.
   *
   * @param value     A supplier that gives value to be examined by {@code condition}.
   * @param condition A predicate that determines if an action created by {@code While}
   *                  object should be executed or not.
   * @param <T>       Type of value supplied by {@code value} and examined by {@code condition}
   * @return Created builder
   * @see While
   * @see While.Builder
   */
  default <T> While.Builder<T> whilst(Supplier<T> value, Predicate<T> condition) {
    return Internal.whilst(generateId(), value, condition);
  }

  /**
   * Creates a builder for {@code While} action. This method was named {@code whilst}
   * just because {@code while} is a reserved word in Java and it can't be used
   * for a method name.
   *
   * @param condition A supplier that determines if an action created by {@code While}
   *                  object should be executed or not.
   * @return Created builder
   * @see While
   * @see While.Builder
   */
  default While.Builder<Boolean> whilst(Supplier<Boolean> condition) {
    return Internal.whilst(generateId(), condition, result -> result);
  }

  /**
   * Creates a builder for {@code When} action.
   *
   * @param value     A supplier that gives value to be examined by {@code condition}.
   * @param condition A predicate that determines if an action created by {@code While}
   *                  object should be executed or not.
   * @param <T>       Type of value supplied by {@code value} and examined by {@code condition}
   * @return Created builder
   * @see When
   * @see When.Builder
   */
  default <T> When.Builder<T> when(Supplier<T> value, Predicate<T> condition) {
    return Internal.when(generateId(), value, condition);
  }

  /**
   * Creates a builder for {@code When} action.
   *
   * @param condition A supplier that determines if an action created by {@code While}
   *                  object should be executed or not.
   * @return Created builder
   * @see When
   * @see When.Builder
   */
  default When.Builder<Boolean> when(Supplier<Boolean> condition) {
    return Internal.when(generateId(), condition, result -> result);
  }

  /**
   * Creates a builder for {@code TimeOut} action.
   *
   * @param action An action executed by {@code TimeOut} action that returned builder
   *               builds
   * @return Created builder
   * @see TimeOut
   * @see TimeOut.Builder
   */
  default TimeOut.Builder timeout(Action action) {
    return Internal.timeout(generateId(), action);
  }

  /**
   * Creates a builder for {@code Attempt} action.
   *
   * @param action An action executed by {@code Attempt} action that returned builder
   *               builds
   * @param <T>    Type of a {@code Throwable} that action may throw.
   * @return Created builder
   * @see Attempt
   * @see Attempt.Builder
   */
  default <T extends Throwable> Attempt.Builder<T> attempt(Action action) {
    return Internal.attempt(generateId(), action);
  }

  /**
   * Creates a builder for {@code Retry} action.
   *
   * @param action An action executed by {@code Retry} action that returned builder
   *               builds
   * @return Created builder
   * @see Retry
   * @see Retry.Builder
   */
  default Retry.Builder retry(Action action) {
    return Internal.retry(generateId(), action);
  }

  /**
   * Creates a builder for {@code TestAction} action. A function to be tested
   * will be passed to a {@code TestAction#when} method.
   *
   * @param description A string that describes a {@code given} supplier.
   * @param given       A supplier that gives a value to be tested by {@code Test} action.
   * @param <I>         Type of input to a function to be tested.
   * @param <O>         Type of output from a function to be tested.
   * @return Created builder
   * @see TestAction
   * @see TestAction.Builder
   */
  default <I, O> TestAction.Builder<I, O> given(String description, Supplier<I> given) {
    return Internal.given(generateId(), description, given);
  }

  default Commander cmd(String program) {
    return Commander.commander(this, program);
  }

  class Impl implements Context {
    ConcurrentMap<String, Object> map = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <V> V set(String variableName, V value) {
      return (V) map.put(variableName, requireNonNull(value));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> V get(String variableName) {
      if (!map.containsKey(variableName))
        throw new ActionException(format("Undefined variable '%s' was referenced.", variableName));
      return (V) map.get(variableName);
    }
  }

  class Internal {
    public static Action simple(int id, final String description, final Runnable runnable) {
      return Leaf.create(id, description, runnable);
    }

    public static Action named(int id, String name, Action action) {
      return Named.create(id, name, action);
    }

    public static Action concurrent(int id, Action... actions) {
      return concurrent(id, asList(actions));
    }

    public static Action concurrent(int id, Iterable<? extends Action> actions) {
      return Concurrent.Factory.INSTANCE.create(id, actions);
    }

    public static Action sequential(int id, Action... actions) {
      return sequential(id, asList(actions));
    }

    public static Action sequential(int id, Iterable<? extends Action> actions) {
      return Sequential.Factory.INSTANCE.create(id, actions);
    }

    public static Action nop(int id) {
      return nop(id, "(nop)");
    }

    public static Action nop(int id, final String description) {
      return new Leaf(id) {
        @Override
        public void perform() {
        }

        @Override
        public String toString() {
          return nonameIfNull(description);
        }
      };
    }

    public static Action sleep(int id, final long duration, final TimeUnit timeUnit) {
      checkArgument(duration >= 0, "duration must be non-negative but %s was given", duration);
      checkNotNull(timeUnit);
      return new Leaf(id) {
        @Override
        public void perform() {
          InternalUtils.sleep(duration, timeUnit);
        }

        @Override
        public String toString() {
          return format("sleep for %s", InternalUtils.formatDuration(NANOSECONDS.convert(duration, timeUnit)));
        }
      };
    }

    public static <E> ForEach.Builder<E> forEachOf(int id, Supplier<Stream<? extends E>> streamSupplier) {
      return ForEach.builder(id, streamSupplier);
    }

    public static <E> ForEach.Builder<E> forEachOf(int id, Iterable<? extends E> elements) {
      return forEachOf(id, () -> StreamSupport.stream(elements.spliterator(), false));
    }

    @SafeVarargs
    public static <E> ForEach.Builder<E> forEachOf(int id, E... elements) {
      return forEachOf(id, asList(elements));
    }

    public static <T> While.Builder<T> whilst(int id, Supplier<T> value, Predicate<T> condition) {
      return new While.Builder<>(id, value, condition);
    }

    public static <T> When.Builder<T> when(int id, Supplier<T> value, Predicate<T> condition) {
      return new When.Builder<>(id, value, condition);
    }

    public static TimeOut.Builder timeout(int id, Action action) {
      return new TimeOut.Builder(id, action);
    }

    public static <T extends Throwable> Attempt.Builder<T> attempt(int id, Action action) {
      return Attempt.builder(id, action);
    }

    public static Retry.Builder retry(int id, Action action) {
      return Retry.builder(id, action);
    }

    public static <I, O> TestAction.Builder<I, O> given(int id, String description, Supplier<I> given) {
      return new TestAction.Builder<I, O>(id).given(description, given);
    }
  }

  /**
   * Associates a {@code value} with a variable specified by {@code variableName}.
   * <p>
   * An implementation of this method internally casts a value associated into
   * a type {@code V}, which is figured out by compiler automatically. This means
   * a user of this method needs to be responsible for using correct type and
   * unless otherwise a class cast exception will be thrown at runtime.
   *
   * @param variableName A name of a variable a {@code value} is going to be associated with.
   * @param value        A value of a variable specified by {@code variableName}.
   * @param <V>          Type of value associated with a variable specified by {@code variableName}.
   * @return A value previously associated with this context.
   */
  default <V> V set(String variableName, V value) {
    throw new UnsupportedOperationException("'Variable' feature is not supported by this implementation.");
  }

  /**
   * Returns a value associated with a variable specified by {@code variableName}.
   * <p>
   * An implementation of this method internally casts a value associated into
   * a type {@code V}, which is figured out by compiler automatically. This means
   * a user of this method needs to be responsible for using correct type and
   * unless otherwise a class cast exception will be thrown at runtime.
   *
   * @param variableName A name of variable whose value should be returned.
   * @param <V>          A type of variable.
   * @return A value associated with this context
   */
  default <V> V get(String variableName) {
    throw new UnsupportedOperationException("'Variable' feature is not supported by this implementation.");
  }
}
