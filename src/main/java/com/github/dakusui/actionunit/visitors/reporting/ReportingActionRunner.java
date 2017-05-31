package com.github.dakusui.actionunit.visitors.reporting;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.Checks;
import com.github.dakusui.actionunit.helpers.Utils;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ActionPerformer;
import com.github.dakusui.actionunit.visitors.WrappedException;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class ReportingActionRunner extends ActionPerformer {
  private final Writer                  writer;
  private final Report.Record.Formatter formatter;
  private final Report                  report;

  public static class Builder {
    private final Action action;
    private Report.Record.Formatter formatter = Report.Record.Formatter.DEFAULT_INSTANCE;
    private Writer    writer    = Writer.Std.OUT;


    public Builder(Action action) {
      this.action = Objects.requireNonNull(action);
    }

    public Builder with(Report.Record.Formatter formatter) {
      this.formatter = Objects.requireNonNull(formatter);
      return this;
    }

    public Builder to(Writer writer) {
      this.writer = Objects.requireNonNull(writer);
      return this;
    }

    public ReportingActionRunner build() {
      return new ReportingActionRunner(ActionTreeBuilder.traverse(action), writer, formatter);
    }
  }

  private ReportingActionRunner(Node<Action> tree, Writer writer, Report.Record.Formatter formatter) {
    this.report = new Report(tree);
    this.writer = writer;
    this.formatter = formatter;
  }

  public void perform() {
    Objects.requireNonNull(formatter);
    try {
      this.report.root.getContent().accept(this);
    } catch (WrappedException e) {
      throw Checks.propagate(e.getCause());
    } finally {
      Node.walk(
          this.report.root,
          (actionNode, nodes) -> writer.writeLine(
              formatter.format(actionNode, this.report.get(actionNode), nodes.size())
          ));
    }
  }

  @Override
  public <A extends Action> Node<A> toNode(Node<Action> parent, A action) {
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
                    "More than one node matching '%s' were found under '%s'(%s). Consider using 'named' action for them.",
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

  @SuppressWarnings("unchecked")
  @Override
  public <A extends Action> void succeeded(Node<A> node) {
    this.report.succeeded((Node<Action>) node);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <A extends Action> void failed(Node<A> node, Throwable e) {
    this.report.failed((Node<Action>) node, e);
  }

}
