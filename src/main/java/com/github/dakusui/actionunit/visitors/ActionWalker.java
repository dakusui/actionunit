package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.AutocloseableIterator;
import com.github.dakusui.actionunit.helpers.Checks;

import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Consumer;

abstract class ActionWalker implements Action.Visitor {
  protected final ThreadLocal<Deque<Node<Action>>> _current;
  Node<Action> root;

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

  abstract Consumer<Leaf> leafActionConsumer();

  Consumer<Named> namedActionConsumer() {
    return (Named named) -> named.getAction().accept(this);
  }

  Consumer<Sequential> sequentialActionConsumer() {
    return (Sequential sequential) -> {
      try (AutocloseableIterator<Action> i = sequential.iterator()) {
        while (i.hasNext()) {
          i.next().accept(this);
        }
      }
    };
  }

  abstract Consumer<Concurrent> concurrentActionConsumer();

  abstract <T> Consumer<ForEach<T>> forEachActionConsumer();

  <A extends Action> void handle(A action, Consumer<A> handler) {
    @SuppressWarnings("unchecked") Node<A> node =
        toNode(
            this.getCurrentPath().peek(),
            action
        );
    before(node);
    try {
      handler.accept(action);
      succeeded(node);
    } catch (ReportingActionRunner.Wrapped e) {
      notFinished(node);
      throw e;
    } catch (Error | RuntimeException e) {
      failed(node, e);
      throw e;
    } finally {
      after(node);
    }
  }

  <A extends Action> void notFinished(Node<A> node) {
  }

  <A extends Action> void succeeded(Node<A> node) {
  }

  <A extends Action> void failed(Node<A> node, Throwable e) {
  }

  @SuppressWarnings({ "unchecked", "WeakerAccess" })
  <A extends Action> void before(Node<A> node) {
    if (getCurrentPath().isEmpty()) {
      pushNode(node);
      root = (Node<Action>) node;
    } else {
      pushNode(node);
    }
  }

  @SuppressWarnings("unchecked")
  <A extends Action> void pushNode(Node<A> node) {
    this.getCurrentPath().push((Node<Action>) node);
  }

  @SuppressWarnings("WeakerAccess")
  <A extends Action> void after(Node<A> node) {
    Checks.checkState(
        this.getCurrentPath().peek() == node,
        "Cannot remove %s from queue=%s", node, this.getCurrentPath()
    );
    this.getCurrentPath().pop();
  }

  <A extends Action> Node<A> toNode(Node<Action> parent, A action) {
    return new Node<>(action, action instanceof Leaf);
  }

  synchronized Deque<Node<Action>> getCurrentPath() {
    return _current.get();
  }
}
