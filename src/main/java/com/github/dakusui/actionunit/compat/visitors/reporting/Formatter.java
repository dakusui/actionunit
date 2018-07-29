package com.github.dakusui.actionunit.compat.visitors.reporting;

import com.github.dakusui.actionunit.compat.core.Action;
import com.github.dakusui.actionunit.n.utils.InternalUtils;
import com.github.dakusui.actionunit.n.visitors.Record;

@FunctionalInterface
public interface Formatter {
  Formatter DEFAULT_INSTANCE = new Formatter() {
    @Override
    public String format(Node<Action> actionNode, Record record, int indentLevel) {
      return String.format(
          "%s[%s]%s",
          InternalUtils.spaces(indentLevel * 2),
          formatRecord(record).replaceAll(".{4,}", "... "),
          actionNode.getContent()
      );
    }

    private String formatRecord(Record runs) {
      StringBuilder b = new StringBuilder();
      runs.forEach(run -> b.append(run.toString()));
      return b.toString();
    }
  };

  Formatter DEBUG_INSTANCE = new Formatter() {
    @Override
    public String format(Node<Action> actionNode, Record record, int indentLevel) {
      return String.format(
          "%s[%s]%d-%s",
          InternalUtils.spaces(indentLevel * 2),
          formatRecord(record).replaceAll("o{4,}", "o..."),
          actionNode.getContent().id(),
          actionNode.getContent()
      );
    }

    private String formatRecord(Record runs) {
      StringBuilder b = new StringBuilder();
      runs.forEach(run -> b.append(run.toString()));
      return b.toString();
    }
  };

  String format(Node<Action> actionNode, Record record, int indentLevel);
}
