package com.github.dakusui.actionunit.utils;

import com.google.common.collect.Iterables;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import static com.google.common.collect.Iterables.size;

public class HasItemAt<T> extends TypeSafeDiagnosingMatcher<Iterable<? super T>> {
  private final Matcher<? super T> elementMatcher;
  private final int                position;

  public HasItemAt(int position, Matcher<? super T> elementMatcher) {
    this.position = position;
    this.elementMatcher = elementMatcher;
  }

  @Override
  public void describeTo(Description description) {
    description
        .appendText("a collection has ")
        .appendDescriptionOf(elementMatcher)
        .appendText(" at " + this.position);
  }

  @Override
  protected boolean matchesSafely(Iterable<? super T> collection, Description mismatchDescription) {
    if (collection == null) {
      mismatchDescription.appendText("was null");
      return false;
    }
    if (this.position >= size(collection)) {
      mismatchDescription.appendText("was not greater than " + this.position);
      return false;
    }
    Object item = Iterables.get(collection, this.position);
    if (this.elementMatcher.matches(item)) {
      return true;
    } else {
      mismatchDescription.appendText("mismatches at " + this.position + " ");
      this.elementMatcher.describeMismatch(item, mismatchDescription);
      return false;
    }
  }
}
