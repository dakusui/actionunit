package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.utils.InternalUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public abstract class ActionPerformer implements Action.Visitor {
  public static final String  ONGOING_EXCEPTIONS_TABLE_NAME = "ONGOING_EXCEPTIONS";
  protected           Context context;

  protected ActionPerformer(Context context) {
    this.context = requireNonNull(context);
    Map<Action, Throwable> ongoingExceptions = new ConcurrentHashMap<>();
    this.context.assignTo(ONGOING_EXCEPTIONS_TABLE_NAME, ongoingExceptions);
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
    Action targetAction = action.perform();
    Throwable lastException = null;
    for (int i = 0; i <= action.times(); i++) {
      try {
        callAccept(targetAction, this);
        succeeded = true;
        break;
      } catch (Throwable t) {
        if (action.targetExceptionClass().isAssignableFrom(t.getClass())) {
          lastException = t;
          registerLastExceptionFor(targetAction, lastException);
          InternalUtils.sleep(action.intervalInNanoseconds(), NANOSECONDS);
        } else {
          throw ActionException.wrap(t);
        }
      }
    }
    if (succeeded) {
      unregisterLastExceptionFor(targetAction);
    } else {
      throw ActionException.wrap(lastException);
    }
  }

  public void visit(TimeOut action) {
    InternalUtils.runWithTimeout(
        () -> {
          callAccept(action.perform(), ActionPerformer.this);
          return true;
        },
        () -> String.format("%s", action),
        () -> formatOngoingExceptions(action.perform()),
        action.durationInNanos(),
        NANOSECONDS
    );
  }

  protected abstract Action.Visitor newInstance(Context context);

  protected abstract void callAccept(Action action, Action.Visitor visitor);

  private void registerLastExceptionFor(Action action, Throwable e) {
    ongoingExceptionsTable().put(action, e);
  }

  private void unregisterLastExceptionFor(Action action) {
    ongoingExceptionsTable().remove(action);
  }

  private Map<Action, Throwable> ongoingExceptionsTable() {
    return this.context.valueOf(ONGOING_EXCEPTIONS_TABLE_NAME);
  }

  private String formatOngoingExceptions(Action action) {
    StringBuilder b = new StringBuilder();
    for (Action ongoingAction : ongoingExceptionsTable().keySet()) {
      b.append(String.format("%n%s%n----%n", ongoingAction));
      Throwable e = ongoingExceptionsTable().get(ongoingAction);
      b.append(e.getMessage());
      b.append(String.format("%n"));
      for (StackTraceElement element : e.getStackTrace()) {
        b.append("\t");
        b.append(element);
        b.append(String.format("%n"));
      }
    }
    return b.toString();
  }
}
