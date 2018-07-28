package com.github.dakusui.actionunit.compat.core;

import com.github.dakusui.actionunit.compat.actions.ValueHolder;
import com.github.dakusui.actionunit.n.exceptions.ActionException;
import com.github.dakusui.actionunit.compat.generators.ActionGenerator;
import com.github.dakusui.actionunit.compat.generators.StringGenerator;
import com.github.dakusui.actionunit.compat.utils.TestUtils;
import com.github.dakusui.actionunit.compat.visitors.reporting.ReportingActionPerformer;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.stream.Stream;

import static com.github.dakusui.actionunit.compat.core.ActionSupport.*;
import static com.github.dakusui.actionunit.compat.generators.BooleanGenerator.equalTo;

@RunWith(Enclosed.class)
public class ActionSupportTest {
  public static class CmdTest {
    @Test
    public void echoTest() {
      run(
          cmd(StringGenerator.of("echo"),
              valueHolder -> context -> commander -> commander.add("hello")
          )
      );
    }
  }

  public static class AttemptTest extends TestUtils.TestBase {
    @Test
    public void attemptTest1() {
      run(
          attempt(
              simple("Fail", throwException(() -> new ActionException("hi")))
          ).recover(
              ActionException.class,
              simple("Let's go", print(StringGenerator.of("GO!")))
          ).ensure(
              simple("Ensured", print(StringGenerator.of("bye...")))
          )
      );
    }

    @Test(expected = ActionException.class)
    public void attemptTest2() {
      run(
          attempt(
              simple("Fail", throwException(() -> new ActionException("hi")))
          ).ensure(
              simple("Ensured", print(StringGenerator.of("bye...")))
          )
      );
    }

    @Ignore
    @Test
    public void attemptTest3() {
      run(
          forEach(() -> Stream.of("Hello", "World")).perform(
              ActionSupport.<String>attempt(
                  i -> c -> c.simple("attempt", new Runnable() {
                    @Override
                    public void run() {
                      System.out.println("attempt:" + i.get());
                      throw new ActionException("exception");
                    }
                  })
              ).recover(
                  ActionException.class,
                  i -> c -> c.simple("recover", new Runnable() {
                    @Override
                    public void run() {
                      System.out.println("attempt:" + i.get());
                    }
                  })
              ).ensure(
                  i -> c -> c.simple("ensure", new Runnable() {
                    @Override
                    public void run() {
                      System.out.println("ensure:" + i.get());
                    }
                  })
              )
          )
      );
    }

  }

  public static class Example extends TestUtils.TestBase {
    @Test
    public void main() {
      run(
          sequential(
              retry(
                  setContextVariable("X", StringGenerator.of("weld")),
                  2, 1, RuntimeException.class
              ),
              attempt(
                  simple("Let's go", print(StringGenerator.of("GO!")))
              ).recover(
                  Throwable.class,
                  simple("Fail", throwException())
              ).ensure(
                  simple("Ensured", print(StringGenerator.of("bye...")))
              ),
              simple(
                  "hello",
                  print(
                      format(
                          StringGenerator.of(">>>>>%s"),
                          getContextVariable("X")
                      ))),
              forEach(
                  () -> Stream.of("hello", "world", "everyone", "!")
              ).perform(
                  ActionSupport.concurrent(
                      simple("step1", print(theValue())),
                      simple("step2", print(theValue())),
                      simple("step3", print(theValue())),
                      ActionSupport.<String>when(
                          equalTo(StringGenerator.of("world"))
                      ).<String>perform(
                          simple("MET", print(StringGenerator.of("Condition is met")))
                      ).otherwise(
                          simple("NOT MET", print(StringGenerator.of("Condition was not met")))
                      )
                  ))));
    }
  }

  private static void run(ActionGenerator<?> actionGenerator) {
    new ReportingActionPerformer.Builder(
        actionGenerator.apply(ValueHolder.empty(), Context.create())
    ).build().performAndReport();
  }
}
