package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.ActionUnit;
import com.github.dakusui.actionunit.ActionUnit.PerformWith;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.helpers.Actions2;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.utils.TestUtils;
import org.junit.*;
import org.junit.runner.RunWith;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.github.dakusui.actionunit.utils.TestUtils.createActionPerformer;
import static java.util.Arrays.asList;

@FixMethodOrder
@RunWith(ActionUnit.class)
public class Basic implements Actions2 {
  @Retention(RetentionPolicy.RUNTIME)
  public @interface DryRun {
  }

  @BeforeClass
  public static void setUpFirstTime() {
  }

  @Before
  public void setUp() {
  }

  @PerformWith(DryRun.class)
  public Action test0() {
    return simple("action:test0", new Runnable() {
      @Override
      public void run() {
        System.out.println("test0");
      }
    });
  }

  @PerformWith
  public Iterable<Action> testM() {
    return asList(
        simple("action:testM[0]", new Runnable() {
          @Override
          public void run() {
            System.out.println("test1");
          }
        }),
        simple("action:testM[1]", new Runnable() {
          @Override
          public void run() {
            System.out.println("test2");
          }
        }));
  }


  @PerformWith(Test.class)
  public Action[] testN() {
    return new Action[] {
        simple("action:testN[0]", new Runnable() {
          @Override
          public void run() {
            System.out.println("test1");
          }
        }),
        simple("action:testN[1]", new Runnable() {
          @Override
          public void run() {
            System.out.println("test2");
          }
        })
    };
  }

  @PerformWith(Test.class)
  public Action[] testAction() {
    return new Action[] {
        this.<Integer, String>given("100", () -> 100)
            .when("incrementAndToString", input -> Integer.toString(input + 1))
            .then("equalToIgnoringCase", output -> output.equalsIgnoreCase("102")),
        this.<Integer, String>given("100", () -> 100)
            .when("increment", new Function<Integer, String>() {
              @Override
              public String apply(Integer input) {
                return Integer.toString(input + 1);
              }
            })
            .then("equalToIgnoringCase", output -> output.equalsIgnoreCase("101"))
    };
  }

  @PerformWith({ DryRun.class, Test.class })
  public Action test99() {
    return simple("action:test99", new Runnable() {
      @Override
      public void run() {
        System.out.println("test99");
      }
    });
  }

  @PerformWith({ DryRun.class, Test.class })
  public Action timeoutAttemptAndRetryInCombination() {
    final Runnable runnable = createRunnable();
    return timeout(
        attempt(
            simple("A runnable (1)", runnable)
        ).recover(
            ActionException.class,
            e -> retry(
                simple(
                    "A runnable (2)", runnable)
            ).times(2).withIntervalOf(20, TimeUnit.MILLISECONDS)
        ).ensure(
            nop()
        )
    ).in(10, TimeUnit.SECONDS);
  }

  private Runnable createRunnable() {
    return new Runnable() {
      int i = 0;

      @Override
      public void run() {
        System.out.println("Start:run()");
        boolean succeeded = false;
        try {
          if (i++ < 3) {
            String msg = String.format("Error:run(%d)", i);
            System.out.println(msg);
            throw new ActionException(msg);
          }
          succeeded = true;
        } finally {
          System.out.printf("End:run(%s)%n", succeeded);
        }
      }
    };
  }

  @Test
  public void runTestAction(Action action) {
    action.accept(createActionPerformer());
  }

  @DryRun
  public void print(Action action) {
    action.accept(TestUtils.createPrintingActionScanner(Writer.Std.OUT));
  }

  @AfterClass
  public static void tearDownLastTime() {
  }
}
