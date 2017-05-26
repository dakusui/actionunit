package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.core.Action;

import java.util.*;

public class Report implements Iterable<Node<Action>> {
  private final Map<Node<Action>, Record> records = new HashMap<>();
  public final Node<Action> root;

  Report(Node<Action> root) {
    Node.walk(root, this::prepare);
    this.root = root;
  }

  private void prepare(Node<Action> node) {
    this.records.put(node, new Record());
  }

  void started(Node<Action> node) {
    Objects.requireNonNull(
        this.records.get(node),
        ""
    ).started();
  }

  void succeeded(Node<Action> node) {
    Objects.requireNonNull(
        this.records.get(node),
        ""
    ).succeeded();
  }

  public <A extends Action> void error(Node<A> node, Error e) {
    Objects.requireNonNull(
        this.records.get(node),
        ""
    ).failed(e);
  }

  void failed(Node<Action> node, Throwable t) {
    Objects.requireNonNull(
        this.records.get(node),
        ""
    ).failed(t);
  }


  @Override
  public String toString() {
    return this.records.toString();
  }

  @Override
  public Iterator<Node<Action>> iterator() {
    return this.records.keySet().iterator();
  }

  public Record get(Node<Action> actionNode) {
    return this.records.get(actionNode);
  }

  public static class Record implements Iterable<Record.Run> {
    final List<Record.Run> runs = new LinkedList<>();

    void started() {
      runs.add(Record.Run.STARTED);
    }

    void succeeded() {
      runs.add(Record.Run.SUCCEEDED);
    }

    void failed(Throwable t) {
      runs.add(Record.Run.failed(t));
    }

    public <A extends Action> void error(Node<A> node, Error e) {
      runs.add(Record.Run.error(e));
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
      Run STARTED = new Record.Run() {
        @Override
        public boolean wasSuccessful() {
          return false;
        }

        @Override
        public Throwable exception() {
          throw new IllegalStateException();
        }

        public String toString() {
          return "-";
        }
      };

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

      static Run error(Error e) {
        Objects.requireNonNull(e);
        return new Record.Run() {
          @Override
          public boolean wasSuccessful() {
            return false;
          }

          @Override
          public Throwable exception() {
            return e;
          }

          @Override
          public String toString() {
            return "E";
          }
        };
      }
    }
  }
}
