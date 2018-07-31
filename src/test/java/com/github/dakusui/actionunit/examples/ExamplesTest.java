package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.ut.utils.TestUtils;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class ExamplesTest {
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
