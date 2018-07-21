package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.core.Action;
import org.junit.Test;

import java.util.Collections;

import static com.github.dakusui.actionunit.core.ActionSupport.nop;
import static com.github.dakusui.actionunit.core.ActionSupport.parallel;
import static com.github.dakusui.actionunit.core.ActionSupport.sequential;
import static org.junit.Assert.assertFalse;

public class CompositeTest {
  @Test
  public void givenSequentialActionAndNonCompositeAction$whenEquals$thenFalse() {
    // given
    Action action = sequential(Collections.emptyList());
    Action another = nop();
    // when
    boolean result = action.equals(another);
    // then
    assertFalse(result);
  }


  @Test
  public void givenSequentialActionAndAnotherDifferentSequentialAction$whenEquals$thenFalse() {
    // given
    Action action = sequential(Collections.emptyList());
    Action another = sequential(nop());

    // when
    boolean result = action.equals(another);
    // then
    assertFalse(result);
  }


  @Test
  public void givenSequentialActionAndConcurrentAction$whenEquals$thenFalse() {
    // given
    Action action = sequential(Collections.<Action>emptyList());
    Action another = parallel(nop());

    // when
    boolean result = action.equals(another);
    // then
    assertFalse(result);
  }
}
