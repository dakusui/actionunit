package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.Checks;

import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Consumer;

abstract class ActionWalker implements Action.Visitor {
  final Deque<Node<Action>> current;
  Node<Action> root;

  ActionWalker() {
    this.current = new LinkedList<>();
    this.root = null;
  }

  <A extends Action> void handle(A action, Consumer<A> handler) {
    class Wrapped extends RuntimeException {
      Wrapped(Throwable t) {
        super(t);
      }
    }
    @SuppressWarnings("unchecked") Node<A> node = toNode(this.current.peek(), action);
    before(node);
    try {
      handler.accept(action);
      succeeded(node);
    } catch (Error | Wrapped e) {
      notFinished(node);
      throw e;
    } catch (RuntimeException e) {
      failed(node, e);
      throw new Wrapped(e);
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
    if (current.isEmpty()) {
      pushNode(node);
      root = (Node<Action>) node;
    } else {
      pushNode(node);
    }
  }

  @SuppressWarnings("unchecked")
  <A extends Action> void pushNode(Node<A> node) {
    this.current.push((Node<Action>) node);
  }

  @SuppressWarnings("WeakerAccess")
  <A extends Action> void after(Node<A> node) {
    Checks.checkState(
        this.current.peek() == node,
        "Cannot remove %s from queue=%s", node, this.current
    );
    this.current.pop();
  }

  <A extends Action> Node<A> toNode(Node<Action> parent, A action) {
    return new Node<>(action, action instanceof Leaf);
  }
}
