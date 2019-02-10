package com.github.dakusui.printables;

import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class PrintablePredicate<T> implements Predicate<T> {
  private final Predicate<? super T> predicate;
  private final Supplier<String>     formatter;

  public PrintablePredicate(Supplier<String> formatter, Predicate<? super T> predicate) {
    this.predicate = requireNonNull(predicate);
    this.formatter = requireNonNull(formatter);
  }

  @Override
  public boolean test(T t) {
    return predicate.test(t);
  }

  @Override
  public Predicate<T> and(Predicate<? super T> other) {
    requireNonNull(other);
    return createPredicate(() -> format("(%s&&%s)", formatter.get(), other), t -> predicate.test(t) && other.test(t));
  }

  @Override
  public Predicate<T> negate() {
    return createPredicate(() -> format("!%s", formatter.get()), predicate.negate());
  }

  @Override
  public Predicate<T> or(Predicate<? super T> other) {
    requireNonNull(other);
    return createPredicate(() -> format("(%s||%s)", formatter.get(), other), t -> predicate.test(t) || other.test(t));
  }

  @Override
  public String toString() {
    return formatter.get();
  }

  protected PrintablePredicate<T> createPredicate(Supplier<String> formatter, Predicate<? super T> predicate) {
    return new PrintablePredicate<>(formatter, predicate);
  }

  public static class Builder<T> {

    private final Predicate<T> predicate;

    public Builder(Predicate<T> predicate) {
      this.predicate = requireNonNull(predicate);
    }

    public Predicate<T> describe(Supplier<String> formatter) {
      return new PrintablePredicate<>(requireNonNull(formatter), predicate);
    }

    public Predicate<T> describe(String description) {
      return describe(() -> description);
    }
  }
}
