package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.helpers.Utils;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.github.dakusui.actionunit.helpers.Utils.runWithTimeout;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.stream.StreamSupport.stream;

public abstract class ActionPerformer extends ActionWalker {
  @Override
  Consumer<Leaf> leafActionConsumer() {
    return Leaf::perform;
  }

  @Override
  Consumer<Concurrent> concurrentActionConsumer() {
    return (Concurrent concurrent) -> {
      Deque<Node<Action>> pathSnapshot = snapshotCurrentPath();
      stream(concurrent.spliterator(), false)
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
  <T> Consumer<ForEach<T>> forEachActionConsumer() {
    return (ForEach<T> forEach) -> stream(forEach.data().spliterator(), forEach.getMode() == ForEach.Mode.CONCURRENTLY)
        .map((T item) -> (Supplier<T>) () -> item)
        .map(forEach::createHandler)
        .forEach((Action eachChild) -> {
          eachChild.accept(ActionPerformer.this);
        });
  }

  @Override
  <T> Consumer<While<T>> whileActionConsumer() {
    return (While<T> while$) -> {
      Supplier<T> value = while$.value();
      //noinspection unchecked
      while (while$.check().test(value.get())) {
        while$.createHandler(value).accept(ActionPerformer.this);
      }
    };
  }

  @Override
  <T> Consumer<When<T>> whenActionConsumer() {
    return (When<T> when) -> {
      Supplier<T> value = when.value();
      //noinspection unchecked
      if (when.check().test(value.get())) {
        when.perform(value).accept(ActionPerformer.this);
      } else {
        when.otherwise(value).accept(ActionPerformer.this);
      }
    };
  }

  @Override
  <T extends Throwable> Consumer<Attempt<T>> attemptActionConsumer() {
    return (Attempt<T> attempt) -> {
      try {
        attempt.attempt().accept(this);
      } catch (Throwable e) {
        if (!attempt.exceptionClass().isAssignableFrom(e.getClass())) {
          throw new Wrapped(e);
        }
        //noinspection unchecked
        attempt.recover(() -> (T) e).accept(this);
      } finally {
        attempt.ensure().accept(this);
      }
    };
  }

  @Override
  Consumer<Retry> retryActionConsumer() {
    return (Retry retry) -> {
      try {
        toRunnable(retry.action).run();
      } catch (Throwable e) {
        Throwable lastException = e;
        for (int i = 0; i < retry.times || retry.times == Retry.INFINITE; i++) {
          if (retry.getTargetExceptionClass().isAssignableFrom(lastException.getClass())) {
            Utils.sleep(retry.intervalInNanos, NANOSECONDS);
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
  Consumer<TimeOut> timeOutActionConsumer() {
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

  static class Wrapped extends RuntimeException {
    private Wrapped(Throwable t) {
      super(t);
    }
  }

  private void branchPath(Deque<Node<Action>> pathSnapshot) {
    ActionPerformer.this._current.set(new LinkedList<>(pathSnapshot));
  }

  private Deque<Node<Action>> snapshotCurrentPath() {
    return new LinkedList<>(this.getCurrentPath());
  }


  /**
   * A simple implementation of an {@link ActionPerformer}.
   */
  public static class Impl extends ActionPerformer {
  }
}

