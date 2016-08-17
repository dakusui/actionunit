package com.github.dakusui.actionunit.connectors;

import com.github.dakusui.actionunit.Describable;
import com.github.dakusui.actionunit.Context;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;

public interface Source<T> {
  T apply(Context context);

  class Immutable<T> implements Source<T>, Describable {
    private final T value;

    public Immutable(T value) {
      this.value = value;
    }

    public T apply(Context context) {
      return this.value;
    }

    @Override
    public String describe() {
      return Objects.toString(this.value);
    }
  }


  class Mutable<T> implements Source<T>, Describable {
    private boolean isSet = false;
    private T value;

    @Override
    public String describe() {
      return isSet
          ? format("current=%s", this.value)
          : "value isn't set yet";
    }

    @Override
    synchronized public T apply(Context context) {
      checkState(isSet, "Value isn't set yet");
      return this.value;
    }

    synchronized public void set(T value) {
      this.value = value;
      this.isSet = true;
    }
  }
}
