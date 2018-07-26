package com.github.dakusui.actionunit.n;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.n.Actions.attempt;
import static com.github.dakusui.actionunit.n.Actions.forEach;
import static java.util.Objects.requireNonNull;

public interface ForEach<E> extends Action {
  String loopVariableName();

  Stream<E> data();

  Action perform();

  boolean isParallel();

  default void accept(Visitor visitor) {
    visitor.visit(this);
  }

  class Builder<E> {
    private final Supplier<Stream<E>> dataSupplier;
    private final String              loopVariableName;
    private       Action              perform = Actions.nop();
    private       boolean             parallel;

    public Builder(String loopVariableName, Supplier<Stream<E>> dataSupplier) {
      this.loopVariableName = requireNonNull(loopVariableName);
      this.dataSupplier = requireNonNull(dataSupplier);
      this.sequential();
    }

    public Builder<E> perform(Action perform) {
      this.perform = requireNonNull(perform);
      return this;
    }

    public Builder<E> parallel() {
      this.parallel = true;
      return this;
    }

    public Builder<E> sequential() {
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
        public Stream<E> data() {
          return requireNonNull(Builder.this.dataSupplier.get());
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

    /**
     * A synonym of {@code build()} method. This method is defined not to use a
     * method name 'build', which introduces extra word not relating to what is
     * being achieved.
     *
     * @return An object built by {@code build} method.
     */
    public ForEach<E> $() {
      return build();
    }
  }

}
