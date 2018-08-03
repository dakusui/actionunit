package com.github.dakusui.actionunit.core;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.github.dakusui.actionunit.core.Context.Impl.ONGOING_EXCEPTION;
import static java.util.Objects.requireNonNull;

public interface Context {

  Context createChild();

  boolean defined(String variableName);

  <V> V valueOf(String variableName);

  Context assignTo(String variableName, Object value);

  <T extends Throwable> T thrownException();

  default boolean wasExceptionThrown() {
    return defined(ONGOING_EXCEPTION);
  }

  class Impl implements Context {
    public static final String              ONGOING_EXCEPTION = "ONGOING_EXCEPTION";
    private final       Map<String, Object> variables         = new HashMap<>();
    private final       Context             parent;

    private Impl() {
      this(null);
    }

    private Impl(Context parent) {
      this.parent = parent;
    }

    @Override
    public Context createChild() {
      return new Impl(this);
    }

    @Override
    public boolean defined(String variableName) {
      if (this.variables.containsKey(variableName))
        return true;
      if (this.parent == null)
        return false;
      return parent.defined(variableName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> V valueOf(String variableName) {
      if (this.variables.containsKey(requireNonNull(variableName)))
        return (V) this.variables.get(variableName);
      if (parent == null)
        throw new NoSuchElementException(String.format("Variable '%s' is not defined.", variableName));
      return parent.valueOf(variableName);
    }

    @Override
    public Context assignTo(String variableName, Object value) {
      this.variables.put(variableName, value);
      return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Throwable> T thrownException() {
      return (T) this.variables.get(ONGOING_EXCEPTION);
    }
  }

  static Context create() {
    return new Impl();
  }
}
