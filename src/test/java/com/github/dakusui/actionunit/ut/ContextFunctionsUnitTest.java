package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.actionunit.core.context.ContextPredicate;
import com.github.dakusui.actionunit.core.context.multiparams.Params;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.multiParamsConsumerFor;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.multiParamsPredicateFor;
import static com.github.dakusui.actionunit.utils.InternalUtils.objectToStringIfOverridden;
import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.printables.Printables.printableConsumer;
import static com.github.dakusui.printables.Printables.printablePredicate;

@RunWith(Enclosed.class)
public class ContextFunctionsUnitTest {
  public static <T> ContextPredicate createContextPredicate(String variableName, Predicate<T> predicate) {
    return multiParamsPredicateFor(variableName)
        .toContextPredicate(
            printablePredicate((Params params) -> predicate.test(params.valueOf(variableName)))
                .describe(() -> objectToStringIfOverridden(predicate, (v) -> "(noname)({{0}})")));
  }

  public static class GivenPrintableContextConsumer {
    List<String>    out = new LinkedList<>();
    ContextConsumer cc  = ContextFunctionsHelperUnitTest.toMultiParamsContextConsumer(
        "i",
        printableConsumer((String each) -> out.add(each)).describe("out.add({{0}}.toString())")
    ).andThen(ContextFunctionsHelperUnitTest.toMultiParamsContextConsumer(
        "i",
        printableConsumer(System.out::println).describe("System.out.println({{0}})")
    ));

    @Test
    public void whenPerformInsideLoop$thenConsumerIsPerformedCorrectly() {

      ReportingActionPerformer.create().performAndReport(
          forEach("i", c -> Stream.of("Hello", "world"))
              .perform(leaf(cc)),
          Writer.Std.OUT
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
      ReportingActionPerformer.create().performAndReport(
          forEach("i", c -> Stream.of("Hello", "world"))
              .perform(leaf(cc)),
          Writer.Std.OUT
      );
      System.out.println(cc.toString());
      assertThat(
          cc,
          asString("toString")
              .equalTo("(i)->out.add(${i}.toString());(i)->System.out.println(${i})").$()
      );
    }
  }

  public static class GivenPrintablePredicate {
    Integer boundary = 100;
    private final ContextPredicate cp = createContextPredicate("j",
        printablePredicate(i -> Objects.equals(i, 0)).describe("{{0}}==0")
    ).or(multiParamsPredicateFor("j").toContextPredicate(
        printablePredicate((Params params) -> params.<Integer>valueOf("i") > 0).describe("{{0}}>0")
    ).and(multiParamsPredicateFor("j").toContextPredicate(
        printablePredicate((Params params) -> params.<Integer>valueOf("j") < boundary).describe(() -> "{{0}}<" + boundary)
    ))).negate();

    @Test
    public void whenPrinted$thenFormattedCorrectly() {
      System.out.println(cp);
      assertThat(
          cp,
          asString("toString").equalTo("!((j)->${j}==0||((j)->${j}>0&&(j)->${j}<100))").$()
      );
    }
  }

  public static class GivenPrintablePredicateAndConsumer {
    Integer          boundary = 100;
    List<String>     out      = new LinkedList<>();
    ContextPredicate cp       = createContextPredicate("j",
        printablePredicate((Integer x) -> Objects.equals(x, 0)).describe("{0}==0")
            .or(printablePredicate((Integer x) -> x > 0).describe("{0}>0"))
            .and(printablePredicate((Integer x) -> x < boundary).describe(() -> "{0}<" + boundary)
            )).negate();

    ContextConsumer cc = multiParamsConsumerFor("i").toContextConsumer(
        printableConsumer((Params params) -> out.add(params.valueOf("i")))
            .describe("out.add({0}.toString)")
    ).andThen(multiParamsConsumerFor("j").toContextConsumer(
        printableConsumer((Params params) -> out.add(params.valueOf("j")))
            .describe("out.add({0}.toString)")
    ));

    @Test
    public void whenPerformedNestedLoop$thenWorksCorrectly() {
      ReportingActionPerformer.create().performAndReport(
          forEach("i", c -> Stream.of("Hello", "world"))
              .perform(
                  forEach("j", c -> Stream.of(-1, 0, 1, 2, 100)).perform(
                      when(cp)
                          .perform(leaf(cc))
                          .otherwise(nop())
                  )), Writer.Std.OUT);

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
}
