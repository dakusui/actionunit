package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.ActionUnit;
import com.github.dakusui.actionunit.ActionUnit.PerformWith;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static com.github.dakusui.actionunit.Actions.nop;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertEquals;

public class ActionUnitTest {
  @RunWith(ActionUnit.class)
  public static class StaticTestMethod {
    @PerformWith(Test.class)
    public static Action staticTestMethod() {
      return nop();
    }

    @Test
    public void run(Action action) {
      action.accept(new ActionPrinter<>(ActionPrinter.Writer.Std.OUT));
    }
  }

  @RunWith(ActionUnit.class)
  public static class UnsupportedTypeReturningTestMethod {
    @PerformWith(Test.class)
    public String invalidTypeTestMethod() {
      return nop().toString();
    }
  }

  @RunWith(ActionUnit.class)
  public static class PerformerMethodDoesntHaveActionParameter {
    @PerformWith(Test.class)
    public Action testMethod() {
      return nop();
    }

    @Test
    public void run() {
    }
  }

  @RunWith(ActionUnit.class)
  public static class PerformerMethodHasTooManyActionParameters {
    @PerformWith(Test.class)
    public Action testMethod() {
      return nop();
    }

    @Test
    public void run(Action action, Action action2) {
    }
  }

  @RunWith(ActionUnit.class)
  public static class PerformerMethodHasIncompatibleParameter {
    @PerformWith(Test.class)
    public Action testMethod() {
      return nop();
    }

    @Test
    public void run(String action) {
    }
  }

  @Test
  public void givenStaticTestMethod$whenRunWithActionUnit$thenError() {
    Result result = JUnitCore.runClasses(StaticTestMethod.class);
    assertEquals(1, result.getRunCount());
    assertEquals(1, result.getFailureCount());
    assertEquals(false, result.wasSuccessful());
    assertEquals("Method staticTestMethod() must not be static", result.getFailures().iterator().next().getMessage());
  }

  @Test
  public void givenInvalidMethod$whenRunWithActionUnit$thenError() {
    Result result = JUnitCore.runClasses(UnsupportedTypeReturningTestMethod.class);
    assertEquals(1, checkNotNull(result).getRunCount());
    assertEquals(1, result.getFailureCount());
    assertEquals(false, result.wasSuccessful());
    assertEquals("Method invalidTypeTestMethod() must return Action, its array, or its iterable", result.getFailures().iterator().next().getMessage());
  }

  @Test
  public void givenTooFewParametersPerformerMethod$whenRunWithActionUnit$thenError() {
    Result result = JUnitCore.runClasses(PerformerMethodDoesntHaveActionParameter.class);
    assertEquals(1, checkNotNull(result).getRunCount());
    assertEquals(1, result.getFailureCount());
    assertEquals(false, result.wasSuccessful());
    assertEquals("Method run() should have one and only one parameter", result.getFailures().iterator().next().getMessage());
  }

  @Test
  public void givenTooManyParametersPerformerMethod$whenRunWithActionUnit$thenError() {
    Result result = JUnitCore.runClasses(PerformerMethodHasTooManyActionParameters.class);
    assertEquals(1, checkNotNull(result).getRunCount());
    assertEquals(1, result.getFailureCount());
    assertEquals(false, result.wasSuccessful());
    assertEquals("Method run() should have only one parameter", result.getFailures().iterator().next().getMessage());
  }

  @Test
  public void givenIncompatiblePerformerMethod$whenRunWithActionUnit$thenError() {
    Result result = JUnitCore.runClasses(PerformerMethodHasIncompatibleParameter.class);
    assertEquals(1, checkNotNull(result).getRunCount());
    assertEquals(1, result.getFailureCount());
    assertEquals(false, result.wasSuccessful());
    assertEquals("Method run()'s 1 st parameter must accept an Action", result.getFailures().iterator().next().getMessage());
  }
}
