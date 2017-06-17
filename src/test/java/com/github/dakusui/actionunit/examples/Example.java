package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.actions.HandlerFactory;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionFactory;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import org.junit.Test;

import java.util.function.Supplier;

import static java.util.Arrays.asList;

public class Example implements ActionFactory {
  @Test
  public void test() {
    Action action = forEachOf(
        asList("hello", "world")
    ).concurrently(
    ).perform(
        new HandlerFactory.Base<String>() {
          @Override
          protected Action create(Supplier<String> data) {
            return sequential(
                simple("print", () -> System.out.println(data.get())),
                simple("print", () -> System.out.println(data.get())),
                sequential(
                    simple("print", () -> System.out.println(data.get())),
                    simple("print", () -> System.out.println(data.get()))
                )
            );
          }
        }
    );

    new ReportingActionPerformer.Builder(action).build().performAndReport();
  }
}
