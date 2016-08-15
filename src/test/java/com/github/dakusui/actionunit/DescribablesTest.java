package com.github.dakusui.actionunit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DescribablesTest {
  @Test
  public void givenNonDesribable$whenDescribe$thenLooksGood() {
    assertEquals(
        "Hello, world",
        Describables.describe("Hello, world")
    );
  }

  @Test
  public void givenNull$whenDescribe$thenLooksGood() {
    assertEquals(
        "null",
        Describables.describe(null)
    );
  }

  @Test
  public void givenDescribable$whenDescribe$thenLooksGood() {
    assertEquals(
        "hello world",
        Describables.describe(
            new Describable() {
              @Override
              public String describe() {
                return "hello world";
              }
            }
        )
    );
  }
}
