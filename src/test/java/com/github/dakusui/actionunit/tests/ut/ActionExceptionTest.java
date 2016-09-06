package com.github.dakusui.actionunit.tests.ut;

import com.github.dakusui.actionunit.exceptions.ActionException;
import org.junit.Test;

import java.io.IOException;

public class ActionExceptionTest {
  @Test(expected = ActionException.class)
  public void givenKnownCheckedException$whenWrap$thenActionExceptionThrown() {
    throw ActionException.wrap(new IOException());
  }

  @Test(expected = RuntimeException.class)
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

  @Test(expected = RuntimeException.class)
  public void givenUnknownCheckedExceptioni$whenWrap$thenRuntimeException() {
    throw ActionException.wrap(new UnknownCheckedExceptionForTest());
  }

  public static class UnknownCheckedExceptionForTest extends Exception {
    UnknownCheckedExceptionForTest() {
    }
  }
}
