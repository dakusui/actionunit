package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.utils.Matchers;
import com.github.dakusui.actionunit.utils.TestUtils;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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


  public static class ForEachExampleTest extends TestUtils.TestBase {
    @Test
    public void testExample() {
      Result result = JUnitCore.runClasses(ForEachExample.class);
      assertThat(result,
          Matchers.allOf(
              TestUtils.<Result, Integer>matcherBuilder()
                  .transform("getRunCount", Result::getRunCount)
                  .check("==12", c -> c == 12),
              TestUtils.<Result, Integer>matcherBuilder()
                  .transform("getFailureCount", Result::getFailureCount)
                  .check("==2", c -> c == 2),
              TestUtils.<Result, Boolean>matcherBuilder()
              .transform("wasSuccessful", Result::wasSuccessful)
              .check("==false", v -> !v)
          ));
    }
  }

  public static class WhileExampleTest extends TestUtils.TestBase {
    @Test
    public void testExample() {
      Result result = JUnitCore.runClasses(WhileExample.class);
      assertEquals(1, result.getRunCount());
      assertEquals(0, result.getFailureCount());
      assertEquals(true, result.wasSuccessful());
    }
  }

  public static class LoopWithRetryExampleTest extends TestUtils.TestBase {
    @Test
    public void testExample() {
      Result result = JUnitCore.runClasses(PracticalExample.class);
      ////
      // Since this test may fail, check only number of run count.
      assertEquals(1, result.getRunCount());
    }
  }

}
