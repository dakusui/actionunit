package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.examples.Example;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class ExampleTest {
  PrintStream stdout = System.out;
  PrintStream stderr = System.err;

  @Before
  public void suppressStdOutErr() {
    System.setOut(new PrintStream(new OutputStream() {
      @Override
      public void write(int b) throws IOException {
      }
    }));
    System.setErr(new PrintStream(new OutputStream() {
      @Override
      public void write(int b) throws IOException {
      }
    }));
  }

  @Test
  public void testExample() {
    Result result = JUnitCore.runClasses(Example.class);
    assertEquals(7, result.getRunCount());
    assertEquals(0, result.getFailureCount());
    assertEquals(true, result.wasSuccessful());
  }

  @After
  public void restoreStdOutErr() {
    System.setOut(stdout);
    System.setOut(stderr);
  }
}
