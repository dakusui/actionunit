package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.actions.Retry;
import org.junit.Test;

import static com.github.dakusui.actionunit.Actions.nop;

public class RetryTest {
  @Test(expected = IllegalArgumentException.class)
  public void givenNegativeInterval$whenCreated$thenExceptionThrown() {
    new Retry(nop(), -1 /* this is not valid */, 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void givenNegativeTimes$whenCreated$thenExceptionThrown() {
    new Retry(nop(), 1 , -100 /* this is not valid*/);
  }


  @Test
  public void givenFOREVERAsTimes$whenCreated$thenExceptionNotThrown() {
    // Make sure only an exception is not thrown on instantiation.
    new Retry(nop(), 1 , Retry.INFINITE);
  }
}
