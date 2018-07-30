package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.DataSupplier;

import java.util.Formatter;

import static java.util.Objects.requireNonNull;

public interface ForEach<E> extends Action {
  String loopVariableName();

  DataSupplier<E> data();

  Action perform();

  boolean isParallel();

  default void accept(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format("for each of %s %s", data(), isParallel() ? "parallely" : "sequentially");
  }

  class Builder<E> extends Action.Builder<ForEach<E>> {
    private final DataSupplier<E> dataSupplier;
    private final String          loopVariableName;
    private       Action          perform = Leaf.NOP;
    private       boolean         parallel;

    public Builder(String loopVariableName, DataSupplier<E> dataSupplier) {
      this.loopVariableName = requireNonNull(loopVariableName);
      this.dataSupplier = requireNonNull(dataSupplier);
      this.sequentially();
    }

    public Action perform(Action perform) {
      this.perform = requireNonNull(perform);
      return this.$();
    }

    public Builder<E> parallelly() {
      this.parallel = true;
      return this;
    }

    public Builder<E> sequentially() {
      this.parallel = false;
      return this;
    }

    public ForEach<E> build() {
      return new ForEach<E>() {
        @Override
        public String loopVariableName() {
          return Builder.this.loopVariableName;
        }

        @Override
        public DataSupplier<E> data() {
          return Builder.this.dataSupplier;
        }

        @Override
        public Action perform() {
          return Builder.this.perform;
        }

        @Override
        public boolean isParallel() {
          return Builder.this.parallel;
        }
      };
    }
  }

}
