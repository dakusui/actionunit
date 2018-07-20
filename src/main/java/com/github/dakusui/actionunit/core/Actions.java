package com.github.dakusui.actionunit.core;

import com.github.dakusui.actionunit.actions.Attempt;
import com.github.dakusui.actionunit.actions.ValueHolder;
import com.github.dakusui.actionunit.actions.When;
import com.github.dakusui.actionunit.extras.cmd.Commander;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;

public interface Actions {
  static ActionGenerator simple(String description, Runnable runnable) {
    return c -> c.simple(description, runnable);
  }

  static <B extends Commander<B>> ActionGenerator cmd(Function<Context, String> commandLineComposer, BiConsumer<Context, Commander<B>> consumer) {
    return c -> {
      //noinspection unchecked
      Commander<B> commander = c.cmd(commandLineComposer.apply(c));
      consumer.accept(c, commander);
      return commander.build();
    };
  }

  static ActionGenerator named(String name, ActionGenerator actionGenerator) {
    return c -> c.named(name, actionGenerator.apply(c));
  }

  static ActionGenerator concurrent(ActionGenerator... actionGenerators) {
    return c -> c.concurrent(
        Arrays.stream(actionGenerators)
            .map(each -> each.apply(c))
            .collect(toList())
            .toArray(new Action[0]));
  }

  static ActionGenerator sequential(ActionGenerator... actionGenerators) {
    return c -> c.sequential(
        Arrays.stream(actionGenerators)
            .map(each -> each.apply(c))
            .collect(toList())
            .toArray(new Action[0]));
  }

  static <T> ActionGenerator forEach(Supplier<Stream<? extends T>> streamSupplier, Function<ValueHolder<T>, ActionGenerator> actionGenerator) {
    return c -> c.forEachOf(streamSupplier).perform(actionGenerator);
  }

  static ActionGenerator when(Function<Context, Supplier<Boolean>> cond, BiConsumer<Context, When.Builder<Boolean>> consumer) {
    return c -> {
      When.Builder<Boolean> b = c.when(cond.apply(c));
      consumer.accept(c, b);
      return b.build();
    };
  }

  static ActionGenerator timeout(ActionGenerator actionGenerator, long durationInSeconds, TimeUnit timeUnit) {
    return c -> c.timeout(actionGenerator.apply(c)).in(durationInSeconds, timeUnit);
  }

  static ActionGenerator retry(ActionGenerator actionGenerator, int times, long intervalInSeconds, Class<? extends Throwable> targetExceptionClass) {
    return c -> c.retry(actionGenerator.apply(c))
        .times(times)
        .withIntervalOf(intervalInSeconds, SECONDS)
        .on(targetExceptionClass)
        .build();
  }

  static <T extends Throwable> ActionGenerator attempt(ActionGenerator actionGenerator, BiConsumer<Context, Attempt.Builder<T>> consumer) {
    return c -> {
      Attempt.Builder<T> b = c.attempt(actionGenerator.apply(c));
      consumer.accept(c, b);
      return b.build();
    };
  }

  static void main(String... args) {
    new ReportingActionPerformer.Builder(
        sequential(
            attempt(
                sequential(
                    retry(
                        c -> c.simple(
                            "SetX",
                            () -> {
                              c.set("X", "weld");
                            }
                        ),
                        2, 1, RuntimeException.class
                    ),
                    simple("Let's go", print(() -> "GO!")),
                    simple("fail", throwException())
                ),
                (c, b) -> b.recover(
                    RuntimeException.class,
                    e -> simple(
                        "printStackTrace",
                        () -> e.get().printStackTrace()
                    )
                ).ensure(
                    simple("bye", print(() -> "BYE"))
                )),
            when(
                c -> () -> Objects.nonNull(c.get("X")),
                (c, b) -> b.perform(simple("HI", print(() -> "hi")))
            ),
            c -> c.simple(
                "hello",
                print(() -> ">>>>>" + c.get("X"))
            ),
            forEach(
                () -> Stream.of("hello", "world", "everyone", "!"),
                v -> concurrent(
                    simple("step1", print(v)),
                    simple("step2", print(v)),
                    simple("step3", print(v))
                ))
        ).apply(
            new Context.Impl()
        )
    ).build().performAndReport();
  }

  static Runnable print(Supplier<String> message) {
    return () -> System.out.println(message.get());
  }

  static Runnable throwException() {
    return () -> {
      throw new RuntimeException();
    };
  }
}
