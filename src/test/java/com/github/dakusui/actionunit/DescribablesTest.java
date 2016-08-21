package com.github.dakusui.actionunit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DescribablesTest {
  @Test
  public void givenNonDesribable$whenDescribe$thenLooksGood() {
    assertEquals(
        "Hello, world",
        Utils.describe("Hello, world")
    );
  }

  @Test
  public void givenNull$whenDescribe$thenLooksGood() {
    assertEquals(
        "null",
        Utils.describe(null)
    );
  }

  @Test
  public void givenToStringOverridden$whenDescribe$thenLooksGood() {
    assertEquals(
        "hello world",
        Utils.describe(
            new Object() {
              @Override
              public String toString() {
                return "hello world";
              }
            }
        )
    );
  }
}
