package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.examples.BasicExample;
import com.github.dakusui.actionunit.examples.ExampleWithName;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class ExamplesTest {
  public static class BasicExampleTest extends TestUtils.StdOutTestBase {
    @Test
    public void testExample() {
      Result result = JUnitCore.runClasses(BasicExample.class);
      assertEquals(10, result.getRunCount());
      assertEquals(1, result.getFailureCount());
      assertEquals(false, result.wasSuccessful());
    }
  }

  public static class ExampleWithNameTest extends TestUtils.StdOutTestBase {
    @Test
    public void testExample() {
      Result result = JUnitCore.runClasses(ExampleWithName.class);
      assertEquals(4, result.getRunCount());
      assertEquals(0, result.getFailureCount());
      assertEquals(true, result.wasSuccessful());
    }
  }
}
