package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.examples.Example;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import static org.junit.Assert.assertEquals;

public class ExampleTest extends TestUtils.StdOutTestBase {
  @Test
  public void testExample() {
    Result result = JUnitCore.runClasses(Example.class);
    assertEquals(10, result.getRunCount());
    assertEquals(1, result.getFailureCount());
    assertEquals(false, result.wasSuccessful());
  }
}
