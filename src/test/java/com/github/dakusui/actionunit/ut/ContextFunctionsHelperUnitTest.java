package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.actions.ContextVariable;
import com.github.dakusui.actionunit.actions.ForEach;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextFunctions;
import com.github.dakusui.actionunit.core.context.StreamGenerator;
import com.github.dakusui.actionunit.core.context.multiparams.MultiParamsContextFunctionBuilder;
import com.github.dakusui.actionunit.core.context.multiparams.Params;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import org.junit.Test;

import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.dakusui.actionunit.core.ActionSupport.forEach;
import static com.github.dakusui.actionunit.core.ActionSupport.leaf;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.multiParamsConsumerFor;
import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.printables.PrintableFunctionals.printableConsumer;
import static com.github.dakusui.printables.PrintableFunctionals.printableFunction;

public class ContextFunctionsHelperUnitTest {
  private static <T, R> Function<Context, R> toMultiParamsContextFunction(String variableName, Function<T, R> function) {
    ContextVariable variable = ContextVariable.createGlobal(variableName);
    return ContextFunctions.<R>multiParamsFunctionFor(variable)
        .toContextFunction(printableFunction((Params params) -> function.apply(params.valueOf(variable))).describe(function.toString()));
  }

  static <T> Consumer<Context> toMultiParamsContextConsumer(ContextVariable variable, Consumer<T> consumer) {
    return multiParamsConsumerFor(variable)
        .toContextConsumer(printableConsumer(
            (Params params) -> consumer.accept(params.valueOf(variable))
        ).describe(
            consumer.toString()
        ));
  }

  @Test
  public void test() {
    Function<Context, Integer> function = new MultiParamsContextFunctionBuilder<Integer>(ContextVariable.createGlobal("i"))
        .toContextFunction((Params params) -> params.<Integer>valueOf(ContextVariable.createGlobal("i")) + 1);
    System.out.println(function.toString());

    assertThat(
        function.toString(),
        asString().equalTo("(i)->(noname)(${i})").$()
    );
  }

  @Test
  public void test2() {
    ContextVariable iVariable = ContextVariable.createGlobal("i");
    Context context = Context.create().assignTo(iVariable.internalVariableName(), 0);
    Function<Context, Integer> function = toMultiParamsContextFunction(
        "i",
        printableFunction((Integer i) -> i + 1).describe("inc({{0}})"));
    System.out.println(function);
    System.out.println(function.apply(context));
    assertThat(
        function.toString(),
        asString().equalTo("(i)->inc(${i})").$()
    );
  }

  @Test
  public void test3() {
    ContextVariable iVariable = ContextVariable.createGlobal("x");
    Context context = Context.create().assignTo(iVariable.internalVariableName(), 0);
    Function<Context, Integer> function = toMultiParamsContextFunction("x",
        printableFunction((Integer i) -> i + 1).describe("inc({{0}})")
    ).andThen(
        printableFunction((Integer j) -> j * 2).describe("double")
    );
    System.out.println(function);
    System.out.println(function.apply(context));
    assertThat(
        function.toString(),
        asString().equalTo("double((x)->inc(${x}))").$()
    );
  }

  @Test
  public void test3_fromBuilder() {
    ReportingActionPerformer.create().performAndReport(
        forEach("i", StreamGenerator.fromArray("A", "B", "C")).perform(
            (ForEach.Builder<String> b) -> leaf(multiParamsConsumerFor(b).toContextConsumer(
                params -> System.out.printf("%s:%s%n", b.variableName(), params.valueOf(b).toString())))),
        Writer.Std.OUT);
  }

  @Test
  public void givenLambda$whenPrettyClassName$thenCorrectStringIsGiven() {
    assertThat(
        ContextFunctions.prettyClassName(((Consumer<String>) (c) -> {
        }).getClass()),
        asString().equalTo("ContextFunctionsHelperUnitTest.lambda").$());
  }

  @SuppressWarnings("Convert2Lambda")
  @Test
  public void givenAnonymousInnerClassObjectFromInterface$whenPrettyClassName$thenCorrectStringIsGiven() {
    assertThat(
        ContextFunctions.prettyClassName(new Runnable() {
          @Override
          public void run() {
          }
        }.getClass()),
        asString().equalTo("(anon:Runnable)").$());
  }

  @SuppressWarnings("AnonymousHasLambdaAlternative")
  @Test
  public void givenAnonymousInnerClassObjectFromClass$whenPrettyClassName$thenCorrectStringIsGiven() {
    assertThat(
        ContextFunctions.prettyClassName(new Thread() {
          @Override
          public void run() {
          }
        }.getClass()),
        asString().equalTo("(anon:Thread)").$());
  }
}
