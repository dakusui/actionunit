package com.github.dakusui.actionunit.core;

import com.github.dakusui.actionunit.actions.ValueHolder;
import com.github.dakusui.actionunit.generators.StringGenerator;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import org.junit.Test;

import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static com.github.dakusui.actionunit.generators.BooleanGenerator.equalTo;

public class ActionSupportTest implements ActionSupport {
  @Test
  public void main() {
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
                        equalTo(StringGenerator.of("world"))
                    ).<String>perform(
                        simple("MET", print(StringGenerator.of("Condition is met")))
                    ).otherwise(
                        simple("NOT MET", print(StringGenerator.of("Condition was not met")))
                    )
                )
            )
        ).get(
            ValueHolder.empty(), Context.create()
        )
    ).build().performAndReport();
  }
}
