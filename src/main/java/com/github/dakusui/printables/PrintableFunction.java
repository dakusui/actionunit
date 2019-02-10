package com.github.dakusui.printables;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class PrintableFunction<T, R> implements Function<T, R> {
  private final Supplier<String>                 s;
  private final Function<? super T, ? extends R> function;

  PrintableFunction(Supplier<String> s, Function<? super T, ? extends R> function) {
    this.s = Objects.requireNonNull(s);
    this.function = Objects.requireNonNull(function);
  }

  @Override
  public R apply(T t) {
    return this.function.apply(t);
  }

  public <V> Function<V, R> compose(Function<? super V, ? extends T> before) {
    Objects.requireNonNull(before);
    return new PrintableFunction<V, R>(() -> String.format("%s%s", before, s), this.function.compose(before));
  }

  public <V> Function<T, V> andThen(Function<? super R, ? extends V> after) {
    Objects.requireNonNull(after);
    return new PrintableFunction<T, V>(() -> String.format("%s%s", s, after), this.function.andThen(after));
  }

  @Override
  public String toString() {
    return s.get();
  }
}
