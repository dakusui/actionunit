package com.github.dakusui.actionunit.n;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public interface Context {

  Context createChild();

  <V> V valueOf(String variableName);

  Context assignTo(String variableName, Object value);

  Optional<Throwable> thrownException();

  class Impl implements Context {
    public static final String              ONGOING_EXCEPTION = "ONGOING_EXCEPTION";
    private final       Map<String, Object> variables         = new HashMap<>();
    private final       Context             parent;

    private Impl() {
      this.parent = null;
    }

    private Impl(Context parent) {
      this.parent = parent;
      this.variables.putAll(Impl.class.cast(this.parent).variables);
    }

    @Override
    public Context createChild() {
      return new Impl(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> V valueOf(String variableName) {
      if (this.variables.containsKey(requireNonNull(variableName)))
        return (V) this.variables.get(variableName);
      throw new NoSuchElementException();
    }

    @Override
    public Context assignTo(String variableName, Object value) {
      if (this.variables.containsKey(requireNonNull(variableName)))
        throw new RuntimeException();
      this.variables.put(variableName, value);
      return this;
    }

    @Override
    public Optional<Throwable> thrownException() {
      return this.variables.containsKey(ONGOING_EXCEPTION)
          ? Optional.of((Throwable) this.variables.get(ONGOING_EXCEPTION))
          : Optional.empty();
    }
  }

  static Context create() {
    return new Impl();
  }
}
