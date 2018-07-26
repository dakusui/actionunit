package com.github.dakusui.actionunit.n;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

public interface Composite extends Action {
  List<Action> children();

  boolean isParallel();

  class Builder {
    private       boolean      parallel;
    private final List<Action> actions;

    public Builder(List<Action> actions) {
      this.actions = actions;
      this.sequential();
    }

    public Builder parallel() {
      this.parallel = true;
      return this;
    }

    public Builder sequential() {
      this.parallel = false;
      return this;
    }

    @SuppressWarnings("unchecked")
    public Composite build() {
      return new Base(actions, parallel);
    }
  }

  static void main(String... args) {
    Action action = Actions.sequential(
        Leaf.of(() -> System.out.println("hello")),
        Leaf.of(() -> System.out.println("world"))
    );

    action.accept(new Visitor.Performer());
  }

  final class Base implements Composite {
    private final List<Action> actions;
    private final boolean      parallel;

    protected Base(List<Action> actions, boolean parallel) {
      this.actions = requireNonNull(actions);
      this.parallel = parallel;
    }

    @Override
    public List<Action> children() {
      return Collections.unmodifiableList(actions);
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    @Override
    public boolean isParallel() {
      return this.parallel;
    }
  }
}
