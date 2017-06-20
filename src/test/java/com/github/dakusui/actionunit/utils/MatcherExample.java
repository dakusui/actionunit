package com.github.dakusui.actionunit.utils;

import org.junit.Test;

import java.util.List;

import static com.github.dakusui.actionunit.utils.Matchers.allOf;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class MatcherExample {
  private <T> MatcherFunction<List<T>, T> elementAt(int i) {
    return new MatcherFunction<List<T>, T>() {
      @Override
      public T apply(List<T> ts) {
        return ts.get(i);
      }

      @Override
      public String format(String varName) {
        return String.format("elementAt(%s,%s)", varName, i);
      }
    };
  }

  @Test
  public void test() {
    List<String> data = asList("Hello", "world", "everyone", "!");
    assertThat(
        data,
        Matchers.matcher(
            elementAt(0),
            allOf(
                containsString("O"),
                containsString("o")
            )));
  }

}
