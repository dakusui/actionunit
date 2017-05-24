package com.github.dakusui.actionunit.compat.visitors;

import com.github.dakusui.actionunit.compat.Context;
import com.github.dakusui.actionunit.compat.actions.*;
import com.github.dakusui.actionunit.compat.connectors.Connectors;
import com.github.dakusui.actionunit.compat.connectors.Sink;
import com.github.dakusui.actionunit.compat.connectors.Source;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.AutocloseableIterator;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.visitors.ActionRunner;

import static com.github.dakusui.actionunit.helpers.Checks.propagate;
import static com.github.dakusui.actionunit.helpers.Utils.describe;
import static java.lang.String.format;

public abstract class CompatActionRunner extends Action.Visitor.Base implements Context {
  protected static <T> void acceptTagAction(Tag tagAction, CompatWith<T> withAction, ActionRunner runner) {
    tagAction.toLeaf(withAction.getSource(), withAction.getSinks(), runner).accept(runner);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(CompatForEach action) {
    action.getElements(this).accept(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> void visit(final CompatWith<T> action) {
    action.getAction().accept(createChildFor(action));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(final CompatAttempt action) {
    try {
      action.attempt.accept(this);
    } catch (Throwable e) {
      //noinspection unchecked
      if (!action.exceptionClass.isAssignableFrom(e.getClass())) {
        throw propagate(e);
      }
      //noinspection unchecked
      new CompatActionRunnerWithResult.IgnoredInPathCalculation.With<>(Connectors.toSource(e), action.recover, action.sinks).accept(this);
    } finally {
      action.ensure.accept(this);
    }
  }

  /**
   * Subclasses of this class must override this method and return a subclass of
   * it whose {@code visit(Action.With.Tag)} is overridden.
   * And the method must call {@code acceptTagAction(Action.With.Tag, Action.With, ActionRunner)}.
   * <p/>
   * <code>
   * {@literal @}Override
   * public void visit(Action.With.Tag tagAction) {
   * acceptTagAction(tagAction, action, this);
   * }
   * </code>
   *
   * @param action action for which the returned Visitor is created.
   */
  protected <T> CompatActionRunner createChildFor(final CompatWith<T> action) {
    return new ActionRunner() {
      @Override
      public CompatActionRunner getParent() {
        return CompatActionRunner.this;
      }

      @Override
      public Object value() {
        //noinspection unchecked
        return action.getSource().apply(CompatActionRunner.this);
      }

      @Override
      public void visit(Tag tagAction) {
        acceptTagAction(tagAction, action, this);
      }
    };
  }

  /**
   * This interface is used to suppress path calculation, which is
   * performed by {@link CompatActionRunnerWithResult}
   * and its printer.
   */
  public interface IgnoredInPathCalculation {
    abstract class Composite implements com.github.dakusui.actionunit.actions.Composite, IgnoredInPathCalculation {
      final com.github.dakusui.actionunit.actions.Composite inner;

      public Composite(com.github.dakusui.actionunit.actions.Composite inner) {
        this.inner = inner;
      }

      @Override
      public int size() {
        return inner.size();
      }

      @Override
      public AutocloseableIterator<Action> iterator() {
        return inner.iterator();
      }

      public static <T extends Composite> T create(com.github.dakusui.actionunit.actions.Composite composite) {
        Composite ret;
        if (composite instanceof com.github.dakusui.actionunit.actions.Sequential) {
          ret = new Sequential((com.github.dakusui.actionunit.actions.Sequential) composite);
        } else if (composite instanceof com.github.dakusui.actionunit.actions.Concurrent) {
          ret = new Concurrent((com.github.dakusui.actionunit.actions.Concurrent) composite);
        } else {
          throw new ActionException(format("Unknown type of composite action was given: %s", describe(composite)));
        }
        //noinspection unchecked
        return (T) ret;
      }
    }

    /**
     * A sequential action created by and run as a part of {@code CompatForEach} action.
     *
     * @see IgnoredInPathCalculation
     */
    class Sequential extends Composite implements com.github.dakusui.actionunit.actions.Sequential {
      public Sequential(com.github.dakusui.actionunit.actions.Sequential sequential) {
        super(sequential);
      }

      @Override
      public void accept(Visitor visitor) {
        visitor.visit(this);
      }
    }

    class Concurrent extends Composite implements com.github.dakusui.actionunit.actions.Concurrent {
      public Concurrent(com.github.dakusui.actionunit.actions.Concurrent concurrent) {
        super(concurrent);
      }

      @Override
      public void accept(Visitor visitor) {
        visitor.visit(this);
      }
    }

    /**
     * A "with" action created by and run as a part of {@code CompatForEach} action.
     *
     * @param <U> Type of the value with which child {@code Action} is executed.
     */
    class With<U> extends CompatWithBase<U> implements IgnoredInPathCalculation {
      public With(Source<U> source, Action action, Sink<U>[] sinks) {
        super(source, action, sinks);
      }
    }
  }
}
