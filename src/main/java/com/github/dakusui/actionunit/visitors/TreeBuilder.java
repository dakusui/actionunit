package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.AutocloseableIterator;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TreeBuilder extends ActionWalker implements Action.Visitor {
  public static Node<Action> traverse(Action action) {
    TreeBuilder treeBuilder = new TreeBuilder();
    Objects.requireNonNull(action).accept(treeBuilder);
    return treeBuilder.root;
  }

  private TreeBuilder() {
  }

  Consumer<Leaf> leafActionConsumer() {
    return this::handleAction;
  }

  /**
   * {@inheritDoc}
   */
  public <T> void visit(While<T> action) {
    handle(
        action,
        (While<T> while$) -> {
          Supplier<T> value = () -> {
            throw new UnsupportedOperationException();
          };
          while$.createHandler(value).accept(TreeBuilder.this);
        }
    );
  }

  /**
   * {@inheritDoc}
   */
  public <T> void visit(When<T> action) {
    handle(
        action,
        (When<T> when) -> {
          Supplier<T> value = () -> {
            throw new UnsupportedOperationException();
          };
          //noinspection unchecked
          when.perform(value).accept(TreeBuilder.this);
          when.otherwise(value).accept(TreeBuilder.this);
        }
    );
  }

  @Override
  public <T extends Throwable> void visit(Attempt<T> action) {
    handle(
        action,
        (Action a) -> {
          handleAction(a);
          @SuppressWarnings("unchecked") Attempt<T> attemptAction = (Attempt<T>) a;
          attemptAction.attempt().accept(this);
          attemptAction.recover(() -> {
            throw new UnsupportedOperationException();
          }).accept(this);
          attemptAction.ensure().accept(this);
        }
    );
  }

  @Override
  public void visit(TestAction action) {
    handle(
        action,
        (TestAction test) -> {
          test.given().accept(this);
          test.when().accept(this);
          test.then().accept(this);
        }
    );
  }

  @Override
  public void visit(Retry action) {
    handle(action, (Retry retry) -> {
      retry.action.accept(this);
    });
  }

  @Override
  public void visit(TimeOut action) {
    handle(
        action,
        (TimeOut timeOut) -> {
          timeOut.action.accept(this);
        }
    );
  }

  @Override
  Consumer<Concurrent> concurrentActionConsumer() {
    return (Concurrent a) -> {
      try (AutocloseableIterator<Action> i = a.iterator()) {
        while (i.hasNext()) {
          i.next().accept(this);
        }
      }
    };
  }

  @Override
  <T> Consumer<ForEach<T>> forEachActionConsumer() {
    return (ForEach<T> a) -> a.createHandler(() -> {
      throw new UnsupportedOperationException();
    }).accept(this);
  }

  @SuppressWarnings("unchecked")
  <A extends Action> void pushNode(Node<A> node) {
    if (!getCurrentPath().isEmpty())
      getCurrentPath().peek().add((Node<Action>) node);
    getCurrentPath().push((Node<Action>) node);
  }

  private void handleAction(Action a) {
  }
}
