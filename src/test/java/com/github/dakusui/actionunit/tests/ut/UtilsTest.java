package com.github.dakusui.actionunit.tests.ut;

import com.github.dakusui.actionunit.Utils;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Iterables.toArray;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class UtilsTest {
  @Test
  public void whenRangeIntIsInvoked$thenWorksRight() {
    List<Integer> result = asList(toArray(
        Utils.range(1),
        Integer.class));
    assertEquals(
        singletonList(0),
        result
    );
  }

  @Test
  public void whenRangeIntIntIsInvoked$thenWorksRight() {
    List<Integer> result = asList(toArray(
        Utils.range(0, 3),
        Integer.class));

    assertEquals(
        asList(0, 1, 2),
        result
    );
  }

  @Test
  public void whenRangeIntIntInIsInvoked$thenWorksRight() {
    List<Integer> result = asList(toArray(
        Utils.range(0, 3, 1),
        Integer.class));
    assertEquals(
        asList(0, 1, 2),
        result
    );
  }

  @Test
  public void givenStartAndStopAreAscendingAndNegativeStep$whenRangeIntIntInIsInvoked$thenEmptyReturned() {
    List<Integer> result = asList(toArray(
        Utils.range(0, 3, -1),
        Integer.class));
    assertEquals(
        emptyList(),
        result
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void givenNegativeStep$whenRangeIsInvoked$thenIllegalArgument() {
    Utils.range(0, 1, 0);
  }

  @Test
  public void givenNearIntegerMax$whenGoBeyondMaximum$thenImmediatelyStops() {
    List<Integer> result = asList(toArray(
        Utils.range(Integer.MAX_VALUE, 0, 1),
        Integer.class));
    assertEquals(
        emptyList(),
        result
    );
  }

  @Test
  public void givenToStringNotOverridden$whenDescribe$thenLooksGood() {
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
