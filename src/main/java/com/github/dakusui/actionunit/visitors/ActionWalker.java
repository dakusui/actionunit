package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.Checks;
import com.github.dakusui.actionunit.visitors.reporting.Node;

import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Consumer;

abstract class ActionWalker implements Action.Visitor {
  private final ThreadLocal<Deque<Node<Action>>> _current;
  private       Node<Action>                     root;

  ActionWalker() {
    this._current = new ThreadLocal<>();
    this._current.set(new LinkedList<>());
    this.root = null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(Leaf action) {
    handle(
        action,
        leafActionConsumer()
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(Named action) {
    handle(
        action,
        namedActionConsumer()
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(Sequential action) {
    handle(
        action,
        sequentialActionConsumer()
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(Concurrent action) {
    handle(
        action,
        concurrentActionConsumer()
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> void visit(ForEach<T> action) {
    handle(
        action,
        forEachActionConsumer()
    );
  }

  /**
   * {@inheritDoc}
   */
  public <T> void visit(While<T> action) {
    handle(
        action,
        whileActionConsumer()
    );
  }

  /**
   * {@inheritDoc}
   */
  public <T> void visit(When<T> action) {
    handle(
        action,
        whenActionConsumer()
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends Throwable> void visit(Attempt<T> action) {
    handle(
        action,
        attemptActionConsumer()
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(TestAction action) {
    handle(
        action,
        testActionConsumer()
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(Retry action) {
    handle(
        action,
        retryActionConsumer()
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(TimeOut action) {
    handle(
        action,
        timeOutActionConsumer()
    );
  }

  protected abstract Consumer<Leaf> leafActionConsumer();

  protected Consumer<Named> namedActionConsumer() {
    return (Named named) -> named.getAction().accept(this);
  }

  protected Consumer<Sequential> sequentialActionConsumer() {
    return (Sequential sequential) -> {
      for (Action each : sequential) {
        each.accept(this);
      }
    };
  }

  protected abstract Consumer<Concurrent> concurrentActionConsumer();

  protected abstract <T> Consumer<ForEach<T>> forEachActionConsumer();

  protected abstract <T> Consumer<While<T>> whileActionConsumer();

  protected abstract <T> Consumer<When<T>> whenActionConsumer();

  protected abstract <T extends Throwable> Consumer<Attempt<T>> attemptActionConsumer();

  protected Consumer<TestAction> testActionConsumer() {
    return (TestAction test) -> {
      test.given().accept(this);
      test.when().accept(this);
      test.then().accept(this);
    };
  }

  protected abstract Consumer<Retry> retryActionConsumer();

  protected abstract Consumer<TimeOut> timeOutActionConsumer();

  protected final <A extends Action> void handle(A action, Consumer<A> handler) {
    @SuppressWarnings("unchecked") Node<A> node = toNode(
        this.getCurrentNode(),
        action
    );
    before(node);
    try {
      handler.accept(action);
      succeeded(node);
    } catch (Error | RuntimeException e) {
      failed(node, e);
      throw e;
    } finally {
      after(node);
    }
  }

  protected <A extends Action> Node<A> toNode(Node<A> parent, A action) {
    return new Node<>(action, action instanceof Leaf);
  }

  @SuppressWarnings({ "unchecked", "WeakerAccess" })
  protected <A extends Action> void before(Node<A> node) {
    if (getCurrentPath().isEmpty()) {
      pushNode(node);
      root = (Node<Action>) node;
    } else {
      pushNode(node);
    }
  }

  protected <A extends Action> void succeeded(Node<A> node) {
  }

  protected <A extends Action> void failed(Node<A> node, Throwable e) {
  }

  @SuppressWarnings("WeakerAccess")
  protected <A extends Action> void after(Node<A> node) {
    Checks.checkState(
        this.getCurrentPath().peek() == node,
        "Cannot remove %s from queue=%s", node, this.getCurrentPath()
    );
    popNode();
  }

  @SuppressWarnings("unchecked")
  protected <A extends Action> void pushNode(Node<A> node) {
    this.getCurrentPath().push((Node<Action>) node);
  }

  @SuppressWarnings("unchecked")
  protected <A extends Action> Node<A> popNode() {
    return (Node<A>) this.getCurrentPath().pop();
  }

  protected <A extends Action> Node<A> getCurrentNode() {
    return (Node<A>) this.getCurrentPath().peek();
  }

  protected Node<Action> getRootNode() {
    return root;
  }

  protected synchronized Deque<Node<Action>> getCurrentPath() {
    return _current.get();
  }

  protected void branchPath(Deque<Node<Action>> pathSnapshot) {
    this._current.set(new LinkedList<>(pathSnapshot));
  }
}
