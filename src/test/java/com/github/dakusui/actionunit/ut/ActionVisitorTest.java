package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.actions.Composite;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.crest.Crest;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static com.github.dakusui.crest.Crest.*;
import static java.util.Collections.singletonList;

/**
 * Tests for ActionVisitor.
 */
public class ActionVisitorTest extends TestUtils.TestBase {
  private final TestUtils.Out  out     = new TestUtils.Out();
  private       Action.Visitor visitor = new Action.Visitor() {
    @Override
    public void visit(Action action) {
      out.writeLine(String.format("%s", action));
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
        allOf(
            asString("get", 0).equalTo("simpleAction").$(),
            asInteger("size").equalTo(1).$()
        )
    );
  }

  private Action createSimpleAction() {
    return simple("simpleAction", (c) -> {
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
        allOf(
            asString("get", 0).equalTo("named").$(),
            asInteger("size").equalTo(1).$()
        )
    );
  }

  @Test
  public void givenCompositeAction$whenAccept$thenVisited() {
    // given simple action
    Action action = new Composite.Impl(singletonList(createSimpleAction()), false) {
      @Override
      public void accept(Visitor visitor) {
        visitor.visit(this);
      }
    };
    // when accept
    action.accept(visitor);
    // then visited
    assertThat(
        out,
        allOf(
            asString("get", 0).equalTo("do sequentially").$(),
            asInteger("size").equalTo(1).$()
        ));
  }

  @Test
  public void givenForEachAction$whenAccept$thenVisited() {
    // given simple action
    Action action = forEach(
        "i", (c) -> Stream.of("hello")
    ).perform(
        nop()
    );
    // when accept
    action.accept(visitor);
    // then visited
    assertThat(
        out,
        allOf(
            asString("get", 0).startsWith("for each").$(),
            asInteger("size").equalTo(1).$()
        )
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
        allOf(
            asString("get", 0).startsWith("retry").$(),
            asInteger("size").equalTo(1).$()
        ));
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
        allOf(
            asString("get", 0).startsWith("timeout in 1[nanoseconds]").$(),
            Crest.asInteger("size").$()
        ));
  }

  @Test
  public void givenAttemptAction$whenAccept$thenVisited() {
    // given attempt action
    Action action = attempt(createSimpleAction())
        .recover(Exception.class, nop())
        .build();
    // when accept
    action.accept(visitor);
    // then visited
    assertThat(
        out,
        Crest.asString("get", 0).startsWith("attempt").$()
    );
  }

  @Test
  public void givenWhenAction$whenAccept$thenVisited() {
    // given while action
    Action action = when(
        context -> false
    ).perform(
        createSimpleAction()
    ).otherwise(
        createSimpleAction()
    );
    // when accept
    action.accept(visitor);
    // then visited
    assertThat(
        out,
        allOf(
            asString("get", 0).startsWith("if [condition] is satisfied").$(),
            asInteger("size").equalTo(1).$()
        ));
  }
}
