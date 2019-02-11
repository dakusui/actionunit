package com.github.dakusui.actionunit.core;

import com.github.dakusui.actionunit.core.ContextFunctions.Params;
import com.github.dakusui.printables.PrintablePredicate;

import java.text.MessageFormat;
import java.util.Formattable;
import java.util.Formatter;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.github.dakusui.actionunit.utils.InternalUtils.objectToStringIfOverridden;
import static com.github.dakusui.printables.Printables.predicate;
import static java.util.Objects.requireNonNull;

@FunctionalInterface
public interface ContextPredicate extends Predicate<Context>, Formattable {
  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format(objectToStringIfOverridden(this, "(noname)"));
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
    return ContextFunctions.contextPredicateFor(variableName)
        .with(
            predicate((Params params) -> predicate.test(params.valueOf(variableName)))
                .describe(predicate.toString())
        );
  }

  class Impl extends PrintablePredicate<Context> implements ContextPredicate {

    Impl(Supplier<String> formatter, Predicate<Context> predicate) {
      super(formatter, predicate);
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
    private final String[]                                variableNames;
    private final BiFunction<Predicate, String[], String> descriptionFormatter;

    public Builder(String... variableNames) {
      this(Builder::defaultDescriptionFormatter, variableNames);
    }

    Builder(BiFunction<Predicate, String[], String> descriptionFormatter, String... variableNames) {
      this.variableNames = requireNonNull(variableNames);
      this.descriptionFormatter = requireNonNull(descriptionFormatter);
    }

    public ContextPredicate with(Predicate<Params> predicate) {
      requireNonNull(predicate);
      return new Impl(
          () -> descriptionFormatter.apply(predicate, variableNames),
          (Context c) -> predicate.test(Params.create(c, variableNames)));
    }

    private static String defaultDescriptionFormatter(Predicate p, String[] v) {
      return String.format("(%s)->%s",
          String.join(",", v),
          MessageFormat.format(objectToStringIfOverridden(p, "(noname)"), (Object[]) v));
    }
  }
}
