package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.actions.Concurrent;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import com.github.dakusui.actionunit.visitors.ReportingActionRunner;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.Iterator;
import java.util.concurrent.Callable;

import static com.github.dakusui.actionunit.helpers.Actions.*;
import static com.github.dakusui.actionunit.helpers.Builders.forEachOf;
import static com.github.dakusui.actionunit.utils.TestUtils.hasItemAt;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(Enclosed.class)
public class ActionRunnerTest {
  public abstract static class Base extends ActionRunnerTestBase {
    @Override
    protected ActionRunner createRunner() {
      return new ActionRunner.Impl();
    }

    @Override
    public ActionPrinter getPrinter(ReportingActionRunner.Writer writer) {
      return ActionPrinter.Factory.DEFAULT_INSTANCE.create(writer);
    }
  }

  public static class Constructor extends Base {
    @Test(expected = IllegalArgumentException.class)
    public void whenNegativeValueToConstructor$thenIllegalArgumentThrown() {
      try {
        new ActionRunner.Impl(-1);
      } catch (IllegalArgumentException e) {
        assertEquals("Thread pool size must be larger than 0 but -1 was given.", e.getMessage());
        throw e;
      }
    }
  }

  public static class DoubleCompatForEach extends Base {
    @Test
    public void givenDoubleForEachAction$whenPerformed$thenExecutedCorrectly() {
      ////
      // given
      Action action = composeAction();
      try {
        ////
        // when
        action.accept(this.getRunner());
      } finally {
        ////
        // then
        //noinspection unchecked
        assertThat(
            getWriter(),
            allOf(
                hasItemAt(0, equalTo("outer-A")),
                hasItemAt(1, equalTo("\\_inner-a")),
                hasItemAt(2, equalTo("\\_inner-b")),
                hasItemAt(3, equalTo("outer-A")),
                hasItemAt(4, equalTo("outer-B")),
                hasItemAt(5, equalTo("\\_inner-a")),
                hasItemAt(6, equalTo("\\_inner-b")),
                hasItemAt(7, equalTo("outer-B"))
            ));
        assertThat(
            getWriter(),
            hasSize(8)
        );
      }
    }

    private Action composeAction() {
      return forEachOf(asList("A", "B")).perform(
          i -> sequential(
              simple("Prefix with 'outer-'", () -> getWriter().writeLine("outer-" + i.get())),
              forEachOf("a", "b").perform(
                  j -> simple("Prefix with '\\_inner-'", j::get)
              ),
              simple("Prefix with 'outer-'", () -> getWriter().writeLine("outer-" + i.get()))
          )
      );
    }
  }

  public static class ConcurrentActionHandling extends Base {
    @Test(expected = RuntimeException.class)
    public void whenIteratorThrowsException$thenExceptionThrown() {
      Action action = concurrent(nop(), nop());
      action.accept(this.getRunner());
    }

    @Override
    protected ActionRunner createRunner() {
      //noinspection unchecked
      final Iterator<Callable<Boolean>> iterator = mock(Iterator.class);
      Mockito.doThrow(new RuntimeException()).when(iterator).hasNext();
      return new ActionRunner.Impl() {
        @Override
        protected Iterable<Callable<Boolean>> toCallables(Concurrent action) {
          return () -> iterator;
        }
      };
    }
  }
}
