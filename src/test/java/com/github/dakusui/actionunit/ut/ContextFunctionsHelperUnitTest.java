package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextFunction;
import com.github.dakusui.actionunit.core.context.ContextFunctions;
import com.github.dakusui.actionunit.core.context.Params;
import org.junit.Test;

import java.util.function.Consumer;

import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.printables.Printables.function;

public class ContextFunctionsHelperUnitTest {
  @Test
  public void test() {
    ContextFunction<Integer> function = new ContextFunction.Builder<Integer>("i")
        .with((Params params) -> params.<Integer>valueOf("i") + 1);
    System.out.println(function.toString());

    assertThat(
        function.toString(),
        asString().equalTo("(i)->(noname)(i)").$()
    );
  }

  @Test
  public void test2() {
    Context context = Context.create().assignTo("i", 0);
    ContextFunction<Integer> function = ContextFunction.of(
        "i",
        function((Integer i) -> i + 1).describe("inc({{0}})"));
    System.out.println(function);
    System.out.println(function.apply(context));
    assertThat(
        function.toString(),
        asString().equalTo("(i)->inc(i)").$()
    );
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
    assertThat(
        function.toString(),
        asString().equalTo("double((i)->inc(i))").$()
    );
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
