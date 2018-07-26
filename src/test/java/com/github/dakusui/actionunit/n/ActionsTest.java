package com.github.dakusui.actionunit.n;

import com.github.dakusui.actionunit.utils.TestUtils;
import org.junit.Test;

import java.util.stream.Stream;

import static com.github.dakusui.actionunit.n.Actions.attempt;
import static com.github.dakusui.actionunit.n.Actions.forEach;
import static com.github.dakusui.actionunit.n.Actions.sequential;

public class ActionsTest extends TestUtils.TestBase {
  @Test(expected = IllegalStateException.class)
  public void giveExampleScenarioThatThrowsError$whenPerform$thenExceptionThrown() {
    Action action = forEach(
        "i",
        () -> Stream.of("a", "b", "c")
    ).parallel(
    ).perform(
        attempt(
            sequential(
                Leaf.of(() -> System.out.println("hello")),
                Leaf.create(context -> () -> System.out.println("i=" + context.valueOf("i"))),
                Leaf.of(() -> {
                  throw new IllegalStateException();
                })
            )
        ).ensure(
            Leaf.of(() -> System.out.println("bye: ensured"))
        ).$()
    ).$();

    action.accept(new Action.Visitor.Performer());
  }
}
