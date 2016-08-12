package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.visitors.ActionPrinter;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;

import static com.github.dakusui.actionunit.Actions.*;
import static com.google.common.collect.Iterables.size;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;

public class ActionPrinterTest {
  private Action composeAction() {
    return concurrent("Concurrent",
        sequential("Sequential",
            simple("simple1", new Runnable() {
              @Override
              public void run() {
              }
            }),
            simple("simple2", new Runnable() {
              @Override
              public void run() {
              }
            })
        ),
        simple("simple3", new Runnable() {
          @Override
          public void run() {
          }
        }),
        forEach(
            asList("hello1", "hello2", "hello3"),
            new Block.Base<String>("block1") {
              @Override
              public void apply(String input, Object... outer) {

              }
            }
        )
    );
  }

  @Test
  public void givenStdout$whenTestActionAccepts$thenNoErrorWillBeGiven() {
    PrintStream stdout = System.out;
    System.setOut(new PrintStream(new OutputStream() {
      @Override
      public void write(int b) throws IOException {
      }
    }));
    try {
      this.composeAction().accept(ActionPrinter.stdout());
    } finally {
      System.setOut(stdout);
    }
  }

  @Test
  public void givenStderr$whenTestActionAccepts$thenNoErrorWillBeGiven() {
    PrintStream stderr = System.err;
    System.setErr(new PrintStream(new OutputStream() {
      @Override
      public void write(int b) throws IOException {
      }
    }));
    try {
      this.composeAction().accept(ActionPrinter.stderr());
    } finally {
      System.setErr(stderr);
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  public void givenStderr$whenAccessRecord$thenUnsupportedOperationThrown() {
    ActionPrinter.stderr().iterator();
  }

  @Test
  public void givenTrace() {
    this.composeAction().accept(ActionPrinter.trace());
  }

  @Test
  public void givenDebug$whenTestActionAccepts$thenNoErrorWillBeGiven() {
    this.composeAction().accept(ActionPrinter.debug());
  }

  @Test
  public void givenInfo$whenTestActionAccepts$thenNoErrorWillBeGiven() {
    this.composeAction().accept(ActionPrinter.info());
  }

  @Test
  public void givenWarn() {
    this.composeAction().accept(ActionPrinter.warn());
  }

  @Test
  public void givenError() {
    this.composeAction().accept(ActionPrinter.error());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void givenSlf4JPrinter$whenAccessRecord$thenUnsupportedOperationThrown() {
    ActionPrinter.debug().iterator();
  }

  @Test
  public void givenNew() {
    ActionPrinter printer = ActionPrinter.create();
    this.composeAction().accept(printer);
    Iterator<String> i = printer.iterator();
    assertThat(i.next(), containsString("Concurrent"));
    assertThat(i.next(), containsString("Sequential"));
    assertThat(i.next(), containsString("simple1"));
    assertThat(i.next(), containsString("simple2"));
    assertThat(i.next(), containsString("simple3"));
    assertThat(i.next(), containsString("ForEach"));
    assertEquals(6, size(printer));
  }
}
