package com.github.dakusui.actionunit.ut;

import org.junit.Test;

import java.util.List;

import static com.github.dakusui.actionunit.ut.utils.TestUtils.range;
import static com.github.dakusui.actionunit.ut.utils.TestUtils.toList;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class UtilsTest {
  @Test
  public void whenRangeIntIsInvoked$thenWorksRight() {
    List<Integer> result = toList(
        range(1));
    assertEquals(
        singletonList(0),
        result
    );
  }

  @Test
  public void whenRangeIntIntIsInvoked$thenWorksRight() {
    List<Integer> result = toList(
        range(0, 3));

    assertEquals(
        asList(0, 1, 2),
        result
    );
  }

  @Test(expected = UnsupportedOperationException.class)
  public void givenRange$whenRemove$thenUnsupported() {
    range(0, 3).iterator().remove();
  }

  @Test
  public void whenRangeIntIntInIsInvoked$thenWorksRight() {
    List<Integer> result = toList(
        range(0, 3, 1));
    assertEquals(
        asList(0, 1, 2),
        result
    );
  }

  @Test
  public void givenStartAndStopAreAscendingAndNegativeStep$whenRangeIntIntInIsInvoked$thenEmptyReturned() {
    List<Integer> result = toList(
        range(0, 3, -1));
    assertEquals(
        emptyList(),
        result
    );
  }

  @Test
  public void givenStartAndStopAreDescendingAndNegativeStep$whenRangeIntIntInIsInvoked$thenWorksRight() {
    List<Integer> result = toList(
        range(3, 0, -1));
    assertEquals(
        asList(3, 2, 1),
        result
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void givenNegativeStep$whenRangeIsInvoked$thenIllegalArgument() {
    range(0, 1, 0);
  }

}
