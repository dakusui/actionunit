package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.Actions;
import com.github.dakusui.actionunit.compat.CompatActions;
import com.github.dakusui.actionunit.actions.Composite;
import com.github.dakusui.actionunit.utils.TestUtils;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.github.dakusui.actionunit.utils.TestUtils.hasItemAt;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests for ActionVisitor.
 */
public class ActionVisitorTest {
  final TestUtils.Out out = new TestUtils.Out();
  Action.Visitor visitor = new Action.Visitor.Base() {
    @Override
    public void visit(Action action) {
      out.writeLine(action.toString());
    }
  };

  @Test
  public void givenSimpleAction$whenAccept$thenVisited() {
    // given simple action
    Action action = createSimpleAction();
    // when accept
    action.accept(visitor);
    // then visited
    assertThat(
        out,
        hasItemAt(0, equalTo("simpleAction"))
    );
    assertThat(
        out,
        hasSize(1)
    );
  }

  private Action createSimpleAction() {
    return CompatActions.simple(new Runnable() {
      @Override
      public void run() {
      }

      public String toString() {
        return "simpleAction";
      }
    });
  }

  @Test
  public void givenNamesAction$whenAccept$thenVisited() {
    // given simple action
    Action action = Actions.named("named", createSimpleAction());
    // when accept
    action.accept(visitor);
    // then visited
    assertThat(
        out,
        hasItemAt(0, equalTo("named"))
    );
    assertThat(
        out,
        hasSize(1)
    );
  }

  @Test
  public void givenCompositeAction$whenAccept$thenVisited() {
    // given simple action
    Action action = new Composite.Base("", singletonList(createSimpleAction())) {
      @Override
      public void accept(Visitor visitor) {
        visitor.visit(this);
      }

      @Override
      public String toString() {
        return "CompositeActionForTest";
      }
    };
    // when accept
    action.accept(visitor);
    // then visited
    assertThat(
        out,
        hasItemAt(0, equalTo("CompositeActionForTest"))
    );
    assertThat(
        out,
        hasSize(1)
    );
  }

  @Test
  public void givenForEachAction$whenAccept$thenVisited() {
    // given simple action
    Action action = CompatActions.foreach(singletonList("hello"));
    // when accept
    action.accept(visitor);
    // then visited
    assertThat(
        out,
        hasItemAt(0, startsWith("CompatForEach"))
    );
    assertThat(
        out,
        hasSize(1)
    );
  }

  @Test
  public void givenWithAction$whenAccept$thenVisited() {
    // given simple action
    Action action = CompatActions.with("Hello");
    // when accept
    action.accept(visitor);
    // then visited
    assertThat(
        out,
        hasItemAt(0, startsWith("CompatWith"))
    );
    assertThat(
        out,
        hasSize(1)
    );
  }

  @Test
  public void givenRetryAction$whenAccept$thenVisited() {
    // given simple action
    Action action = CompatActions.retry(createSimpleAction(), 1, 1, TimeUnit.NANOSECONDS);
    // when accept
    action.accept(visitor);
    // then visited
    assertThat(
        out,
        hasItemAt(0, startsWith("Retry"))
    );
    assertThat(
        out,
        hasSize(1)
    );
  }

  @Test
  public void givenTimeOutAction$whenAccept$thenVisited() {
    // given timeout action
    Action action = CompatActions.timeout(createSimpleAction(), 1, TimeUnit.NANOSECONDS);
    // when accept
    action.accept(visitor);
    // then visited
    assertThat(
        out,
        hasItemAt(0, startsWith("TimeOut"))
    );
    assertThat(
        out,
        hasSize(1)
    );
  }

  @Test
  public void givenAttemptAction$whenAccept$thenVisited() {
    // given attempt action
    Action action = CompatActions.attempt(createSimpleAction()).build();
    // when accept
    action.accept(visitor);
    // then visited
    assertThat(
        out,
        hasItemAt(0, startsWith("CompatAttempt"))
    );
    assertThat(
        out,
        hasSize(1)
    );
  }

  @Test
  public void givenWhileAction$whenAccept$thenVisited() {
    // given while action
    Action action = CompatActions.repeatwhile(
        v -> true,
        createSimpleAction(),
        createSimpleAction());
    // when accept
    action.accept(visitor);
    // then visited
    assertThat(
        out,
        hasItemAt(0, startsWith("CompatWhile"))
    );
    assertThat(
        out,
        hasSize(1)
    );
  }

  @Test
  public void givenWhenAction$whenAccept$thenVisited() {
    // given while action
    Action action = CompatActions.when(
        v -> true,
        createSimpleAction(),
        createSimpleAction());
    // when accept
    action.accept(visitor);
    // then visited
    assertThat(
        out,
        hasItemAt(0, startsWith("CompatWhen"))
    );
    assertThat(
        out,
        hasSize(1)
    );
  }
}
