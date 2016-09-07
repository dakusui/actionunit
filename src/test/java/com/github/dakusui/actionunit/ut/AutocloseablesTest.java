package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.AutocloseableIterator;
import com.github.dakusui.actionunit.Autocloseables;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.google.common.base.Function;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.actionunit.Autocloseables.autocloseable;
import static com.github.dakusui.actionunit.utils.TestUtils.hasItemAt;
import static java.util.Arrays.asList;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AutocloseablesTest {
  @Test(expected = Exception.class)
  public void givenErrorThrowingResource$whenClosed$thenWrappedExceptionThrown() {
    AutoCloseable autoCloseable = new AutoCloseable() {
      @Override
      public void close() throws Exception {
        throw new IOException("hello");
      }
    };
    TestUtils.Out out = new TestUtils.Out();
    try (AutocloseableIterator<String> autocloseableIterator = autocloseable(asList("1", "2").iterator(), autoCloseable)) {
      while (autocloseableIterator.hasNext()) {
        out.writeLine(autocloseableIterator.next());
      }
      assertThat(out, allOf(
          hasItemAt(0, equalTo("1")),
          hasItemAt(1, equalTo("2"))
      ));
    };
  }

  @Test(expected = UnsupportedOperationException.class)
  public void givenResource$whenRemoved$thenUnsupportedExceptionThrown() {
    AutoCloseable autoCloseable = new AutoCloseable() {
      @Override
      public void close() throws Exception {
        throw new Exception("hello");
      }
    };
    try (AutocloseableIterator<String> autocloseableIterator = autocloseable(asList("1", "2").iterator(), autoCloseable)) {
      while (autocloseableIterator.hasNext()) {
        autocloseableIterator.remove();
      }
    }
  }

  @Test
  public void givenTransformedCollection$whenCleared$thenRemovedFromBackingList() {
    List<String> original = new LinkedList<>(asList("Hello", "World"));
    Collection transformed = Autocloseables.transformCollection(original, new        Function<String, String>() {
          @Override
          public String apply(String input) {
            return input + "!";
          }
        });
    assertEquals(2, original.size());
    assertEquals(2, transformed.size());

    transformed.clear();

    assertTrue(original.isEmpty());
    assertTrue(transformed.isEmpty());
  }
}
