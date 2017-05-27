package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.AutocloseableIterator;
import com.github.dakusui.actionunit.helpers.Utils;

import java.util.Objects;

public class TreeBuilder extends ActionWalker implements Action.Visitor {
  public static Node<Action> traverse(Action action) {
    TreeBuilder treeBuilder = new TreeBuilder();
    Objects.requireNonNull(action).accept(treeBuilder);
    return treeBuilder.root;
  }

  private TreeBuilder() {
  }

  @Override
  public void visit(Action action) {
    throw new UnsupportedOperationException(Utils.describe(action));
  }

  @Override
  public void visit(Leaf action) {
    handle(
        action,
        this::handleAction
    );
  }

  @Override
  public void visit(Named action) {
    handle(
        action,
        (Named a) -> {
          handleAction(a);
          a.getAction().accept(this);
        }
    );
  }

  @Override
  public void visit(Composite action) {
    handle(
        action,
        (Composite a) -> {
          handleAction(a);
          try (AutocloseableIterator<Action> i = a.iterator()) {
            while (i.hasNext()) {
              i.next().accept(this);
            }
          }
        }
    );
  }

  @Override
  public <T> void visit(ForEach<T> action) {
    handle(
        action,
        (ForEach<T> a) -> a.createHandler(() -> {
          throw new UnsupportedOperationException();
        }).accept(this));
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
    handle(action, this::handleAction);
  }

  @Override
  public void visit(Retry action) {
    handle(action, this::handleAction);
  }

  @Override
  public void visit(TimeOut action) {
    handle(action, this::handleAction);
  }

  @SuppressWarnings("unchecked")
  <A extends Action> void pushNode(Node<A> node) {
    if (!current.isEmpty())
      this.current.peek().add((Node<Action>) node);
    this.current.push((Node<Action>) node);
  }

  private void handleAction(Action a) {
  }
}
