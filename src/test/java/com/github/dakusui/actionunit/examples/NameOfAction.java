package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.ActionUnit;
import com.github.dakusui.actionunit.actions.Named;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.github.dakusui.actionunit.ActionUnit.PerformWith;
import static java.util.Arrays.asList;

/**
 * This example shows how to access name of method by which action passed to test
 * is created.
 */
@FixMethodOrder
@RunWith(ActionUnit.class)
public class NameOfAction implements Context {
  @PerformWith(Test.class)
  public Action aMethodToTestSomething() {
    return forEachOf(
        asList(1, 2, 3)).perform(
        ($, i) -> $.simple("print out incremented value", () -> System.out.println(i.get() + 1))
    );
  }

  @PerformWith(Test.class)
  public List<Action> aMethodToTestSomethingElse() {
    return asList(
        simple("print hello", () -> System.out.println("hello")),
        forEachOf(asList(1, 2, 3)).perform(
            ($, i) -> $.simple("print out incremented value", () -> System.out.println(i.get() + 1))
        ),
        simple("print bye", () -> System.out.println("bye"))
    );
  }

  /**
   * An action to passed to a test method is an instance of {@link Named}.
   * And you can access the name of the test method from which the action is created
   * through {@code getName()} method.
   *
   * If an action is an element of a list/an array returned by a providing method,
   * an index in the container will be returned in brackets.
   * E.g., {@code aMethodToTestSomethingElse[0]}
   *
   * @param action action to be executed by this method.
   */
  @Test
  public void runAction(Action action) {
    System.err.printf("action name:%s%n", ((Named) action).getName());
    new ReportingActionPerformer.Builder(action).build().performAndReport();
  }
}
