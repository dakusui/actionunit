package com.github.dakusui.printables;

import com.github.dakusui.actionunit.core.context.SerializableFunction;
import com.github.dakusui.actionunit.core.context.SerializableSupplier;

import java.io.Serializable;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class PrintableFunction<T, R> implements Function<T, R>, Serializable {
  private final transient SerializableFunction<? super T, ? extends R> function;
  private final transient SerializableSupplier<String>                 formatter;

  public PrintableFunction(Supplier<String> formatter, Function<? super T, ? extends R> function) {
    this.formatter = () -> requireNonNull(formatter).get();
    this.function = t -> requireNonNull(function).apply(t);
  }

  @Override
  public R apply(T t) {
    return this.function.apply(t);
  }

  public <V> Function<V, R> compose(Function<? super V, ? extends T> before) {
    requireNonNull(before);
    return new PrintableFunction<>(() -> String.format("%s(%s)", formatter.get(), before), this.function.compose(before));
  }

  public <V> Function<T, V> andThen(Function<? super R, ? extends V> after) {
    requireNonNull(after);
    return new PrintableFunction<>(() -> String.format("%s(%s)", after, formatter.get()), this.function.andThen(after));
  }

  protected Supplier<String> getFormatter() {
    return formatter;
  }

  protected Function<? super T, ? extends R> getFunction() {
    return this.function;
  }

  @Override
  public String toString() {
    if (formatter == null)
      return super.toString();
    return formatter.get();
  }


  public static <T, R> PrintableFunction.Builder<T, R> of(Function<T, R> func) {
    return new PrintableFunction.Builder<>(requireNonNull(func));
  }

  public static class Builder<T, R> {

    private final Function<T, R> function;

    public Builder(Function<T, R> function) {
      this.function = requireNonNull(function);
    }

    public Function<T, R> describe(Supplier<String> formatter) {
      return new PrintableFunction<>(requireNonNull(formatter), function);
    }

    public Function<T, R> describe(String description) {
      return describe(() -> description);
    }
  }
}
