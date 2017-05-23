package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.utils.TestUtils;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class ExamplesTest {
  public static class BasicTest extends TestUtils.TestBase {
    @Test
    public void testExample() {
      Result result = JUnitCore.runClasses(Basic.class);
      assertEquals(11, result.getRunCount());
      assertEquals(1, result.getFailureCount());
      assertEquals(false, result.wasSuccessful());
    }
  }

  public static class NameOfActionTest extends TestUtils.TestBase {
    @Test
    public void testExample() {
      Result result = JUnitCore.runClasses(NameOfAction.class);
      assertEquals(4, result.getRunCount());
      assertEquals(0, result.getFailureCount());
      assertEquals(true, result.wasSuccessful());
    }
  }


  public static class CompatForEachExampleTest extends TestUtils.TestBase {
    @Test
    public void testExample() {
      Result result = JUnitCore.runClasses(CompatForEachExample.class);
      assertEquals(1, result.getRunCount());
      assertEquals(0, result.getFailureCount());
      assertEquals(true, result.wasSuccessful());
    }
  }
}
