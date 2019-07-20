package com.github.dakusui.actionunit.core;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.exceptions.ActionException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Formattable;

import static com.github.dakusui.actionunit.utils.InternalUtils.serialize;

public interface Action<A extends Action> extends Formattable, Serializable {
  void accept(Visitor visitor);

  @SuppressWarnings("unchecked")
  default A cloneAction() {
    try {
      return (A) new ObjectInputStream(serialize(this)).readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw ActionException.wrap(e);
    }
  }

  abstract class Builder<A extends Action> {
    public abstract A build();

    /**
     * A synonym of {@code build()} method. This method is defined not to use a
     * method name 'build', which introduces extra word not relating to what is
     * being achieved.
     * <p>
     * If you prefer a word 'build' in your code, simply use {@code build}
     * method.
     *
     * @return An object built by {@code build} method.
     */
    final public A $() {
      return build();
    }
  }

  interface Visitor {
    @SuppressWarnings("unused")
    default void visit(Action action) {
      throw new UnsupportedOperationException();
    }

    default void visit(Leaf action) {
      this.visit((Action) action);
    }

    default void visit(Named action) {
      this.visit((Action) action);
    }

    default void visit(Composite action) {
      this.visit((Action) action);
    }

    default <E> void visit(ForEach<E> action) {
      this.visit((Action) action);
    }

    default void visit(When action) {
      this.visit((Action) action);
    }

    default void visit(Attempt action) {
      this.visit((Action) action);
    }

    default void visit(Retry action) {
      this.visit((Action) action);
    }

    default void visit(TimeOut action) {
      this.visit((Action) action);
    }
  }
}