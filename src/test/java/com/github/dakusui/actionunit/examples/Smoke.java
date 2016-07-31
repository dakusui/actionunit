package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.ActionUnit;
import com.github.dakusui.actionunit.Actions;
import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(ActionUnit.class)
public class Smoke {
  @BeforeClass
  public static Action setUpFirstTime() {
    return Actions.simple(new Runnable() {
      @Override
      public void run() {
        System.out.println("setUpFirstTime");
      }
    });
  }

  @Before
  public Action setUp() {
    return Actions.simple(new Runnable() {
      @Override
      public void run() {
        System.out.println("setUp");
      }
    });
  }

  @Test
  public Action test0() {
    return Actions.simple("action:test0", new Runnable() {
      @Override
      public void run() {
        System.out.println("test0");
      }
    });
  }

  @Test
  public Action[] testN() {
    return new Action[] {
        Actions.simple("action:testN[0]", new Runnable() {
          @Override
          public void run() {
            System.out.println("test1");
          }
        }),
        Actions.simple("action:testN[1]", new Runnable() {
          @Override
          public void run() {
            System.out.println("test2");
          }
        })
    };
  }

  @After
  public Action tearDown() {
    return Actions.simple(new Runnable() {
      @Override
      public void run() {
        System.out.println("tearDown");
      }
    });
  }

  @AfterClass
  public static Action tearDownLastTime() {
    return Actions.simple(new Runnable() {
      @Override
      public void run() {
        System.out.println("tearDownLastTime");
      }
    });
  }
}
