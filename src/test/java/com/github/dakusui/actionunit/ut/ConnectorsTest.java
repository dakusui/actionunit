package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.Context;
import com.github.dakusui.actionunit.connectors.Connectors;
import com.github.dakusui.actionunit.connectors.Pipe;
import com.github.dakusui.actionunit.connectors.Sink;
import com.google.common.base.Predicate;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.PrintStream;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class ConnectorsTest {
  @Test
  public void givenImmutableWithObjectWhoseToStringOverridden$whenToString$thenGivesStringByOverriddenToString() {
    String toString = Connectors.immutable("HelloWorld").toString();
    assertEquals("HelloWorld", toString);

  }

  @Test
  public void givenImmutableWithObject$whenToString$thenGivesStringByUtilsDescribe() {
    String toString = Connectors.immutable(new PrintStream(System.out)).toString();
    assertEquals("PrintStream", toString);
  }


  @Test
  public void givenSinkCreatedFromPredicate$whenToString$thenGivesStringByUtilsDescribe() {
    String toString = Connectors.toSink(new Predicate<Object>() {
      @Override
      public boolean apply(Object input) {
        return false;
      }
    }).toString();
    assertThat(toString, Matchers.startsWith("Predicate(ConnectorsTest$"));
  }

  @Test
  public void givenSinkCreatedFromPredicate$whenAppliedAndPassed$thenSucceeds() {
    Connectors.toSink(new Predicate<Object>() {
      @Override
      public boolean apply(Object input) {
        return true;
      }
    }).apply(new Object(), createDummyContext());
  }

  @Test(expected = AssertionError.class)
  public void givenSinkCreatedFromPredicate$whenAppliedAndFailed$thenAssertionFailedError() {
    // Since this test expects AssertionError is thrown, you cannot use
    // assertXyz methods which confuses test expectation in case of failure.
    Connectors.toSink(new Predicate<Object>() {
      @Override
      public boolean apply(Object input) {
        return false;
      }
    }).apply(new Object(), createDummyContext());
  }

  @Test
  public void givenDumSink$whenToString$thenLooksGood() {
    String toString = Connectors.dumb().toString();
    assertEquals("Sink(dumb)", toString);
  }

  @Test
  public void givenPipeFromSinkWithoutDescription$whenToString$thenValueCreatedFromClassName() {
    String toString = Connectors.toPipe(new Sink<Object>() {
      @Override
      public void apply(Object input, Context context) {
      }
    }).toString();

    assertThat(toString, Matchers.startsWith("Sink(ConnectorsTest$"));
  }

  @Test
  public void givenPipeFromSinkWithDescription$whenToString$thenValueCreatedFromClassName() {
    String toString = Connectors.toPipe("SinkDescription", new Sink<Object>() {
      @Override
      public void apply(Object input, Context context) {
      }
    }).toString();

    assertThat(toString, Matchers.startsWith("SinkDescription"));
  }

  @Test
  public void givenPipeWithoutDescription$whenToString$thenValueCreatedFromClassName() {
    String toString = Connectors.toPipe(new Function<Object, Object>() {
      @Override
      public Object apply(Object input) {
        return null;
      }
    }).toString();

    assertThat(toString, Matchers.startsWith("Function(ConnectorsTest$"));
  }

  @Test
  public void givenPipeWithDescription$whenToString$thenValueCreatedFromClassName() {
    String toString = Connectors.toPipe("DESCRIPTION", new Function<Object, Object>() {
      @Override
      public Object apply(Object input) {
        return null;
      }
    }).toString();

    assertThat(toString, Matchers.equalTo("DESCRIPTION"));
  }

  @Test
  public void givenPipeWithNullDescription$whenToString$thenValueCreatedFromClassName() {
    String toString = new Pipe.Base<Object, Object>() {
      @Override
      protected Object apply(Object input, Object... outer) {
        return null;
      }
    }.toString();
    assertThat(toString, Matchers.startsWith("ConnectorsTest$"));
  }

  private Context createDummyContext() {
    return new Context() {
      @Override
      public Context getParent() {
        return null;
      }

      @Override
      public Object value() {
        return Connectors.INVALID;
      }
    };
  }
}
