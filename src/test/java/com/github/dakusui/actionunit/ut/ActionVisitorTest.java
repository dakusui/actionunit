package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.actions.Composite;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
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
public class ActionVisitorTest implements Context {
  final   TestUtils.Out  out     = new TestUtils.Out();
  private Action.Visitor visitor = new Action.Visitor.Base() {
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
    return simple("simpleAction", () -> {
    });
  }

  @Test
  public void givenNamesAction$whenAccept$thenVisited() {
    // given simple action
    Action action = named("named", createSimpleAction());
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
    Action action = new Composite.Base(0, "noname", singletonList(createSimpleAction())) {
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
    Action action = forEachOf(singletonList("hello")).perform(($, s) -> nop());
    // when accept
    action.accept(visitor);
    // then visited
    assertThat(
        out,
        hasItemAt(0, startsWith("ForEach"))
    );
    assertThat(
        out,
        hasSize(1)
    );
  }

  @Test
  public void givenRetryAction$whenAccept$thenVisited() {
    // given simple action
    Action action = retry(createSimpleAction()).times(1).withIntervalOf(1, TimeUnit.NANOSECONDS).build();
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
    Action action = timeout(createSimpleAction()).in(1, TimeUnit.NANOSECONDS);
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
    Action action = attempt(createSimpleAction())
        .recover(Exception.class, ($, e) -> nop())
        .build();
    // when accept
    action.accept(visitor);
    // then visited
    assertThat(
        out,
        hasItemAt(0, startsWith("Attempt"))
    );
    assertThat(
        out,
        hasSize(1)
    );
  }

  @Test
  public void givenWhileAction$whenAccept$thenVisited() {
    // given while action
    Action action = whilst(
        () -> "Hello",
        v -> true
    ).perform(
        $ -> sequential(
            createSimpleAction(),
            createSimpleAction()
        )
    );
    // when accept
    action.accept(visitor);
    // then visited
    assertThat(
        out,
        hasItemAt(0, startsWith("While"))
    );
    assertThat(
        out,
        hasSize(1)
    );
  }

  @Test
  public void givenWhenAction$whenAccept$thenVisited() {
    // given while action
    Action action = when(
        () -> "Hello",
        "Hello"::equals
    ).perform(
        $ -> createSimpleAction()
    ).otherwise(
        $ -> createSimpleAction()
    );
    // when accept
    action.accept(visitor);
    // then visited
    assertThat(
        out,
        hasItemAt(0, startsWith("When"))
    );
    assertThat(
        out,
        hasSize(1)
    );
  }
}
