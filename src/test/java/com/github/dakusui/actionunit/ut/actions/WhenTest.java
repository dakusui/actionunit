package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.actions.HandlerFactory;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionFactory;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import org.junit.Test;

import java.util.function.Supplier;

import static java.util.Arrays.asList;

public class WhenTest implements ActionFactory {
  @Test
  public void test() {
    Action action = forEachOf(
        asList(1, 2, 3, 4)
    ).perform(
        new HandlerFactory.Base<Integer>() {
          @Override
          protected Action create(Supplier<Integer> v) {
            return when(
                v,
                (Integer input) -> input > 2
            ).perform(
                new HandlerFactory.Base<Integer>() {
                  @Override
                  protected Action create(Supplier<Integer> data) {
                    return simple(
                        "hello",
                        () -> System.out.println("hello" + data.get())
                    );
                  }
                }
            ).$();
          }
        }
    );
    new ReportingActionPerformer.Builder(action).build().performAndReport();
  }
}
