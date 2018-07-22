package com.github.dakusui.actionunit.core;

import com.github.dakusui.actionunit.actions.ValueHolder;
import com.github.dakusui.actionunit.extras.cmd.Commander;
import com.github.dakusui.actionunit.generators.*;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;

public interface ActionSupport {
  static <I> ActionGenerator<I> simple(String description, RunnableGenerator<I> runnable) {
    return v -> c -> c.simple(description, runnable.get(v, c));
  }

  static <I> ActionGenerator<I> cmd(
      ValueGenerator<I, String> commandLineComposer,
      ConsumerGenerator<I, Commander> consumer) {
    return v -> c -> {
      Commander commander = c.cmd(commandLineComposer.get(v, c));
      consumer.get(v, c).accept(commander);
      return commander.build();
    };
  }

  static <I> ActionGenerator<I> named(String name, ActionGenerator<I> actionGenerator) {
    return v -> c -> c.named(name, actionGenerator.apply(v).apply(c));
  }

  @SafeVarargs
  static <I> ActionGenerator<I> concurrent(ActionGenerator<I>... actionGenerators) {
    return v -> c -> c.concurrent(
        Arrays.stream(actionGenerators)
            .map(each -> each.apply(v).apply(c))
            .collect(toList())
            .toArray(new Action[0]));
  }

  @SafeVarargs
  static <I> ActionGenerator<I> sequential(ActionGenerator<I>... actionGenerators) {
    return v -> c -> c.sequential(
        Arrays.stream(actionGenerators)
            .map(each -> each.apply(v).apply(c))
            .collect(toList())
            .toArray(new Action[0]));
  }

  static <I, E> ForEachGenerator<I, E> forEach(Supplier<Stream<E>> streamSupplier) {
    return ForEachGenerator.create(streamSupplier);
  }

  static <I> WhenGenerator<I> when(BooleanGenerator<I> cond) {
    return WhenGenerator.create(cond);
  }

  static <I> ActionGenerator<I> timeout(ActionGenerator<I> actionGenerator, long durationInSeconds, TimeUnit timeUnit) {
    return v -> c -> c.timeout(actionGenerator.apply(v).apply(c)).in(durationInSeconds, timeUnit);
  }

  static <I> ActionGenerator<I> retry(ActionGenerator<I> actionGenerator, int times, long intervalInSeconds, Class<? extends Throwable> targetExceptionClass) {
    return v -> c -> c.retry(actionGenerator.apply(v).apply(c))
        .times(times)
        .withIntervalOf(intervalInSeconds, SECONDS)
        .on(targetExceptionClass)
        .build();
  }

  static <I> AttemptGenerator<I> attempt(ActionGenerator<I> target) {
    return AttemptGenerator.create(target);
  }

  static <I> RunnableGenerator<I> print(ValueGenerator<I, ?> message) {
    return (ValueHolder<I> v) -> (Context c) -> () -> System.out.println(
        String.format("%s", message.get(v, c))
    );
  }

  static <I> RunnableGenerator<I> throwException() {
    return v -> c -> () -> {
      throw new RuntimeException((Throwable) v.get());
    };
  }

  static <I, T> ActionGenerator<I> setContextVariable(String name, ValueGenerator<I, T> value) {
    return v -> c -> c.simple(
        String.format("Set value to variable '%s'", name),
        () -> c.set(name, value.get(v, c))
    );
  }

  static <I, T> ValueGenerator<I, T> getContextVariable(String name) {
    return v -> c -> c.get(name);
  }

  @SafeVarargs
  static <I> StringGenerator<I> format(StringGenerator<I> format, ValueGenerator<I, ?>... args) {
    return v -> c -> String.format(
        format.get(v, c),
        Arrays.stream(args).map(each -> each.get(v, c)).toArray()
    );
  }

  static <I> ValueGenerator<I, I> theValue() {
    return v -> c -> v.get();
  }


  static void main(String... args) {
    new ReportingActionPerformer.Builder(
        sequential(
            retry(
                setContextVariable("X", StringGenerator.of("weld")),
                2, 1, RuntimeException.class
            ),
            attempt(
                simple("Let's go", print(StringGenerator.of("GO!")))
            ).recover(
                Throwable.class,
                simple("Fail", throwException())
            ).ensure(
                simple("Ensured", print(StringGenerator.of("bye...")))
            ),
            simple(
                "hello",
                print(
                    format(
                        StringGenerator.of(">>>>>%s"),
                        getContextVariable("X")
                    ))),
            forEach(
                () -> Stream.of("hello", "world", "everyone", "!")
            ).perform(
                ActionSupport.concurrent(
                    simple("step1", print(theValue())),
                    simple("step2", print(theValue())),
                    simple("step3", print(theValue())),
                    ActionSupport.<String>when(
                        BooleanGenerator.equalTo(StringGenerator.of("world"))
                    ).perform(
                        simple("MET", print(StringGenerator.of("Condition is met")))
                    )
                )
            )
        ).get(
            ValueHolder.empty(), Context.create()
        )
    ).build().performAndReport();
  }
}
