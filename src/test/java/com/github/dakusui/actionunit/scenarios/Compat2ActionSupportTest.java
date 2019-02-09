package com.github.dakusui.actionunit.scenarios;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import junit.framework.AssertionFailedError;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.*;

@RunWith(Enclosed.class)
public class Compat2ActionSupportTest {
  public static class CmdTest {
    @Test
    public void echoTest() {
      run(
          cmd("echo").add("hello").$()
      );
    }
  }

  public static class AttemptTest extends TestUtils.TestBase {
    @Test
    public void attemptTest1a() {
      run(
          attempt(
              simple("Fail", context -> {
                throw new ActionException("hi");
              })
          ).recover(
              ActionException.class,
              simple("Let's go", context -> print("GO!"))
          ).ensure(
              simple("Ensured", context ->
                  print("bye...")
              )
          ));
    }

    @Test
    public void attemptTest1b() {
      run(
          attempt(
              simple("Fail", context -> {
                throw new AssertionFailedError("hi");
              })
          ).recover(
              Throwable.class,
              simple("Let's go", context -> print("GO!"))
          ).ensure(
              simple("Ensured", context ->
                  print("bye...")
              )
          ));
    }

    @Test(expected = ActionException.class)
    public void attemptTest2() {
      run(
          attempt(
              simple("Fail", context -> {
                throw new ActionException("hi");
              })
          ).ensure(
              simple("Ensured", context -> {
                print("bye...");
              })
          )
      );
    }

    @Test
    public void attemptTest3() {
      run(
          forEach("i", (c) -> Stream.of("Hello", "World")).perform(
              ActionSupport.<String>attempt(
                  leaf(context -> {
                    String i = context.valueOf("i");
                    System.out.println("attempt:" + i);
                    throw new ActionException("exception");
                  })
              ).recover(
                  ActionException.class,
                  leaf(context -> {
                        String i = context.valueOf("i");
                        System.out.println("attempt:" + i);
                      }
                  )
              ).ensure(
                  leaf(context -> {
                        String i = context.valueOf("i");
                        System.out.println("ensure:" + i);
                      }
                  )
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
              retry(leaf(
                  context -> context.assignTo("X", "weld"))
              ).withIntervalOf(1, TimeUnit.SECONDS).on(RuntimeException.class).times(2).$(),
              attempt(
                  simple("Let's go", context -> print("GO!"))
              ).recover(
                  Exception.class,
                  simple("Fail", context -> {
                    throw new RuntimeException();
                  })
              ).ensure(
                  simple("Ensured", context -> print("bye..."))
              ),
              simple(
                  "hello",
                  context -> print(
                      String.format(
                          ">>>>>%s",
                          context.<String>valueOf("X")
                      ))),
              forEach(
                  "i",
                  (c) -> Stream.of("hello", "world", "everyone", "!")
              ).perform(
                  parallel(
                      simple("step1", context -> print(context.valueOf("i"))),
                      simple("step2", context -> print(context.valueOf("i"))),
                      simple("step3", context -> print(context.valueOf("i"))),
                      when(
                          context -> "world".equals(context.valueOf("i"))
                      ).perform(
                          simple("MET", context -> print("Condition is met"))
                      ).otherwise(
                          simple("NOT MET", context -> print("Condition was not met"))
                      )
                  )
              ),
              sequential(
                  simple("Set", context -> context.assignTo("j", "set")),
                  simple("Output", context -> print(context.valueOf("j"))),
                  simple("Override", context -> context.assignTo("j", "override")),
                  simple("Output", context -> print(context.valueOf("j")))
              )
          )
      );
    }
  }

  private static void run(Action action) {
    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(action);
  }

  private static void print(String str) {
    System.out.println(str);
  }
}
