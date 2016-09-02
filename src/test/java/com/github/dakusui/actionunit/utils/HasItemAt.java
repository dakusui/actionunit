package com.github.dakusui.actionunit.utils;

import com.google.common.collect.Iterables;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class HasItemAt<T> extends TypeSafeMatcher<Iterable<T>> {
  private final Matcher<? extends T> elementMatcher;
  private final int                  position;

  public HasItemAt(int position, Matcher<? extends T> elementMatcher) {
    this.position = position;
    this.elementMatcher = elementMatcher;
  }

  @Override
  public boolean matchesSafely(Iterable<T> ts) {
    return this.elementMatcher.matches(Iterables.get(ts, this.position));
  }

  @Override
  public void describeTo(Description description) {
    description
        .appendText("a collection has ")
        .appendDescriptionOf(elementMatcher)
        .appendText(" at " + this.position);
  }

}
