package com.github.dakusui.actionunit.core.context.multiparams;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.actionunit.core.context.ContextFunction;
import com.github.dakusui.actionunit.core.context.ContextFunctions;
import com.github.dakusui.actionunit.core.context.ContextPredicate;

import java.util.function.*;

import static com.github.dakusui.actionunit.core.context.ContextFunctions.PLACE_HOLDER_FORMATTER;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.describeFunctionalObject;
import static com.github.dakusui.printables.Printables.consumer;
import static com.github.dakusui.printables.Printables.function;
import static java.util.Objects.requireNonNull;

public enum MultiParameters {
  ;

  public enum Consumers {
    ;
    public static ContextConsumer from(Runnable runnable) {
      return ContextFunctions.contextConsumerFor().with(
          consumer((Params params) -> runnable.run()).describe(runnable::toString)
      );
    }

    public static <T> ContextConsumer of(String variableName, Consumer<T> consumer) {
      return ContextFunctions.contextConsumerFor(variableName)
          .with(consumer(
              (Params params) -> consumer.accept(params.valueOf(variableName))
          ).describe(
              consumer.toString()
          ));
    }

  }

  public enum Functions {
    ;

    public static <T, R> ContextFunction<R> of(String variableName, Function<T, R> function) {
      return ContextFunctions.<R>contextFunctionFor(variableName)
          .with(function(
              (Params params) -> function.apply(params.valueOf(variableName))
          ).describe(
              function.toString()
          ));
    }

  }

  public enum Predicates {
    ;

  }

}
