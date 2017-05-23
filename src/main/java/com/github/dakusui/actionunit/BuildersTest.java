package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.actions.HandlerFactory;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.List;

import static java.util.Arrays.asList;

@RunWith(Enclosed.class)
public class BuildersTest {
  public static class ForEachTest {
    @Test
    public void givenA_B_and_C$whenRunForEachSequentially$thenWorksFine() {
      Builders.foreach("A", "B", "C")
          .sequentially()
          .perform(HandlerFactory.create(System.out::println))
          .accept(new ActionRunner.Impl());
    }

    @Test
    public void givenA_B_and_C$whenPrintForEachSequentially$thenWorksFine() {
      Builders.foreach("A", "B", "C")
          .sequentially()
          .perform(HandlerFactory.create(System.out::println))
          .accept(new ActionPrinter(ActionPrinter.Writer.Std.OUT));
    }

    @Test
    public void givenA_B_and_C$whenRunForEachConcurrently$thenWorksFine() {
      Builders.foreach("A", "B", "C")
          .concurrently()
          .perform(HandlerFactory.create(System.out::println))
          .accept(new ActionRunner.Impl());
    }

    @Test
    public void givenA_B_and_CAsList$whenRunForEachConcurrently$thenWorksFine() {
      List<Object> data = asList("A", "B", "C");
      Builders.foreach(data)
          .concurrently()
          .perform(HandlerFactory.create(System.out::println))
          .accept(new ActionRunner.Impl());
    }

  }
}
