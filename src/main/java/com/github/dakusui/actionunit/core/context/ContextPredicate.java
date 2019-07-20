package com.github.dakusui.actionunit.core.context;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.printables.PrintablePredicate;

import java.io.Serializable;
import java.util.Formatter;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.github.dakusui.actionunit.utils.InternalUtils.objectToStringIfOverridden;

@FunctionalInterface
public interface ContextPredicate extends Predicate<Context>, Serializable, Printable {
  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format(objectToStringIfOverridden(this, () -> "(noname)"));
  }

  @Override
  default ContextPredicate and(Predicate<? super Context> other) {
    Objects.requireNonNull(other);
    return (t) -> test(t) && other.test(t);
  }

  @Override
  default ContextPredicate negate() {
    return (t) -> !test(t);
  }

  @Override
  default ContextPredicate or(Predicate<? super Context> other) {
    Objects.requireNonNull(other);
    return (t) -> test(t) || other.test(t);
  }

  class Impl extends PrintablePredicate<Context> implements ContextPredicate {

    public Impl(Supplier<String> formatter, Predicate<Context> predicate) {
      super(formatter, predicate);
    }

    @Override
    public ContextPredicate and(Predicate<? super Context> other) {
      return (ContextPredicate) super.and(other);
    }

    @Override
    public ContextPredicate negate() {
      return (ContextPredicate) super.negate();
    }

    @Override
    public ContextPredicate or(Predicate<? super Context> other) {
      return (ContextPredicate) super.or(other);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ContextPredicate.Impl createPredicate(
        Supplier<String> formatter,
        Predicate<? super Context> predicate) {
      return new ContextPredicate.Impl(formatter, (Predicate<Context>) predicate);
    }
  }
}
