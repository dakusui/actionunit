package com.github.dakusui.actionunit.compat.visitors.reporting;

import com.github.dakusui.actionunit.compat.actions.ForEach;
import com.github.dakusui.actionunit.compat.core.Action;
import com.github.dakusui.actionunit.compat.visitors.ActionScanner;

import java.util.Objects;
import java.util.function.Consumer;

public class ActionTreeBuilder extends ActionScanner implements Action.Visitor {
  public static Node<Action> traverse(Action action) {
    ActionTreeBuilder actionTreeBuilder = new ActionTreeBuilder();
    Objects.requireNonNull(action).accept(actionTreeBuilder);
    return actionTreeBuilder.getRootNode();
  }

  private ActionTreeBuilder() {
  }

  @Override
  protected <T> Consumer<ForEach<T>> forEachActionConsumer() {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unchecked")
  protected  <A extends Action> void pushNode(Node<A> node) {
    if (!getCurrentPath().isEmpty())
      getCurrentPath().peek().add((Node<Action>) node);
    getCurrentPath().push((Node<Action>) node);
  }
}
