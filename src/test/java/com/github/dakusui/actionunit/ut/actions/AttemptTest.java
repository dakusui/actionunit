package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.actionunit.core.context.ContextFunction;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import org.junit.Test;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.github.dakusui.actionunit.core.ActionSupport.attempt;
import static com.github.dakusui.actionunit.core.ActionSupport.leaf;
import static com.github.dakusui.actionunit.core.ActionSupport.sequential;
import static com.github.dakusui.crest.Crest.allOf;
import static com.github.dakusui.crest.Crest.asInteger;
import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class AttemptTest {
  @Test
  public void givenAttemptAction$whenPerform$thenWorksFine() {
    List<String> out = new LinkedList<>();
    buildAttemptAction(out::add).accept(TestUtils.createActionPerformer());

    assertThat(
        out,
        allOf(
            asInteger("size").equalTo(2).$(),
            asString("get", 0).equalTo("UPDATED").$(),
            asString("get", 1).equalTo("UPDATED").$()
            )
    );
  }

  private Action buildAttemptAction(Consumer<String> sink) {
    return sequential(
        leaf(assignTo("i", immediateOf("INITIAL"))),
        attempt(
            leaf(throwIllegalArgument())
        ).recover(
            IllegalArgumentException.class,
            leaf(
                assignTo("i", immediateOf("UPDATED"))
                    .andThen(
                        writeTo(sink, contextValueOf("i")).andThen(
                            printTo(System.out, contextValueOf("i"))
                        )))
        ).ensure(
            leaf(writeTo(sink, contextValueOf("i")).andThen(
                printTo(System.out, contextValueOf("i"))
            ))
        )
    );
  }

  private static <R> ContextConsumer assignTo(String variableName, ContextFunction<R> value) {
    return ContextConsumer.of(
        () -> format("assignTo[%s](%s)", variableName, value),
        (c) -> c.assignTo(variableName, value.apply(c)));
  }

  private static <R> ContextFunction<R> immediateOf(R value) {
    return ContextFunction.of(
        () -> format("immediateOf[%s]", value),
        c -> value
    );
  }

  private static <R> ContextFunction<R> contextValueOf(String variableName) {
    requireNonNull(variableName);
    return ContextFunction.of(
        () -> format("valueOf[%s]", variableName),
        c -> c.valueOf(variableName)
    );
  }

  private static ContextConsumer throwIllegalArgument() {
    return ContextConsumer.of(
        () -> "throw IllegalArgumentException",
        (c) -> {
          throw new IllegalArgumentException();
        }
    );
  }

  private static <R> ContextConsumer printTo(PrintStream ps, ContextFunction<R> value) {
    requireNonNull(ps);
    return ContextConsumer.of(
        () -> format("printTo[%s](%s)", prettyClassName(ps.getClass()), value),
        c -> ps.println(value.apply(c))
    );
  }

  private static <R> ContextConsumer writeTo(Consumer<R> sink, ContextFunction<R> value) {
    requireNonNull(sink);
    return ContextConsumer.of(
        () -> format("writeTo[%s](%s)", prettyClassName(sink.getClass()), value),
        c -> sink.accept(value.apply(c))
    );
  }

  private static String prettyClassName(Class c) {
    String ret = c.getSimpleName();
    if (ret.equals("")) {
      Class mostRecentNamedSuper = mostRecentNamedSuperOf(c);
      if (!mostRecentNamedSuper.equals(Object.class))
        ret = format("(anon:%s)", mostRecentNamedSuperOf(c).getSimpleName());
      else
        ret = Arrays.stream(c.getInterfaces()).map(Class::getSimpleName).collect(Collectors.joining
            (",", "(anon:", ")"));
    }
    return ret.replaceFirst("\\$\\$Lambda\\$[\\d]+/[\\d]+$", ".lambda");
  }

  private static Class mostRecentNamedSuperOf(Class c) {
    if ("".equals(c.getSimpleName()))
      return mostRecentNamedSuperOf(c.getSuperclass());
    return c;
  }

  @Test
  public void test() {
    System.out.println(
        prettyClassName(((Consumer<String>) (c) -> {
        }).getClass())
    );
  }

  @SuppressWarnings("Convert2Lambda")
  @Test
  public void test2() {
    System.out.println(
        prettyClassName(new Runnable() {
          @Override
          public void run() {
          }
        }.getClass())
    );
  }

  @SuppressWarnings("AnonymousHasLambdaAlternative")
  @Test
  public void test3() {
    System.out.println(
        prettyClassName(new Thread() {
          @Override
          public void run() {
          }
        }.getClass())
    );
  }


  @SuppressWarnings("AnonymousHasLambdaAlternative")
  @Test
  public void test4() {
    int i = 0;
    try {
      throw new NullPointerException();
    } catch (NullPointerException e) {
      i = 1;
    } finally {
      System.out.println(i);
    }
  }
}
