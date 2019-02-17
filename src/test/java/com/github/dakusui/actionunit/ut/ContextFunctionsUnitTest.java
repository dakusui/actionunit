package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.actionunit.core.context.ContextFunction;
import com.github.dakusui.actionunit.core.context.ContextPredicate;
import com.github.dakusui.actionunit.core.context.Params;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.forEach;
import static com.github.dakusui.actionunit.core.ActionSupport.leaf;
import static com.github.dakusui.actionunit.core.ActionSupport.nop;
import static com.github.dakusui.actionunit.core.ActionSupport.when;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.contextConsumerFor;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.contextPredicateFor;
import static com.github.dakusui.crest.Crest.allOf;
import static com.github.dakusui.crest.Crest.asInteger;
import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.printables.Printables.consumer;
import static com.github.dakusui.printables.Printables.function;
import static com.github.dakusui.printables.Printables.predicate;

@RunWith(Enclosed.class)
public class ContextFunctionsUnitTest {
  public static class GivenPrintableContextConsumer {
    List<String>    out = new LinkedList<>();
    ContextConsumer cc  = ContextConsumer.of(
        "i",
        consumer((String each) -> out.add(each)).describe("out.add({{0}}.toString())")
    ).andThen(ContextConsumer.of(
        "i",
        consumer(System.out::println).describe("System.out.println({{0}})")
    ));

    @Test
    public void whenPerformInsideLoop$thenConsumerIsPerformedCorrectly() {

      ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(
          forEach("i", c -> Stream.of("Hello", "world"))
              .perform(leaf(cc))
      );
      out.forEach(System.out::println);
      assertThat(
          out,
          allOf(
              asString("get", 0).equalTo("Hello").$(),
              asString("get", 1).equalTo("world").$()));
    }

    @Test
    public void whenPerformInsideLoop$thenConsumerIsFormattedCorrectly() {
      ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(
          forEach("i", c -> Stream.of("Hello", "world"))
              .perform(leaf(cc))
      );
      System.out.println(cc.toString());
      assertThat(
          cc,
          asString("toString")
              .equalTo("(i)->out.add(i.toString());(i)->System.out.println(i)").$()
      );
    }
  }

  public static class GivenPrintablePredicate {
    Integer boundary = 100;
    private final ContextPredicate cp = ContextPredicate.of("j",
        predicate(i -> Objects.equals(i, 0)).describe("{{0}}==0")
    ).or(contextPredicateFor("j").with(
        predicate((Params params) -> params.<Integer>valueOf("i") > 0).describe("{{0}}>0")
    ).and(contextPredicateFor("j").with(
        predicate((Params params) -> params.<Integer>valueOf("j") < boundary).describe(() -> "{{0}}<" + boundary)
    ))).negate();

    @Test
    public void whenPrinted$thenFormattedCorrectly() {
      System.out.println(cp);
      assertThat(
          cp,
          asString("toString").equalTo("!((j)->j==0||((j)->j>0&&(j)->j<100))").$()
      );
    }
  }

  public static class GivenPrintablePredicateAndConsumer {
    Integer          boundary = 100;
    List<String>     out      = new LinkedList<>();
    ContextPredicate cp       = ContextPredicate.of("j",
        predicate((Integer x) -> Objects.equals(x, 0)).describe("{0}==0")
            .or(predicate((Integer x) -> x > 0).describe("{0}>0"))
            .and(predicate((Integer x) -> x < boundary).describe(() -> "{0}<" + boundary)
            )).negate();

    ContextConsumer cc = contextConsumerFor("i").with(
        consumer((Params params) -> out.add(params.valueOf("i")))
            .describe("out.add({0}.toString)")
    ).andThen(contextConsumerFor("j").with(
        consumer((Params params) -> out.add(params.valueOf("j")))
            .describe("out.add({0}.toString)")
    ));

    @Test
    public void whenPerformedNestedLoop$thenWorksCorrectly() {
      ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(
          forEach("i", c -> Stream.of("Hello", "world"))
              .perform(
                  forEach("j", c -> Stream.of(-1, 0, 1, 2, 100)).perform(
                      when(cp)
                          .perform(leaf(cc))
                          .otherwise(nop())
                  )));

      assertThat(
          out,
          allOf(
              asString("get", 0).equalTo("Hello").$(),
              asInteger("get", 1).equalTo(-1).$(),
              asString("get", 2).equalTo("Hello").$(),
              asInteger("get", 3).equalTo(100).$(),
              asString("get", 4).equalTo("world").$(),
              asInteger("get", 5).equalTo(-1).$(),
              asString("get", 6).equalTo("world").$(),
              asInteger("get", 7).equalTo(100).$(),
              asInteger("size").equalTo(8).$()
          )
      );
    }
  }

  public static class ContextFunctionUnitTest {
    @Test
    public void test() {
      ContextFunction<Integer> function = new ContextFunction.Builder<Integer>("i")
          .with((Params params) -> params.<Integer>valueOf("i") + 1);
      System.out.println(function.toString());
    }

    @Test
    public void test2() {
      Context context = Context.create().assignTo("i", 0);
      ContextFunction<Integer> function = ContextFunction.of(
          "i",
          function((Integer i) -> i + 1).describe("inc({{0}})"));
      System.out.println(function);
      System.out.println(function.apply(context));
    }

    @Test
    public void test3() {
      Context context = Context.create().assignTo("i", 0);
      ContextFunction<Integer> function = ContextFunction.of("i",
          function((Integer i) -> i + 1).describe("inc({{0}})")
      ).andThen(
          function((Integer j) -> j * 2).describe("double")
      );
      System.out.println(function);
      System.out.println(function.apply(context));
    }
  }
}
