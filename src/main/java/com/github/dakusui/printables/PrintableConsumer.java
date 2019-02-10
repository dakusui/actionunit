package com.github.dakusui.printables;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class PrintableConsumer<T> implements Consumer<T> {
  private final Consumer<? super T> consumer;
  private final Supplier<String>    formatter;

  public PrintableConsumer(Supplier<String> formatter, Consumer<? super T> consumer) {
    this.consumer = requireNonNull(consumer);
    this.formatter = requireNonNull(formatter);
  }

  @Override
  public void accept(T t) {
    consumer.accept(t);
  }

  public Consumer<T> andThen(Consumer<? super T> after) {
    Objects.requireNonNull(after);
    return createConsumer(
        () -> format("%s;%s", formatter.get(), after),
        (T t) -> {
          accept(t);
          after.accept(t);
        }
    );
  }

  @Override
  public String toString() {
    return formatter.get();
  }

  protected PrintableConsumer<T> createConsumer(Supplier<String> formatter, Consumer<? super T> consumer) {
    return new PrintableConsumer<>(formatter, consumer);
  }

  public static class Builder<T> {

    private final Consumer<T> consumer;

    public Builder(Consumer<T> predicate) {
      this.consumer = requireNonNull(predicate);
    }

    public Consumer<T> describe(Supplier<String> formatter) {
      return new PrintableConsumer<>(requireNonNull(formatter), consumer);
    }

    public Consumer<T> describe(String description) {
      return describe(() -> description);
    }
  }
}
