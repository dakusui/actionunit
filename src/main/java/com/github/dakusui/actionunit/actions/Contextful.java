package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.core.Context;

import java.util.Formatter;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.dakusui.actionunit.utils.InternalUtils.toStringIfOverriddenOrNoname;
import static com.github.dakusui.pcond.Requires.requireNonNull;

public interface Contextful<T> extends Leaf {
  Function<Context, T> action();

  default <R> Contextful<R> thenApply(Function<T, R> function) {
    return new Impl<>(context -> function.apply(context.valueOf(internalVariableName())));
  }


  default <R> Action thenConsumeWith(Consumer<R> consumer) {
    return ActionSupport.simple(
        toStringIfOverriddenOrNoname(consumer),
        c -> consumer.accept(c.valueOf(internalVariableName())));
  }

  <V> V value(Context context);

  String internalVariableName();

  /**
   * A name of a variable this action assigns its result to.
   * This method is intended to return a string to display to humans.
   *
   * @return A human-readable name of the variable.
   */
  String variableName();

  class Impl<T> implements Contextful<T> {

    private final Function<Context, T> function;
    private final String               baseName;

    private final boolean anonymous;
    private final boolean threadLocal;

    public Impl(Function<Context, T> function) {
      this(null, function, true);
    }

    public Impl(Function<Context, T> function, boolean threadLocal) {
      this(null, function, threadLocal);
    }

    public Impl(String variableName, Function<Context, T> function, boolean threadLocal) {
      this.function = requireNonNull(function);
      this.threadLocal = threadLocal;
      if (variableName != null) {
        this.baseName = variableName;
        this.anonymous = false;
      } else {
        this.baseName = String.format("anonymous:%s", System.identityHashCode(this));
        this.anonymous = true;
      }
    }

    @Override
    public Function<Context, T> action() {
      return this.function;
    }

    public String variableName() {
      return this.baseName;
    }


    @Override
    public <V> V value(Context context) {
      return requireNonNull(context).valueOf(internalVariableName());
    }

    @Override
    public String internalVariableName() {
      return this.threadLocal ?
          this.baseName + ":threadId-" + Thread.currentThread().getId() :
          this.baseName;
    }

    @Override
    public Runnable runnable(Context context) {
      return () -> context.assignTo(internalVariableName(), function.apply(context));
    }

    @Override
    public void formatTo(Formatter formatter, int i, int i1, int i2) {
      formatter.format("%s:%s", anonymous ? "(noname)" : (variableName() + (threadLocal ? "" : "(global)")), toStringIfOverriddenOrNoname(function));
    }
  }
}
