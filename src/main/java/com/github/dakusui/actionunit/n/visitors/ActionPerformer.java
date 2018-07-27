package com.github.dakusui.actionunit.n.visitors;

import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.n.actions.*;
import com.github.dakusui.actionunit.n.core.Action;
import com.github.dakusui.actionunit.n.core.context.Context;
import com.github.dakusui.actionunit.n.utils.InternalUtils;

import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class ActionPerformer implements Action.Visitor {
  private Context context;

  private ActionPerformer() {
    this(Context.create());
  }

  private ActionPerformer(Context context) {
    this.context = context;
  }

  public static ActionPerformer create() {
    return new ActionPerformer();
  }

  @Override
  public void visit(Leaf action) {
    action.runnable(context).run();
  }

  @Override
  public void visit(Named action) {
    action.action().accept(this);
  }

  @Override
  public void visit(Composite action) {
    Stream<Action> actionStream = action.isParallel()
        ? action.children().parallelStream()
        : action.children().stream();
    actionStream.forEach(
        a -> a.accept(this)
    );
  }

  @Override
  public <E> void visit(ForEach<E> action) {
    Stream<E> data = action.isParallel()
        ? action.data().parallel()
        : action.data();
    data.forEach(
        e -> action.perform().accept(
            new ActionPerformer(
                this.context.createChild().assignTo(
                    action.loopVariableName(),
                    e
                ))));
  }

  @Override
  public void visit(When action) {
    if (action.cond().test(this.context)) {
      action.perform().accept(this);
    } else {
      action.otherwise().accept(this);
    }
  }

  @Override
  public void visit(Attempt action) {
    try {
      action.perform().accept(this);
    } catch (Throwable t) {
      if (action.targetExceptionClass().isAssignableFrom(t.getClass()))
        action.recover().accept(new ActionPerformer(this.context.createChild().assignTo(Context.Impl.ONGOING_EXCEPTION, t)));
      throw ActionException.wrap(t);
    } finally {
      action.ensure().accept(this);
    }
  }

  @Override
  public void visit(Retry action) {
    boolean succeeded = false;
    Throwable lastException = null;
    for (int i = 0; i <= action.times(); i++) {
      try {
        action.perform().accept(this);
        succeeded = true;
      } catch (Throwable t) {
        if (action.targetExceptionClass().isAssignableFrom(t.getClass())) {
          lastException = t;
          InternalUtils.sleep(action.intervalInNanoseconds(), NANOSECONDS);
        } else {
          throw ActionException.wrap(t);
        }
      }
    }
    if (!succeeded)
      throw new ActionException(lastException);
  }

  @Override
  public void visit(TimeOut action) {
    InternalUtils.runWithTimeout(
        () -> {
          action.perform().accept(ActionPerformer.this);
          return true;
        },
        action.durationInNnanos(),
        NANOSECONDS
    );
  }
}
