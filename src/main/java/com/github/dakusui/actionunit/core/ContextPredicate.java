package com.github.dakusui.actionunit.core;

import com.github.dakusui.printables.PrintablePredicate;

import java.text.MessageFormat;
import java.util.Formattable;
import java.util.Formatter;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.github.dakusui.actionunit.utils.InternalUtils.suppressObjectToString;
import static java.util.Objects.requireNonNull;

@FunctionalInterface
public interface ContextPredicate extends Predicate<Context>, Formattable {
  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format(suppressObjectToString(this.toString()));
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

  static <T> ContextPredicate of(String variableName, Predicate<T> predicate) {
    return ContextFunctions.contextPredicateFor(variableName).with(predicate);
  }

  class Impl extends PrintablePredicate<Context> implements ContextPredicate {

    Impl(Supplier<String> s, Predicate<Context> predicate) {
      super(s, predicate);
    }

    @Override
    public ContextPredicate and(Predicate<? super Context> other) {
      requireNonNull(other);
      return (ContextPredicate) super.and(other);
    }

    @Override
    public ContextPredicate negate() {
      return (ContextPredicate) super.negate();
    }

    @Override
    public ContextPredicate or(Predicate<? super Context> other) {
      requireNonNull(other);
      return (ContextPredicate) super.or(other);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ContextPredicate.Impl createPredicate(Supplier<String> formatter, Predicate<? super Context> predicate) {
      return new ContextPredicate.Impl(formatter, (Predicate<Context>) predicate);
    }
  }

  class Builder {
    private final String                                variableName;
    private final BiFunction<Predicate, String, String> descriptionFormatter;

    Builder(String variableName) {
      this(variableName,
          (p, v) -> String.format("%s:[%s]",
              v,
              MessageFormat.format(suppressObjectToString(p.toString()), v)));
    }

    Builder(String variableName, BiFunction<Predicate, String, String> descriptionFormatter) {
      this.variableName = requireNonNull(variableName);
      this.descriptionFormatter = requireNonNull(descriptionFormatter);
    }

    public <T> ContextPredicate with(Predicate<T> predicate) {
      requireNonNull(predicate);
      return new Impl(
          () -> descriptionFormatter.apply(predicate, variableName),
          c -> predicate.test(c.valueOf(variableName)));
    }
  }
}
