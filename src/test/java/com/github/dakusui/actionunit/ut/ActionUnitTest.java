package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.ActionUnit;
import com.github.dakusui.actionunit.ActionUnit.PerformWith;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.PrintingActionScanner;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static com.github.dakusui.actionunit.core.ActionSupport.nop;
import static com.github.dakusui.actionunit.helpers.Checks.checkNotNull;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ActionUnitTest {
  @RunWith(ActionUnit.class)
  public static class StaticTestMethod {
    @PerformWith(Test.class)
    public static Action staticTestMethod() {
      return nop();
    }

    @Test
    public void run(Action action) {
      action.accept(PrintingActionScanner.Factory.DEFAULT_INSTANCE.create(Writer.Std.OUT));
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
  public static class UnsupportedArrayTypeReturningTestMethod {
    @PerformWith(Test.class)
    public String[] invalidArrayTypeTestMethod() {
      return new String[] { nop().toString() };
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

  @RunWith(ActionUnit.class)
  public static class TestMethodReturnsNull {
    @PerformWith(Test.class)
    public Action testMethod() {
      return null;
    }

    @Test
    public void run(Action action) {
    }
  }

  @RunWith(ActionUnit.class)
  public static class TestMethodThrowsThrowable {
    public static final String EXPECTED_MESSAGE = "Hello, Throwable";

    @PerformWith(Test.class)
    public Action testMethod() throws Throwable {
      throw new Throwable(EXPECTED_MESSAGE);
    }

    @Test
    public void run(Action action) {
    }
  }

  @RunWith(ActionUnit.class)
  public static class TestMethodThrowsIllegalAccess {
    public static final String EXPECTED_MESSAGE = "Hello, IllegalAccessException";

    @PerformWith(Test.class)
    public Action testMethod() throws Throwable {
      throw new IllegalAccessException(EXPECTED_MESSAGE);
    }

    @Test
    public void run(Action action) {
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
  public void givenInvalidArrayMethod$whenRunWithActionUnit$thenError() {
    Result result = JUnitCore.runClasses(UnsupportedArrayTypeReturningTestMethod.class);
    assertEquals(1, checkNotNull(result).getRunCount());
    assertEquals(1, result.getFailureCount());
    assertEquals(false, result.wasSuccessful());
    assertEquals("Method invalidArrayTypeTestMethod() must return Action, its array, or its iterable", result.getFailures().iterator().next().getMessage());
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

  @Test
  public void givenTestMethodReturningNull$whenRunWithActionUnit$thenError() {
    Result result = JUnitCore.runClasses(TestMethodReturnsNull.class);
    assertEquals(1, checkNotNull(result).getRunCount());
    assertEquals(1, result.getFailureCount());
    assertEquals(false, result.wasSuccessful());
    assertEquals("testMethod() returned null", result.getFailures().iterator().next().getMessage());
  }

  @Test
  public void givenTestMethodThrowingThrowable$whenRunWithActionUnit$thenThrowableThrown() {
    Result result = JUnitCore.runClasses(TestMethodThrowsThrowable.class);
    assertEquals(1, checkNotNull(result).getRunCount());
    assertEquals(1, result.getFailureCount());
    assertEquals(false, result.wasSuccessful());
    assertThat(
        result.getFailures().iterator().next().getMessage(),
        containsString(TestMethodThrowsThrowable.EXPECTED_MESSAGE));
  }

  @Test
  public void givenTestMethodThrowingIllegalAccess$whenRunWithActionUnit$thenThrowableThrown() {
    Result result = JUnitCore.runClasses(TestMethodThrowsIllegalAccess.class);
    assertEquals(1, checkNotNull(result).getRunCount());
    assertEquals(1, result.getFailureCount());
    assertEquals(false, result.wasSuccessful());
    assertThat(
        result.getFailures().iterator().next().getMessage(),
        containsString(TestMethodThrowsIllegalAccess.EXPECTED_MESSAGE));
  }
}
