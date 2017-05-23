package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.ActionUnit;
import com.github.dakusui.actionunit.ActionUnit.PerformWith;
import com.github.dakusui.actionunit.compat.CompatActions;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.hamcrest.Matchers;
import org.junit.*;
import org.junit.runner.RunWith;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.github.dakusui.actionunit.Actions.*;
import static java.util.Arrays.asList;

@FixMethodOrder
@RunWith(ActionUnit.class)
public class Basic {
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
        CompatActions.<Integer, String>test()
            .given(100)
            .when(input -> Integer.toString(input + 1))
            .then(
                Matchers.equalToIgnoringCase("102")
            )
            .build(),
        CompatActions.<Integer, String>test()
            .given(100)
            .when(new Function<Integer, String>() {
              @Override
              public String apply(Integer input) {
                return Integer.toString(input + 1);
              }
            })
            .then(
                Matchers.equalToIgnoringCase("101")
            )
            .build()
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
        CompatActions.attempt(runnable)
            .recover(retry(simple(runnable), 2, 20, TimeUnit.MILLISECONDS))
            .ensure(nop())
            .build(),
        10,
        TimeUnit.SECONDS
    );
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
    action.accept(new ActionRunner.Impl());
  }

  @DryRun
  public void print(Action action) {
    action.accept(new ActionPrinter(ActionPrinter.Writer.Std.OUT));
  }

  @AfterClass
  public static void tearDownLastTime() {
  }
}
