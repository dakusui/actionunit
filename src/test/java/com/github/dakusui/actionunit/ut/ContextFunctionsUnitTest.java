package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.actions.ContextVariable;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.actionunit.core.context.ContextPredicate;
import com.github.dakusui.actionunit.core.context.multiparams.Params;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import com.github.dakusui.pcond.TestAssertions;
import com.github.dakusui.pcond.fluent.Fluents;
import com.github.dakusui.pcond.forms.Functions;
import com.github.dakusui.pcond.forms.Predicates;
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
import static com.github.dakusui.printables.PrintableFunctionals.printableConsumer;
import static com.github.dakusui.printables.PrintableFunctionals.printablePredicate;

@RunWith(Enclosed.class)
public class ContextFunctionsUnitTest {
  public static <T> ContextPredicate createContextPredicate(ContextVariable variable, Predicate<T> predicate) {
    return multiParamsPredicateFor(variable)
        .toContextPredicate(
            printablePredicate((Params params) -> predicate.test(params.valueOf(variable)))
                .describe(() -> objectToStringIfOverridden(predicate, (v) -> "(noname)({{0}})")));
  }

  public static class GivenPrintableContextConsumer {
    List<String> out = new LinkedList<>();

    private static ContextConsumer createContextConsumer(GivenPrintableContextConsumer printableContextConsumer, ContextVariable variable) {
      return ContextFunctionsHelperUnitTest.toMultiParamsContextConsumer(
              variable, printableConsumer((String each) -> printableContextConsumer.out.add(each)).describe("out.add({{0}}.toString())"))
          .andThen(ContextFunctionsHelperUnitTest.toMultiParamsContextConsumer(
              variable, printableConsumer(System.out::println).describe("System.out.println({{0}})")));
    }

    @Test
    public void whenPerformInsideLoop$thenConsumerIsPerformedCorrectly() {

      ReportingActionPerformer.create().performAndReport(
          forEach("i", c -> Stream.of("Hello", "world"))
              .perform(b -> leaf(createContextConsumer(this, b))),
          Writer.Std.OUT);
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
          forEach(c -> Stream.of("Hello", "world")).perform(b ->
              leaf(createContextConsumer(this, b))),
          Writer.Std.OUT
      );
      assertThat(
          createContextConsumer(this, ContextVariable.createGlobal("i")),
          asString("toString").equalTo("(i)->out.add(${i}.toString());(i)->System.out.println(${i})").$()
      );
    }
  }

  public static class GivenPrintablePredicate {
    Integer boundary = 100;
    private final ContextPredicate cp = createContextPredicate(
        ContextVariable.createGlobal("j"), printablePredicate(i -> Objects.equals(i, 0)).describe("{{0}}==0")
    ).or(multiParamsPredicateFor(ContextVariable.createGlobal("j")).toContextPredicate(
        printablePredicate((Params params) -> params.<Integer>valueOf(ContextVariable.createGlobal("i")) > 0).describe("{{0}}>0")
    ).and(multiParamsPredicateFor(ContextVariable.createGlobal("j")).toContextPredicate(
        printablePredicate((Params params) -> params.<Integer>valueOf(ContextVariable.createGlobal("j")) < boundary).describe(() -> "{{0}}<" + boundary)
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

  public static class GivenPrintablePredicateAndConsumer extends TestUtils.TestBase {
    Integer      boundary = 100;
    List<Object> out      = new LinkedList<>();

    private ContextPredicate not_$_i_ge_0_and_i_lt_boundary_$(ContextVariable variable) {
      return createContextPredicate(
          variable, printablePredicate((Integer x) -> Objects.equals(x, 0)).describe("{0}==0")
              .or(printablePredicate((Integer x) -> x > 0).describe("{0}>0"))
              .and(printablePredicate((Integer x) -> x < boundary).describe(() -> "{0}<" + boundary)
              )).negate();
    }

    private static ContextConsumer createContextConsumer(GivenPrintablePredicateAndConsumer printableConsumer, ContextVariable i, ContextVariable j) {
      return multiParamsConsumerFor(i).toContextConsumer(
          printableConsumer((Params params) -> printableConsumer.out.add(params.valueOf(i)))
              .describe("out.add({0}.toString)")
      ).andThen(multiParamsConsumerFor(j).toContextConsumer(
          printableConsumer((Params params) -> printableConsumer.out.add(params.valueOf(j)))
              .describe("out.add({0}.toString)")
      ));
    }

    @Test
    public void whenPerformedNestedLoop$thenWorksCorrectly() {
      System.out.print("<" + out + ">");
      ReportingActionPerformer.create().performAndReport(
          forEach("i", c -> Stream.of("Hello", "world")).perform(i ->
              forEach("j", c -> Stream.of(-1, 0, 1, 2, 100)).perform(j ->
                  when(not_$_i_ge_0_and_i_lt_boundary_$(j))
                      .perform(leaf(createContextConsumer(this, i, j)))
                      .otherwise(nop())
              )), Writer.Std.OUT);
      System.out.print("<" + out + ">");

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
              asInteger("size").equalTo(8).$()));
    }
  }
}
