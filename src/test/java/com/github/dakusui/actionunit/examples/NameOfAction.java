package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.ActionUnit;
import com.github.dakusui.actionunit.actions.Named;
import com.github.dakusui.actionunit.compat.connectors.Sink;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.github.dakusui.actionunit.ActionUnit.PerformWith;
import static com.github.dakusui.actionunit.compat.CompatActions.foreach;
import static com.github.dakusui.actionunit.Actions.simple;
import static java.util.Arrays.asList;

/**
 * This example shows how to access name of method by which action passed to test
 * is created.
 */
@FixMethodOrder
@RunWith(ActionUnit.class)
public class NameOfAction {
  @PerformWith(Test.class)
  public Action aMethodToTestSomething() {
    return foreach(
        asList(1, 2, 3),
        new Sink.Base<Integer>() {
          @Override
          protected void apply(Integer input, Object... outer) {
            System.out.println(input + 1);
          }
        }
    );
  }

  @PerformWith(Test.class)
  public List<Action> aMethodToTestSomethingElse() {
    return asList(
        simple(new Runnable() {
          @Override
          public void run() {
            System.out.println("hello");
          }
        }),
        foreach(
            asList(1, 2, 3),
            new Sink.Base<Integer>() {
              @Override
              protected void apply(Integer input, Object... outer) {
                System.out.println(input + 1);
              }
            }
        ),
        simple(new Runnable() {
          @Override
          public void run() {
            System.out.println("bye");
          }
        })
    );
  }

  /**
   * An action to passed to a test method is an instance of {@link Named}.
   * And you can access the name of the test method from which the action is created
   * through {@code getName()} method.
   * <p>
   * If an action is an element of a list/an array returned by a providing method,
   * an index in the container will be returned in brackets.
   * E.g., {@code aMethodToTestSomethingElse[0]}
   *
   * @param action action to be executed by this method.
   */
  @Test
  public void runAction(Action action) {
    ActionRunner.WithResult runner = new ActionRunner.WithResult();
    ////
    // Here, this line will print lines like
    //   action name:aMethodToTestSomething
    //   action name:aMethodToTestSomethingElse[0]
    System.err.printf("action name:%s%n", ((Named) action).getName());
    action.accept(runner);
    action.accept(runner.createPrinter());
  }
}
