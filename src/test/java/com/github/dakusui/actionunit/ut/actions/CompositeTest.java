package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.actions.Composite;
import org.junit.Test;

import java.util.Collections;
import java.util.Iterator;

import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CompositeTest {
  @Test
  public void givenSequentialActionCreatedFromNonCollectionIterable$whenSize$thenCorrectSizeReturned() {
    // given
    Composite action = (Composite) sequential(new Iterable<Action>() {
      @Override
      public Iterator<Action> iterator() {
        return singletonList(nop()).iterator();
      }
    });
    // when
    int size = action.size();
    // then
    assertEquals(-1, size);
  }

  @Test
  public void givenSequentialActionAndNonCompositeAction$whenEquals$thenFalse() {
    // given
    Action action = sequential(Collections.<Action>emptyList());
    Action another = nop();
    // when
    boolean result = action.equals(another);
    // then
    assertFalse(result);
  }


  @Test
  public void givenSequentialActionAndAnotherDifferentSequentialAction$whenEquals$thenFalse() {
    // given
    Action action = sequential(Collections.<Action>emptyList());
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
    Action another = concurrent(nop());

    // when
    boolean result = action.equals(another);
    // then
    assertFalse(result);
  }
}
