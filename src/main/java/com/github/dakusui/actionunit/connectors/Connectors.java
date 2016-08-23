package com.github.dakusui.actionunit.connectors;

import com.github.dakusui.actionunit.Context;
import com.github.dakusui.actionunit.Utils;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.hamcrest.Matcher;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public enum Connectors {
  ;

  public static <I, O> Pipe<I, O> toPipe(final Function<I, O> func) {
    checkNotNull(func);
    return new Pipe.Base<I, O>() {
      @Override
      protected O apply(I input, Object... outer) {
        return func.apply(input);
      }

      @Override
      public String toString() {
        return String.format("Function(%s)", Utils.describe(func));
      }
    };
  }

  public static <V> Source<V> immutable(final V value) {
    return new Source.Immutable<V>(value) {
      @Override
      public String toString() {
        return Utils.describe(value);
      }
    };
  }

  public static <T> Source<T> toSource(T value) {
    return immutable(value);
  }

  public static <V> Source.Mutable<V> mutable() {
    return new Source.Mutable<>();
  }

  public static <V> Source<V> context() {
    return new Source<V>() {
      @Override
      public V apply(Context context) {
        return context.value();
      }

      public String toString() {
        return "context";
      }
    };
  }

  /**
   * Creates a dumb sink of type {@code V}.
   *
   * @param <V> type of input.
   */
  public static <V> Sink<V> dumb() {
    return new Sink<V>() {
      @Override
      public void apply(V input, Context context) {
      }

      public String toString() {
        return "Sink(dumb)";
      }
    };
  }

  public static <O> Sink<O> toSink(final Matcher<O> matcher) {
    checkNotNull(matcher);
    return new Sink.Base<O>() {
      @Override
      protected void apply(O input, Object... outer) {
        assertThat(input, matcher);
      }

      @Override
      public String toString() {
        return String.format("Matcher(%s)", Utils.describe(matcher));
      }
    };
  }

  public static <O> Sink<O> toSink(final Predicate<O> predicate) {
    checkNotNull(predicate);
    return new Sink.Base<O>() {
      @Override
      protected void apply(O input, Object... outer) {
        assertTrue(predicate.apply(input));
      }

      @Override
      public String toString() {
        return String.format("Matcher(%s)", Utils.describe(predicate));
      }
    };
  }

}
