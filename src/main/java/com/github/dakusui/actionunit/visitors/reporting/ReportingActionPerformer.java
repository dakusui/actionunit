package com.github.dakusui.actionunit.visitors.reporting;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.InternalUtils;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ActionPerformer;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class ReportingActionPerformer extends ActionPerformer {
  private final Writer                  writer;
  private final Report.Record.Formatter formatter;
  private final Report                  report;
  private final Identifier              identifier;

  public static class Builder {
    private final Action action;
    private Report.Record.Formatter formatter  = Report.Record.Formatter.DEFAULT_INSTANCE;
    private Writer                  writer     = Writer.Std.OUT;
    /**
     * A bi-predicate to check if given 2 nodes are identical or not.
     */
    private Identifier              identifier = Identifier.BY_ID;

    public Builder(Action action) {
      this.action = Objects.requireNonNull(action);
    }

    public Builder with(Report.Record.Formatter formatter) {
      this.formatter = Objects.requireNonNull(formatter);
      return this;
    }

    public Builder with(Identifier identifier) {
      this.identifier = Objects.requireNonNull(identifier);
      return this;
    }

    public Builder to(Writer writer) {
      this.writer = Objects.requireNonNull(writer);
      return this;
    }

    public ReportingActionPerformer build() {
      return new ReportingActionPerformer(ActionTreeBuilder.traverse(action), identifier, writer, formatter);
    }
  }

  private ReportingActionPerformer(Node<Action> tree, Identifier identifier, Writer writer, Report.Record.Formatter formatter) {
    super();
    this.report = new Report(tree);
    this.identifier = Objects.requireNonNull(identifier);
    this.writer = Objects.requireNonNull(writer);
    this.formatter = Objects.requireNonNull(formatter);
  }

  public void performAndReport() {
    try {
      perform();
    } finally {
      report();
    }
  }

  public void perform() {
    this.report.root.getContent().accept(this);
  }

  public void report() {
    Node.walk(
        this.report.root,
        (actionNode, nodes) -> writer.writeLine(
            formatter.format(actionNode, this.report.get(actionNode), nodes.size())
        ));
  }

  public static ReportingActionPerformer create(Action action) {
    return create(action, Writer.Std.OUT);
  }

  public static ReportingActionPerformer create(Action action, Writer writer) {
    return new Builder(
        action
    ).with(
        Report.Record.Formatter.DEFAULT_INSTANCE
    ).with(
        Identifier.BY_ID
    ).to(
        Objects.requireNonNull(writer)
    ).build();
  }

  @SuppressWarnings("unchecked")
  @Override
  protected <A extends Action> void succeeded(Node<A> node) {
    this.report.succeeded((Node<Action>) node);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected <A extends Action> void failed(Node<A> node, Throwable e) {
    this.report.failed((Node<Action>) node, e);
  }

  @Override
  protected <A extends Action> Node<A> toNode(Node<A> parent, A action) {
    if (parent == null) {
      //noinspection unchecked
      return (Node<A>) this.report.root;
    }
    //noinspection unchecked
    return parent.children().stream(
    ).filter(
        (Node<A> n) -> this.identifier.identifier().test(n, super.toNode(parent, action))
    ).collect(
        InternalUtils.singletonCollector(
            () -> new IllegalStateException(
                composeErrorMessageOnDuplicatedNodes(parent, action)))
    ).orElseThrow(
        () -> new IllegalStateException(
            composeMessageOnMissingNode(parent, action))
    );
  }

  private <A extends Action> String composeMessageOnMissingNode(Node<A> parent, A action) {

    return this.identifier.composeMessageOnMissingNode(parent, action);
  }

  private <A extends Action> String composeErrorMessageOnDuplicatedNodes(Node<A> parent, A action) {
    return this.identifier.composeMessageOnDuplicatedNodes(parent, action);
  }

  public enum Identifier {
    BY_NAME {
      @Override
      BiPredicate<Node<? extends Action>, Node<? extends Action>> identifier() {
        return (a, b) ->
            Objects.equals(a, b)
                || Objects.equals(a.getContent(), b.getContent())
                || Objects.equals(InternalUtils.describe(a.getContent()), InternalUtils.describe(b.getContent()));
      }

      @Override
      <A extends Action> String composeMessageOnMissingNode(Node<A> parent, A action) {
        return format(
            "Node matching '%s' was not found under '%s'(%s)",
            InternalUtils.describe(action),
            parent,
            childrenToString(parent)
        );
      }

      @Override
      public <A extends Action> String composeMessageOnDuplicatedNodes(Node<A> parent, A action) {
        return format(
            "More than one node matching '%s' were found under '%s'(%s). Consider using 'named' action for them.",
            InternalUtils.describe(action),
            parent,
            childrenToString(parent)
        );
      }
    },
    BY_ID {
      @Override
      BiPredicate<Node<? extends Action>, Node<? extends Action>> identifier() {
        return (a, b) ->
            Objects.equals(a, b)
                || Objects.equals(a.getContent().id(), b.getContent().id());
      }

      @Override
      <A extends Action> String composeMessageOnMissingNode(Node<A> parent, A action) {
        return format(
            "Node matching '%d(%s)' was not found under '%s(%s)'(%s)",
            action.id(),
            InternalUtils.describe(action),
            parent.getContent().id(),
            InternalUtils.describe(parent.getContent()),
            childrenToString(parent)
        );
      }

      @Override
      public <A extends Action> String composeMessageOnDuplicatedNodes(Node<A> parent, A action) {
        return format(
            "More than one node whose id is '%d(%s)' were found under '%s(%s)'(%s). Examine they are created in an appropriate context.",
            action.id(),
            InternalUtils.describe(action),
            parent.getContent().id(),
            InternalUtils.describe(parent.getContent()),
            childrenToString(parent)
        );
      }
    };

    abstract BiPredicate<Node<? extends Action>, Node<? extends Action>> identifier();

    abstract <A extends Action> String composeMessageOnMissingNode(Node<A> parent, A action);

    String childrenToString(Node<? extends Action> parent) {
      return String.join(
          ",",
          parent.children().stream()
              .map(
                  n -> String.format(
                      "%d-%s",
                      n.getContent().id(),
                      n.getContent().toString()
                  )
              ).collect(Collectors.toList())
      );
    }

    public abstract <A extends Action> String composeMessageOnDuplicatedNodes(Node<A> parent, A action);
  }
}
