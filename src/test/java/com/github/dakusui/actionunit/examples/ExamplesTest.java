package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.compat.utils.TestUtils;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class ExamplesTest {
  public static class BasicTest extends TestUtils.ContextTestBase {
    @Test
    public void testExample() {
      Result result = JUnitCore.runClasses(Basic.class);
      assertEquals(11, result.getRunCount());
      assertEquals(1, result.getFailureCount());
      assertEquals(false, result.wasSuccessful());
    }
  }

  public static class NameOfActionTest extends TestUtils.ContextTestBase {
    @Test
    public void testExample() {
      Result result = JUnitCore.runClasses(NameOfAction.class);
      assertEquals(4, result.getRunCount());
      assertEquals(0, result.getFailureCount());
      assertEquals(true, result.wasSuccessful());
    }
  }



  public static class LoopWithRetryExampleTest extends TestUtils.ContextTestBase {
    @Test
    public void testExample() {
      Result result = JUnitCore.runClasses(PracticalExample.class);
      ////
      // Since this test may fail, check only number of run count.
      assertEquals(1, result.getRunCount());
    }
  }

}
