package com.github.dakusui.actionunit.visitors.reporting;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.visitors.ActionScanner;

import java.util.Objects;

public class ActionTreeBuilder extends ActionScanner implements Action.Visitor {
  public static Node<Action> traverse(Action action) {
    ActionTreeBuilder actionTreeBuilder = new ActionTreeBuilder();
    Objects.requireNonNull(action).accept(actionTreeBuilder);
    return actionTreeBuilder.getRootNode();
  }

  private ActionTreeBuilder() {
  }

  @SuppressWarnings("unchecked")
  protected  <A extends Action> void pushNode(Node<A> node) {
    if (!getCurrentPath().isEmpty())
      getCurrentPath().peek().add((Node<Action>) node);
    getCurrentPath().push((Node<Action>) node);
  }
}
