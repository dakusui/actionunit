package com.github.dakusui.actionunit.n;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public enum Actions {
  ;

  public static Action nop() {
    return Leaf.of(() -> {
    });
  }

  public static Attempt.Builder attempt(Action perform) {
    return new Attempt.Builder(perform);
  }

  public static <E> ForEach.Builder<E> forEach(String variableName, Supplier<Stream<E>> dataSupplier) {
    return new ForEach.Builder<>(variableName, dataSupplier);
  }

  public static Composite sequential(Action... actions) {
    return sequential(asList(actions));
  }

  public static Composite sequential(List<Action> actions) {
    return new Composite.Builder(actions).build();
  }

  public static Composite parallel(Action... actions) {
    return parallel(asList(actions));
  }

  public static Composite parallel(List<Action> actions) {
    return new Composite.Builder(actions).build();
  }
}
