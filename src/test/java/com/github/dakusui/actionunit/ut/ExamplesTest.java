package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.examples.Basic;
import com.github.dakusui.actionunit.examples.NameOfAction;
import com.github.dakusui.actionunit.utils.TestUtils;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class ExamplesTest {
  public static class BasicTest extends TestUtils.StdOutTestBase {
    @Test
    public void testExample() {
      Result result = JUnitCore.runClasses(Basic.class);
      assertEquals(11, result.getRunCount());
      assertEquals(1, result.getFailureCount());
      assertEquals(false, result.wasSuccessful());
    }
  }

  public static class NameOfActionTest extends TestUtils.StdOutTestBase {
    @Test
    public void testExample() {
      Result result = JUnitCore.runClasses(NameOfAction.class);
      assertEquals(4, result.getRunCount());
      assertEquals(0, result.getFailureCount());
      assertEquals(true, result.wasSuccessful());
    }
  }
}
