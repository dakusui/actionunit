package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.ActionUnit;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.runner.JUnitCore.runClasses;

public class ActionMethodNegativeTest implements Context {
  public abstract static class Base {
    @Test
    public void runAction(@SuppressWarnings("UnusedParameters") Action action) {
    }
  }

  @RunWith(ActionUnit.class)
  public static class NonPublic extends Base implements Context {
    @ActionUnit.PerformWith
    protected Action nonPublic() {
      return nop();
    }
  }

  @Test
  public void givenNonPublicActionMethod$whenRunWithActionUnit$thenErrorReported() {
    Result result = runClasses(NonPublic.class);
    //noinspection ThrowableResultOfMethodCallIgnored
    assertEquals("Method nonPublic() must be public", result.getFailures().get(0).getException().getMessage());
    //noinspection ThrowableResultOfMethodCallIgnored
    assertEquals(Exception.class, result.getFailures().get(0).getException().getClass());
    assertEquals(1, result.getFailureCount());
  }


  @RunWith(ActionUnit.class)
  public static class NonActionReturning extends Base implements Context {
    @ActionUnit.PerformWith
    public Object nonActionReturning() {
      return nop();
    }
  }

  @Test
  public void givenNonActionReturning$whenRunWithActionUnit$thenErrorReported() {
    Result result = runClasses(NonActionReturning.class);
    //noinspection ThrowableResultOfMethodCallIgnored
    assertEquals("Method nonActionReturning() must return Action, its array, or its iterable", result.getFailures().get(0).getException().getMessage());
    //noinspection ThrowableResultOfMethodCallIgnored
    assertEquals(Exception.class, result.getFailures().get(0).getException().getClass());
    assertEquals(1, result.getFailureCount());
  }


  @RunWith(ActionUnit.class)
  public static class WithParameter extends Base implements Context {
    @ActionUnit.PerformWith
    public Action withParameter(Object arg) {
      return nop();
    }
  }

  @Test
  public void givenWithParameter$whenRunWithActionUnit$thenErrorReported() {
    Result result = runClasses(WithParameter.class);
    //noinspection ThrowableResultOfMethodCallIgnored
    assertEquals("Method withParameter(...) must have no parameters", result.getFailures().get(0).getException().getMessage());
    //noinspection ThrowableResultOfMethodCallIgnored
    assertEquals(Exception.class, result.getFailures().get(0).getException().getClass());
    assertEquals(1, result.getFailureCount());
  }
}
