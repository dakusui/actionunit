package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.exceptions.ActionException;
import org.junit.Test;

public class ActionExceptionTest {
  @Test(expected = ActionException.class)
  public void givenCheckedException$whenWrap$thenActionExceptionThrown() {
    throw ActionException.wrap(new UnknownCheckedExceptionForTest());
  }

  @Test(expected = OutOfMemoryError.class)
  public void givenOutOfMemory$whenWrap$thenOutOfMemoryThrown() {
    throw ActionException.wrap(new OutOfMemoryError());
  }

  @Test(expected = NullPointerException.class)
  public void givenNullPointer$thenNullPointerThrown() {
    throw ActionException.wrap(new NullPointerException());
  }

  @Test(expected = ActionException.class)
  public void givenUnknownCheckedExceptioni$whenWrap$thenRuntimeException() {
    throw ActionException.wrap(new UnknownCheckedExceptionForTest());
  }

  @Test(expected = ActionException.class)
  public void givenNull$whenWrap$thenActionExceptionIsThrown() {
    throw ActionException.wrap(null);
  }

  public static class UnknownCheckedExceptionForTest extends Exception {
    UnknownCheckedExceptionForTest() {
    }
  }
}
