package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;

import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

public interface Composite extends Action {
  enum Type {
    SEQUENTIAL,
    PARALLEL,
    ALT;
  }

  Optional<Action> base();

  List<Action> children();

  default boolean isParallel() {
    return type() == Type.PARALLEL;
  }

  Type type();

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format(
        "do %s",
        isParallel()
            ? "parallelly"
            : "sequentially"
    );
  }

  class Builder {
    private       Type         type = Type.SEQUENTIAL;
    private final List<Action> actions;

    public Builder(List<Action> actions) {
      this.actions = actions;
      this.sequential();
    }

    public Builder parallel() {
      this.type = Type.PARALLEL;
      return this;
    }

    public Builder sequential() {
      this.type = Type.SEQUENTIAL;
      return this;
    }

    public Composite build() {
      return build(null);
    }

    public Composite build(Action baseAction) {
      return new Impl(baseAction, actions, baseAction != null ? Type.ALT : type);
    }
  }

  class Impl implements Composite {
    private final List<Action> actions;
    private final Type         type;
    private final Action       baseAction;

    protected Impl(Action baseAction, List<Action> actions, Type type) {
      assert actions != null;
      assert type != null;
      assert (baseAction == null && type == Type.ALT)
          || (baseAction != null && type != Type.ALT);
      this.baseAction = baseAction;
      this.actions = requireNonNull(actions);
      this.type = type;
    }

    @Override
    public Optional<Action> base() {
      return Optional.ofNullable(baseAction);
    }

    @Override
    public List<Action> children() {
      return unmodifiableList(actions);
    }

    @Override
    public Type type() {
      return this.type;
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }
  }
}
