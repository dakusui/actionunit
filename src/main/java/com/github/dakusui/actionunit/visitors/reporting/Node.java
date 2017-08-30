package com.github.dakusui.actionunit.visitors.reporting;

import com.github.dakusui.actionunit.helpers.Checks;
import com.github.dakusui.actionunit.helpers.InternalUtils;
import com.github.dakusui.actionunit.helpers.Utils;
import com.github.dakusui.actionunit.io.Writer;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

public class Node<A> {
  final         List<Node<A>> children;
  private final A             content;
  private final String        description;

  public Node(A content, boolean leaf) {
    this.content = Objects.requireNonNull(content);
    this.description = InternalUtils.describe(content);
    children = leaf
        ? null
        : new LinkedList<>();
  }

  static <A> void walk(Node<A> node, Consumer<Node<A>> consumer) {
    walk(node, (aNode, nodes) -> consumer.accept(aNode));
  }

  public static <A> void walk(Node<A> node, BiConsumer<Node<A>, Deque<Node<A>>> consumer) {
    walk(node, consumer, new LinkedList<>());
  }

  private static <A> void walk(Node<A> node, BiConsumer<Node<A>, Deque<Node<A>>> consumer, Deque<Node<A>> context) {
    consumer.accept(node, context);
    context.push(node);
    try {
      if (!node.isLeaf())
        node.children().forEach(i -> walk(i, consumer, context));
    } finally {
      context.pop();
    }
  }

  public static <A> void print(Node<A> node, Writer writer) {
    walk(node, (aNode, nodes) -> writer.writeLine(Utils.spaces(nodes.size() * 2) + aNode));
  }

  void add(Node<A> node) {
    Checks.checkState(!isLeaf(), "This node is a leaf.");
    this.children.add(Objects.requireNonNull(node));
  }

  List<Node<A>> children() {
    Checks.checkState(!isLeaf(), "This node is a leaf.");
    return Collections.unmodifiableList(this.children);
  }

  boolean isLeaf() {
    return children == null;
  }

  public A getContent() {
    return this.content;
  }

  @Override
  public String toString() {
    return String.format("Node(%s)", this.description);
  }

  public String format() {
    return this.isLeaf()
        ? "<" + this.description + ">"
        : "<" + this.description + ">" + String.format(":%n") + children().stream().map(Node::format).collect(toList()).toString();
  }
}
