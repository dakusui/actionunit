package com.github.dakusui.actionunit.scenarios;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.actionunit.exceptions.ActionTimeOutException;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import com.github.dakusui.actionunit.utils.InternalUtils;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import com.github.dakusui.actionunit.visitors.SimpleActionPerformer;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

@RunWith(JUnit4.class)
public class ActionSupportTest extends TestUtils.TestBase {

  private static final Action EXAMPLE_ACTION = forEach(
      "i",
      (c) -> Stream.of("a", "b", "c", "d", "e")
  ).parallelly(
  ).perform(
      attempt(
          sequential(
              timeout(
                  leaf(
                      $ -> System.out.println("hello"))
              ).in(10, SECONDS),
              retry(
                  leaf(
                      $ -> System.out.println("i=" + $.valueOf("i")))
              ).on(
                  Exception.class
              ).withIntervalOf(
                  500, MILLISECONDS
              ).$(),
              nop(),
              forEach("i", (c) -> Stream.of(1, 2, 3)).perform(
                  leaf(
                      $ -> {
                        throw new IllegalStateException("err");
                      })))
      ).recover(
          Exception.class,
          simple(
              "let's recover",
              context -> System.out.println("Exception was caught:" + context.thrownException().getMessage()))
      ).ensure(
          simple(
              "a simple action",
              $ -> System.out.println("bye: ensured : " + $.valueOf("i"))
          )
      )
  );

  @Ignore // This feature is not implemented yet.
  @Test(timeout = 10_000)
  public void makeSureParallelActionFailsFast() {
    parallel(
        leaf($ -> {
          System.out.println("ERROR");
          throw new Error();
        }),
        leaf($ -> {
          try {
            Thread.sleep(SECONDS.toMillis(2));
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        })

    ).accept(SimpleActionPerformer.create());
  }

  @Test
  public void giveExampleScenarioThatThrowsError$whenPerform$thenExceptionThrown() {
    EXAMPLE_ACTION.accept(SimpleActionPerformer.create());
  }


  @Test
  public void givenSequentialAction$whenPerformed$thenWorksFine() {
    Action action = sequential(
        leaf($ -> System.out.println("hello")),
        leaf($ -> System.out.println("world")));

    action.accept(SimpleActionPerformer.create());
  }

  @Test
  public void givenParallelAction$whenPerformed$thenWorksFine() {
    Action action = parallel(
        leaf($ -> System.out.println("hello")),
        leaf($ -> System.out.println("world")));

    action.accept(SimpleActionPerformer.create());
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

    action.accept(SimpleActionPerformer.create());
  }

  @Test
  public void givenWhenAction$whenNotMet$thenOtherwise() {
    Action action = when(
        c -> false
    ).perform(
        leaf($ -> System.out.println("hello"))
    ).otherwise(
        leaf($ -> System.out.println("world"))
    );

    action.accept(SimpleActionPerformer.create());
  }

  @Test
  public void givenRetryWithPassingAction$whenPerform$thenNoRetry() {
    Action action = retry(
        leaf($ -> System.out.println("hello"))
    ).$();

    action.accept(SimpleActionPerformer.create());
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
        1_000, MILLISECONDS
    ).$();

    action.accept(SimpleActionPerformer.create());
  }

  @Test
  public void givenTimeout$whenPerformed$thenPass() {
    Action action = timeout(
        leaf($ -> System.out.println("hello"))
    ).in(
        5, MILLISECONDS
    );

    action.accept(SimpleActionPerformer.create());
  }

  @Test(expected = ActionTimeOutException.class)
  public void givenTimeout$whenPerformed$thenFail() {
    Action action = timeout(
        leaf($ -> InternalUtils.sleep(1, SECONDS))
    ).in(
        5, MILLISECONDS
    );

    action.accept(SimpleActionPerformer.create());
  }

  @Test
  public void print() {
    EXAMPLE_ACTION.accept(new ActionPrinter(Writer.Std.OUT));
  }

  @Test
  public void performAndReport() {
    ReportingActionPerformer.create().performAndReport(EXAMPLE_ACTION, Writer.Std.OUT);
  }
}
