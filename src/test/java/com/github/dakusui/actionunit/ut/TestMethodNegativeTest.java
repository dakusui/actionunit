package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.ActionUnit;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestMethodNegativeTest implements Context {
  @RunWith(ActionUnit.class)
  public static class NoParameter implements Context {
    @ActionUnit.PerformWith(Test.class)
    public Action testMethod() {
      return simple("not executed", () -> System.out.println("This method will not be executed."));
    }

    @Test
    public void targetMethod() {
    }
  }

  @Test
  public void givenNoParameterMethod$whenRunWithActionUnit$thenFailsOnInitialization() {
    List<Failure> failures = JUnitCore.runClasses(NoParameter.class).getFailures();
    //noinspection ThrowableResultOfMethodCallIgnored
    assertEquals("Method targetMethod() should have one and only one parameter", failures.get(0).getException().getMessage());
    //noinspection ThrowableResultOfMethodCallIgnored
    assertEquals(Exception.class, failures.get(0).getException().getClass());
  }

  @RunWith(ActionUnit.class)
  public static class TooManyParameters implements Context {
    @ActionUnit.PerformWith(Test.class)
    public Action testMethod() {
      return simple("not executed", () -> System.out.println("This method will not be executed."));
    }

    @Test
    public void targetMethod(
        @SuppressWarnings("UnusedParameters") Action a,
        @SuppressWarnings("UnusedParameters") Action b) {
    }
  }

  @Test
  public void givenTooManyParameterMethod$whenRunWithActionUnit$thenFailsOnInitialization() {
    List<Failure> failures = JUnitCore.runClasses(TooManyParameters.class).getFailures();
    assertEquals("Method targetMethod() should have only one parameter", failures.get(0).getException().getMessage());
  }

  @RunWith(ActionUnit.class)
  public static class MismatchParameter implements Context {
    @ActionUnit.PerformWith(Test.class)
    public Action testMethod() {
      return simple("not executed", () -> System.out.println("This method will not be executed."));
    }

    @Test
    public void targetMethod(@SuppressWarnings("UnusedParameters") String a) {
    }
  }

  @Test
  public void givenMismatchParameterMethod$whenRunWithActionUnit$thenFailsOnInitialization() {
    List<Failure> failures = JUnitCore.runClasses(MismatchParameter.class).getFailures();
    //noinspection ThrowableResultOfMethodCallIgnored
    assertEquals("Method targetMethod()'s 1 st parameter must accept an Action", failures.get(0).getException().getMessage());
    //noinspection ThrowableResultOfMethodCallIgnored
    assertEquals(Exception.class, failures.get(0).getException().getClass());
  }

  @RunWith(ActionUnit.class)
  public static class NoRunnerMethod implements Context {
    @ActionUnit.PerformWith(RunWith.class)
    public Action testMethod() {
      return simple("not executed", () -> System.out.println("This method will not be executed."));
    }

    @Test
    public void targetMethod(@SuppressWarnings("UnusedParameters") Action a) {
    }
  }

  @Test
  public void givenNoRunnerMethod$whenRunWithActionUnit$thenFailsOnInitialization() {
    List<Failure> failures = JUnitCore.runClasses(NoRunnerMethod.class).getFailures();
    //noinspection ThrowableResultOfMethodCallIgnored
    assertEquals("No runnable methods", failures.get(0).getException().getMessage());
    //noinspection ThrowableResultOfMethodCallIgnored
    assertEquals(Exception.class, failures.get(0).getException().getClass());
  }
}
