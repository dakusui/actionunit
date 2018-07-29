package com.github.dakusui.actionunit.compat.visitors.reporting;

import com.github.dakusui.actionunit.compat.core.Action;
import com.github.dakusui.actionunit.n.visitors.Record;

import java.util.*;

public class Report implements Iterable<Node<Action>> {
  private final Map<Node<Action>, Record> records = Collections.synchronizedMap(new HashMap<>());
  final         Node<Action>              root;

  Report(Node<Action> root) {
    Node.walk(root, this::prepare);
    this.root = root;
  }

  private void prepare(Node<Action> node) {
    this.records.put(node, new Record());
  }

  void succeeded(Node<Action> node) {
    Objects.requireNonNull(
        this.records.get(node),
        String.format("Unknown node '%s' was requested:", node)
    ).succeeded();
  }

  void failed(Node<Action> node, Throwable t) {
    Objects.requireNonNull(
        this.records.get(node),
        String.format("Unknown node '%s' was requested:", node)
    ).failed(t);
  }

  public Record get(Node<Action> actionNode) {
    return this.records.get(actionNode);
  }

  @Override
  public Iterator<Node<Action>> iterator() {
    return this.records.keySet().iterator();
  }

}
