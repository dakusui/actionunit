package com.github.dakusui.actionunit.visitors.reporting;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.InternalUtils;

import java.util.*;

public class Report implements Iterable<Node<Action>> {
  private final Map<Node<Action>, Record> records = Collections.synchronizedMap(new HashMap<>());
  final Node<Action> root;

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
  public String toString() {
    return this.records.toString();
  }

  @Override
  public Iterator<Node<Action>> iterator() {
    return this.records.keySet().iterator();
  }

  public static class Record implements Iterable<Record.Run> {
    @FunctionalInterface
    public interface Formatter {
      Formatter DEFAULT_INSTANCE = new Formatter() {
        @Override
        public String format(Node<Action> actionNode, Record record, int indentLevel) {
          return String.format(
              "%s[%s]%s",
              InternalUtils.spaces(indentLevel * 2),
              formatRecord(record).replaceAll("o{4,}", "o..."),
              actionNode.getContent()
          );
        }

        private String formatRecord(Report.Record runs) {
          StringBuilder b = new StringBuilder();
          runs.forEach(run -> {
            b.append(run.toString());
          });
          return b.toString();
        }
      };

      String format(Node<Action> actionNode, Record record, int indentLevel);
    }

    final List<Record.Run> runs = new LinkedList<>();

    void succeeded() {
      runs.add(Record.Run.SUCCEEDED);
    }

    void failed(Throwable t) {
      runs.add(Record.Run.failed(t));
    }

    @Override
    public Iterator<Record.Run> iterator() {
      return runs.iterator();
    }

    @Override
    public String toString() {
      return runs.toString();
    }

    public interface Run {
      boolean wasSuccessful();

      Throwable exception();

      Record.Run SUCCEEDED = new Record.Run() {
        @Override
        public boolean wasSuccessful() {
          return true;
        }

        @Override
        public Throwable exception() {
          throw new IllegalStateException();
        }

        public String toString() {
          return "o";
        }
      };

      static Record.Run failed(Throwable t) {
        Objects.requireNonNull(t);
        return new Record.Run() {
          @Override
          public boolean wasSuccessful() {
            return false;
          }

          @Override
          public Throwable exception() {
            return t;
          }

          @Override
          public String toString() {
            return "x";
          }
        };
      }
    }
  }
}
