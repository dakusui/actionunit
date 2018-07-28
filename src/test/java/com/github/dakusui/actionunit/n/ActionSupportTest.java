package com.github.dakusui.actionunit.n;

import com.github.dakusui.actionunit.exceptions.ActionTimeOutException;
import com.github.dakusui.actionunit.n.core.Action;
import com.github.dakusui.actionunit.n.core.context.Context;
import com.github.dakusui.actionunit.n.core.context.ContextConsumer;
import com.github.dakusui.actionunit.n.utils.InternalUtils;
import com.github.dakusui.actionunit.n.visitors.ActionPerformer;
import com.github.dakusui.actionunit.utils.TestUtils;
import org.junit.Test;

import java.util.stream.Stream;

import static com.github.dakusui.actionunit.n.utils.ActionSupport.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ActionSupportTest extends TestUtils.TestBase {
  @Test(expected = IllegalStateException.class)
  public void giveExampleScenarioThatThrowsError$whenPerform$thenExceptionThrown() {
    Action action = forEach(
        "i",
        () -> Stream.of("a", "b", "c")
    ).parallel(
    ).perform(
        attempt(
            sequential(
                leaf(
                    $ -> System.out.println("hello")),
                leaf(
                    $ -> System.out.println("i=" + $.valueOf("i"))),
                leaf(
                    $ -> {
                      throw new IllegalStateException();
                    }))
        ).ensure(
            simple(
                "a simple action",
                $ -> System.out.println("bye: ensured : " + $.valueOf("i"))
            )
        )
    );
    action.accept(ActionPerformer.create());
  }


  @Test
  public void givenSequentialAction$whenPerformed$thenWorksFine() {
    Action action = sequential(
        leaf($ -> System.out.println("hello")),
        leaf($ -> System.out.println("world")));

    action.accept(ActionPerformer.create());
  }

  @Test
  public void givenParallelAction$whenPerformed$thenWorksFine() {
    Action action = parallel(
        leaf($ -> System.out.println("hello")),
        leaf($ -> System.out.println("world")));

    action.accept(ActionPerformer.create());
  }

  @Test
  public void givenWhenAction$whenMet$thenPerform() {
    Action action = when(
        c -> true
    ).perform(
        leaf($ -> System.out.println("hello")
        )
    ).otherwise(
        leaf($ -> System.out.println("world")
        )
    );

    action.accept(ActionPerformer.create());
  }

  @Test
  public void givenRetryAction$whenNotMet$thenOtherwise() {
    Action action = when(
        c -> false
    ).perform(
        leaf($ -> System.out.println("hello"))
    ).otherwise(
        leaf($ -> System.out.println("world"))
    );

    action.accept(ActionPerformer.create());
  }

  @Test
  public void givenRetryWithPassingAction$whenPerform$thenNoRetry() {
    Action action = retry(
        leaf($ -> System.out.println("hello"))
    ).$();

    action.accept(ActionPerformer.create());
  }

  @Test
  public void givenRetryWithPassingOnSecondTry$whenPerform$thenFinallyPass() {
    Action action = retry(
        leaf(new ContextConsumer() {
          boolean firstTime = true;

          @Override
          public void accept(Context $) {
            if (firstTime) {
              firstTime = false;
              throw new RuntimeException();
            }
          }
        })
    ).withIntervalOf(
        1, MILLISECONDS
    ).$();

    action.accept(ActionPerformer.create());
  }

  @Test
  public void givenTimeout$whenPerformed$thenPass() {
    Action action = timeout(
        leaf($ -> System.out.println("hello"))
    ).in(
        1, MILLISECONDS
    );

    action.accept(ActionPerformer.create());
  }

  @Test(expected = ActionTimeOutException.class)
  public void givenTimeout$whenPerformed$thenFail() {
    Action action = timeout(
        leaf($ -> InternalUtils.sleep(1, SECONDS))
    ).in(
        5, MILLISECONDS
    );

    action.accept(ActionPerformer.create());
  }

  @Test
  public void givenCmd$when$then() {
    Action action = cmd("/bin/echo").addq("hello").$();

    action.accept(ActionPerformer.create());
  }
}
