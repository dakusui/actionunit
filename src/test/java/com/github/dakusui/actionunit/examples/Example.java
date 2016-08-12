package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.ActionUnit;
import com.github.dakusui.actionunit.ActionUnit.PerformWith;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.github.dakusui.actionunit.Actions.simple;
import static java.util.Arrays.asList;

@FixMethodOrder
@RunWith(ActionUnit.class)
public class Example {
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

  @PerformWith({ DryRun.class, Test.class })
  public Action test99() {
    return simple("action:test99", new Runnable() {
      @Override
      public void run() {
        System.out.println("test99");
      }
    });
  }

  @Test
  public void test(Action action) {
    action.accept(new ActionRunner.Impl());
  }

  @DryRun
  public void print(Action action) {
    System.out.println(action.describe());
  }

  @AfterClass
  public static void tearDownLastTime() {
  }
}
