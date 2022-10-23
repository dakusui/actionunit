package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.utils.InternalUtils;
import com.github.dakusui.printables.PrintableFunctionals;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.dakusui.actionunit.utils.InternalUtils.toStringIfOverriddenOrNoname;
import static com.github.dakusui.printables.PrintableFunctionals.printableConsumer;
import static com.github.dakusui.printables.PrintableFunctionals.printableFunction;

public enum TestFunctionals {
  ;

  public static <T, R> Function<T, R> constant(R value) {
    return printableFunction((T in) -> value).describe(toStringIfOverriddenOrNoname(value));
  }

  public static <V> Consumer<V> printVariable() {
    return printableConsumer((V value) -> System.out.println(value)).describe("printVariable");
  }

  public static Function<Integer, Integer> increment() {
    return PrintableFunctionals.<Integer, Integer>printableFunction(i -> i + 1).describe("increment");
  }
}
