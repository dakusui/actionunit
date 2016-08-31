package com.github.dakusui.actionunit;

import org.junit.Test;

import java.io.IOException;

public class ActionExceptionTest {
  @Test(expected = ActionException.class)
  public void givenKnownCheckedException$whenWrap$thenActionExceptionThrown() {
    throw ActionException.wrap(new IOException());
  }

  @Test(expected = ActionException.class)
  public void givenUnknownCheckedException$whenWrap$thenRuntimeExceptionThrown() {
    throw ActionException.wrap(new Exception());
  }

  @Test(expected = OutOfMemoryError.class)
  public void givenOutOfMemory$whenWrap$thenOutOfMemoryThrown() {
    throw ActionException.wrap(new OutOfMemoryError());
  }

  @Test(expected = NullPointerException.class)
  public void givenNullPointer$thenNullPointerThrown() {
    throw ActionException.wrap(new NullPointerException());
  }
}
