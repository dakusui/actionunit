package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.actionunit.core.context.ContextFunction;
import com.github.dakusui.actionunit.core.context.ContextFunctions;
import com.github.dakusui.actionunit.core.context.StreamGenerator;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import com.github.dakusui.actionunit.core.context.multiparams.MultiParamsContextFunctionBuilder;
import com.github.dakusui.actionunit.core.context.multiparams.Params;
import org.junit.Test;

import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.dakusui.actionunit.core.ActionSupport.forEach;
import static com.github.dakusui.actionunit.core.ActionSupport.leaf;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.multiParamsConsumerFor;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.contextValueOf;
import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.printables.Printables.printableConsumer;
import static com.github.dakusui.printables.Printables.printableFunction;

public class ContextFunctionsHelperUnitTest {
  private static <T, R> ContextFunction<R> toMultiParamsContextFunction(String variableName, Function<T, R> function) {
    return ContextFunctions.<R>multiParamsFunctionFor(variableName)
        .toContextFunction(printableFunction(
            (Params params) -> function.apply(params.valueOf(variableName))
        ).describe(
            function.toString()
        ));
  }

  static <T> ContextConsumer toMultiParamsContextConsumer(String variableName, Consumer<T> consumer) {
    return multiParamsConsumerFor(variableName)
        .toContextConsumer(printableConsumer(
            (Params params) -> consumer.accept(params.valueOf(variableName))
        ).describe(
            consumer.toString()
        ));
  }

  @Test
  public void test() {
    ContextFunction<Integer> function = new MultiParamsContextFunctionBuilder<Integer>("i")
        .toContextFunction((Params params) -> params.<Integer>valueOf("i") + 1);
    System.out.println(function.toString());

    assertThat(
        function.toString(),
        asString().equalTo("(i)->(noname)(${i})").$()
    );
  }

  @Test
  public void test2() {
    Context context = Context.create().assignTo("i", 0);
    ContextFunction<Integer> function = toMultiParamsContextFunction(
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
    Context context = Context.create().assignTo("i", 0);
    ContextFunction<Integer> function = toMultiParamsContextFunction("i",
        printableFunction((Integer i) -> i + 1).describe("inc({{0}})")
    ).andThen(
        printableFunction((Integer j) -> j * 2).describe("double")
    );
    System.out.println(function);
    System.out.println(function.apply(context));
    assertThat(
        function.toString(),
        asString().equalTo("double((i)->inc(${i}))").$()
    );
  }

  @Test
  public void test3_fromBuilder() {
    ReportingActionPerformer.create().perform(
        forEach("i", StreamGenerator.fromArray("A", "B", "C"))
            .perform(leaf(multiParamsConsumerFor("i").toContextConsumer(
                params -> ContextFunctions.printTo(
                    System.out, contextValueOf("i"))))
            ));
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
