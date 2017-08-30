package com.github.dakusui.actionunit.utils;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
import org.hamcrest.Matcher;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public enum Matchers {
  ;

  public static <T, U> MatcherFunction<T, U> function(String name, Function<T, U> function) {
    return new MatcherFunction<T, U>() {
      @Override
      public U apply(T t) {
        return function.apply(t);
      }

      @Override
      public String name() {
        return Objects.toString(name);
      }
    };
  }

  public static <T> MatcherPredicate<T> predicate(String name, Predicate<T> predicate) {
    return new MatcherPredicate<T>() {
      @Override
      public boolean test(T t) {
        return predicate.test(t);
      }

      @Override
      public String name() {
        return Objects.toString(name);
      }
    };
  }

  public static <T> Matcher<T> matcher(MatcherPredicate<T> predicate) {
    return new BaseMatcher<T>() {
      @Override
      public boolean matches(Object item) {
        return predicate.test((T) item);
      }

      @Override
      public void describeMismatch(Object item, Description description) {
        description.appendText("was false because x=").appendValue(item);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(predicate.format("x"));
      }
    };
  }

  public static <T, U> Matcher<T> matcher(MatcherFunction<T, U> transform, Matcher<U> matcher) {
    return new BaseMatcher<T>() {
      @SuppressWarnings("unchecked")
      @Override
      public boolean matches(Object item) {
        return matcher.matches(transform.apply((T) item));
      }

      @SuppressWarnings("unchecked")
      @Override
      public void describeMismatch(Object item, Description description) {
        description
            .appendText("was false because " + transform.format("x") + "=")
            .appendValue(transform.apply((T) item))
            .appendText("; x=")
            .appendValue(item)
        ;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(transform.format("x"));
        matcher.describeTo(description);
      }
    };
  }

  /**
   * A bit better version of CoreMatchers.allOf.
   * For example:
   * <pre>assertThat("myValue", allOf(startsWith("my"), containsString("Val")))</pre>
   */
  @SafeVarargs
  public static <T> Matcher<T> allOf(Matcher<? super T>... matchers) {
    /*
    Expected: (
      =='Hello'(0thElement(x)) and
      =='world'(1stElement(x)) and
      =='!'(2ndElement(x))
    )
         but:
      =='Hello'(0thElement(x)) was false because 0thElement(x)="Hello"; x=<[Hello, world, !]>
      =='world'(1stElement(x)) was false because 1stElement(x)="world"; x=<[Hello, world, !]>
      =='!'(2ndElement(x)) was false because 2ndElement(x)="!"; x=<[Hello, world, !]>
     */
    return new DiagnosingMatcher<T>() {
      @Override
      protected boolean matches(Object o, Description mismatch) {
        boolean ret = true;
        for (Matcher<? super T> matcher : matchers) {
          if (!matcher.matches(o)) {
            if (ret)
              mismatch.appendText("(");
            mismatch.appendText("\n  ");
            mismatch.appendDescriptionOf(matcher).appendText(" ");
            matcher.describeMismatch(o, mismatch);
            ret = false;
          }
        }
        if (!ret)
          mismatch.appendText("\n)");
        return ret;
      }

      @Override
      public void describeTo(Description description) {
        description.appendList("(\n  ", " " + "and" + "\n  ", "\n)", Arrays.stream(matchers).collect(toList()));
      }
    };
  }
}
