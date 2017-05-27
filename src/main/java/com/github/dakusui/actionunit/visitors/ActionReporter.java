package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.AutocloseableIterator;
import com.github.dakusui.actionunit.helpers.Utils;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.StreamSupport.stream;

public class ActionReporter extends ActionWalker implements Action.Visitor {
  private final ActionPrinter.Writer    writer;
  private final Report.Record.Formatter formatter;
  private final Report                  report;

  public static class Builder {
    private final Action action;
    private Report.Record.Formatter formatter = Report.Record.Formatter.DEFAULT_INSTANCE;
    private ActionPrinter.Writer    writer    = ActionPrinter.Writer.Std.OUT;


    public Builder(Action action) {
      this.action = Objects.requireNonNull(action);
    }

    public Builder with(Report.Record.Formatter formatter) {
      this.formatter = Objects.requireNonNull(formatter);
      return this;
    }

    public Builder to(ActionPrinter.Writer writer) {
      this.writer = Objects.requireNonNull(writer);
      return this;
    }

    public ActionReporter build() {
      return new ActionReporter(TreeBuilder.traverse(action), writer, formatter);
    }
  }

  private ActionReporter(Node<Action> tree, ActionPrinter.Writer writer, Report.Record.Formatter formatter) {
    this.report = new Report(tree);
    this.writer = writer;
    this.formatter = formatter;
  }

  public void perform() {
    Objects.requireNonNull(formatter);
    try {
      this.report.root.getContent().accept(this);
    } finally {
      Node.walk(
          this.report.root,
          (actionNode, nodes) -> writer.writeLine(
              formatter.format(actionNode, this.report.get(actionNode), nodes.size())
          ));
    }
  }

  @Override
  public void visit(Action action) {
    throw new UnsupportedOperationException(Utils.describe(action));
  }

  @Override
  public void visit(Leaf action) {
    handle(
        action,
        leaf -> action.perform()
    );
  }

  @Override
  public void visit(Named action) {
    handle(
        action,
        (Named a) -> {
          a.getAction().accept(this);
        }
    );
  }

  @Override
  public void visit(Composite action) {
    handle(
        action,
        (Composite a) -> {
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
        (ForEach<T> forEach) -> stream(forEach.data().spliterator(), false)
            .map((T item) -> (Supplier<T>) () -> item)
            .map(forEach::createHandler)
            .forEach((Action eachChild) -> {
              eachChild.accept(ActionReporter.this);
            })
    );
  }

  @Override
  public <T extends Throwable> void visit(Attempt<T> action) {
    handle(
        action,
        (Action a) -> {
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
  <A extends Action> Node<A> toNode(Node<Action> parent, A action) {
    if (parent == null) {
      //noinspection unchecked
      return (Node<A>) this.report.root;
    }
    //noinspection unchecked
    return (Node<A>) parent.children().stream(
    ).filter(
        (Node<Action> n) -> checker().test(n, super.toNode(parent, action))
    ).collect(
        Utils.singletonCollector(
            () -> new IllegalStateException(
                format(
                    "More than one node matching '%s' were found under '%s'(%s)",
                    Utils.describe(action),
                    parent,
                    childrenToString(parent)
                )))
    ).orElseThrow(
        () -> new IllegalStateException(
            format(
                "Node matching '%s' was not found under '%s'(%s)",
                Utils.describe(action),
                parent,
                childrenToString(parent)
            ))
    );
  }

  private String childrenToString(Node<?> parent) {
    return String.join(
        ",",
        parent.children().stream()
            .map(n -> n.getContent().toString())
            .collect(Collectors.toList())
    );
  }

  private <A extends Node<?>> BiPredicate<A, A> checker() {
    return (a, b) ->
        Objects.equals(a, b)
            || Objects.equals(a.getContent(), b.getContent())
            || Objects.equals(Utils.describe(a.getContent()), Utils.describe(b.getContent()));
  }

  <A extends Action> void notfinished(Node<A> node) {
    this.report.notfinished((Node<Action>) node);
  }

  @SuppressWarnings("unchecked")
  <A extends Action> void succeeded(Node<A> node) {
    this.report.succeeded((Node<Action>) node);
  }

  @SuppressWarnings("unchecked")
  <A extends Action> void failed(Node<A> node, Throwable e) {
    this.report.failed((Node<Action>) node, e);
  }
}
