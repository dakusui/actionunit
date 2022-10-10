package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.utils.InternalUtils;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public abstract class ActionPerformer implements Action.Visitor {
  protected Context context;

  protected ActionPerformer(Context context) {
    this.context = requireNonNull(context);
  }

  public void visit(Leaf action) {
    action.runnable(context).run();
  }

  public void visit(Named action) {
    callAccept(action.action(), this);
  }

  public void visit(Composite action) {
    Stream<Action> actionStream = action.isParallel()
        ? action.children().parallelStream()
        : action.children().stream();
    actionStream.forEach(
        a -> callAccept(a, this)
    );
  }

  public <E> void visit(ForEach<E> action) {
    Stream<E> data = requireNonNull(action.data().apply(this.context));
    data = action.isParallel()
        ? data.parallel()
        : data;
    data.forEach(
        e -> callAccept(action.perform(),
            newInstance(
                this.context.createChild().assignTo(
                    action.loopVariableName(),
                    e
                )
            )));
  }

  public void visit(While action) {
    while (action.condition().test(this.context)) {
      callAccept(action.perform(), this);
    }
  }

  public void visit(When action) {
    if (action.cond().test(this.context)) {
      callAccept(action.perform(), this);
    } else {
      callAccept(action.otherwise(), this);
    }
  }

  public void visit(Attempt action) {
    Context originalContext = this.context;
    try {
      callAccept(action.perform(), this);
    } catch (Throwable t) {
      if (action.targetExceptionClass().isAssignableFrom(t.getClass())) {
        this.context = this.context.createChild();
        callAccept(action.recover(), newInstance(
            originalContext.assignTo(Context.Impl.ONGOING_EXCEPTION, t)
        ));
      } else
        throw ActionException.wrap(t);
    } finally {
      callAccept(action.ensure(), this);
      this.context = originalContext;
    }
  }

  public void visit(Retry action) {
    boolean succeeded = false;
    Throwable lastException = null;
    for (int i = 0; i <= action.times(); i++) {
      try {
        callAccept(action.perform(), this);
        succeeded = true;
        break;
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
      throw ActionException.wrap(lastException);
  }

  public void visit(TimeOut action) {
    InternalUtils.runWithTimeout(
        () -> {
          callAccept(action.perform(), ActionPerformer.this);
          return true;
        },
        action.durationInNanos(),
        NANOSECONDS
    );
  }

  protected abstract Action.Visitor newInstance(Context context);

  protected abstract void callAccept(Action action, Action.Visitor visitor);
}
