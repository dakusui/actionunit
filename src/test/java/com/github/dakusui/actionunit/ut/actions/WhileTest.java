package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import com.google.common.base.Predicate;
import org.junit.Test;

import static com.github.dakusui.actionunit.Actions.repeatwhile;
import static com.github.dakusui.actionunit.Actions.simple;

public class WhileTest {
  @Test
  public void test() {
    Action action = repeatwhile(new Predicate() {
                                  int i = 0;

                                  @Override
                                  public boolean apply(Object input) {
                                    return i++ < 4;
                                  }
                                },
        simple(new Runnable() {
          @Override
          public void run() {
            System.out.println("Hello");
          }
        })
    );
    ActionRunner.WithResult runner = new ActionRunner.WithResult();
    try {
      action.accept(runner);
    } finally {
      action.accept(runner.createPrinter());
    }
  }
}
