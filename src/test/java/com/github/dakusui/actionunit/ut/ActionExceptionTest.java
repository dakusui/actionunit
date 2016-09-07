package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.exceptions.ActionException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class ActionExceptionTest {
  @Test(expected = ActionException.class)
  public void givenKnownCheckedException$whenWrap$thenActionExceptionThrown() {
    throw ActionException.wrap(new IOException());
  }

  @Test(expected = Error.class)
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

  @Test(expected = Error.class)
  public void givenUnknownCheckedExceptioni$whenWrap$thenRuntimeException() {
    throw ActionException.wrap(new UnknownCheckedExceptionForTest());
  }

  @Test
  public void whenThrowableOnlyConstructorIsInvoked$thenInstantiatedProperly() {
    assertNotNull(new ActionException(new Exception()));
  }

  public static class UnknownCheckedExceptionForTest extends Exception {
    UnknownCheckedExceptionForTest() {
    }
  }
}
