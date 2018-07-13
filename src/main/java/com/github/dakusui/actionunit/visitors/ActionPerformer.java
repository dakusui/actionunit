package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.helpers.InternalUtils;
import com.github.dakusui.actionunit.visitors.reporting.Node;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.github.dakusui.actionunit.helpers.InternalUtils.runWithTimeout;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class ActionPerformer extends ActionWalker {
  public ActionPerformer() {
  }

  @Override
  protected Consumer<Leaf> leafActionConsumer() {
    return Leaf::perform;
  }

  @Override
  protected Consumer<Concurrent> concurrentActionConsumer() {
    return (Concurrent concurrent) -> {
      Deque<Node<Action>> pathSnapshot = snapshotCurrentPath();
      StreamSupport.stream(concurrent.spliterator(), false)
          .map(this::toRunnable)
          .map((Runnable runnable) -> (Runnable) () -> {
            branchPath(pathSnapshot);
            runnable.run();
          })
          .collect(Collectors.toList())
          .parallelStream()
          .forEach(Runnable::run);
    };
  }

  @Override
  protected <T> Consumer<ForEach<T>> forEachActionConsumer() {
    return (ForEach<T> forEach) -> {
      Deque<Node<Action>> pathSnapshot = snapshotCurrentPath();
      (forEach.getMode() == ForEach.Mode.CONCURRENTLY ?
          forEach.data().parallel() :
          forEach.data()).map(ValueHolder::<T>of)
          .map(forEach::<T>createHandler)
          .forEach((Action eachChild) -> {
            branchPath(pathSnapshot);
            eachChild.accept(ActionPerformer.this);
          });
    };
  }

  @Override
  protected <T> Consumer<While<T>> whileActionConsumer() {
    return (While<T> while$) -> {
      Supplier<T> value = while$.value();
      //noinspection unchecked
      while (while$.check().test(value.get())) {
        while$.createAction().accept(ActionPerformer.this);
      }
    };
  }

  @Override
  protected <T> Consumer<When<T>> whenActionConsumer() {
    return (When<T> when) -> {
      Supplier<T> value = when.value();
      //noinspection unchecked
      if (when.check().test(value.get())) {
        when.perform().accept(ActionPerformer.this);
      } else {
        when.otherwise().accept(ActionPerformer.this);
      }
    };
  }

  @Override
  protected <T extends Throwable> Consumer<Attempt<T>> attemptActionConsumer() {
    return (Attempt<T> attempt) -> {
      try {
        attempt.attempt().accept(this);
      } catch (Throwable e) {
        if (!attempt.exceptionClass().isAssignableFrom(e.getClass())) {
          throw ActionException.wrap(e);
        }
        //noinspection unchecked
        attempt.recover(ValueHolder.of((T) e)).accept(this);
      } finally {
        attempt.ensure().accept(this);
      }
    };
  }

  @Override
  protected Consumer<Retry> retryActionConsumer() {
    return (Retry retry) -> {
      try {
        toRunnable(retry.action).run();
      } catch (Throwable e) {
        Throwable lastException = e;
        for (int i = 0; i < retry.times || retry.times == Retry.INFINITE; i++) {
          if (retry.getTargetExceptionClass().isAssignableFrom(lastException.getClass())) {
            retry.getHandler().accept(lastException);
            InternalUtils.sleep(retry.intervalInNanos, NANOSECONDS);
            try {
              toRunnable(retry.action).run();
              return;
            } catch (Throwable t) {
              lastException = t;
            }
          } else {
            throw ActionException.wrap(lastException);
          }
        }
        throw ActionException.wrap(lastException);
      }
    };
  }

  @Override
  protected Consumer<TimeOut> timeOutActionConsumer() {
    return (TimeOut timeOut) -> {
      Deque<Node<Action>> snapshotPath = snapshotCurrentPath();
      runWithTimeout((Callable<Object>) () -> {
            branchPath(snapshotPath);
            timeOut.action.accept(ActionPerformer.this);
            return true;
          },
          timeOut.durationInNanos,
          NANOSECONDS
      );
    };
  }

  private Runnable toRunnable(final Action action) {
    return () -> action.accept(ActionPerformer.this);
  }

  private Deque<Node<Action>> snapshotCurrentPath() {
    return new LinkedList<>(this.getCurrentPath());
  }
}

