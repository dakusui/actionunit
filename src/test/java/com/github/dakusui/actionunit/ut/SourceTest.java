package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.compat.connectors.Source;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SourceTest {
  @Test
  public void givenImmutable$whenToString$thenLooksGood() {
    String toString = new Source.Immutable<>("Hello, world").toString();
    assertEquals("Hello, world", toString);
  }

  @Test
  public void givenMutableNotYetValueSet$whenToString$thenLooksGood() {
    String toString = new Source.Mutable<>().toString();
    assertEquals("(value isn't set yet)", toString);
  }

  @Test
  public void givenMutableValueSetAlready$whenToString$thenLooksGood() {
    Source.Mutable<String> source = new Source.Mutable<>();
    source.set("The value");
    String toString = source.toString();
    assertEquals("current=The value", toString);
  }
}
