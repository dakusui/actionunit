package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.exceptions.ActionAssertionError;
import com.github.dakusui.actionunit.helpers.InternalUtils;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface TestAction extends Action, Context {
  Action given();

  Action when();

  Action then();

  class Builder<I, O> {
    private final int            id;
    private       Supplier<I>    input;
    private       Function<I, O> operation;
    private       Predicate<O>   check;

    public Builder(int id) {
      this.id = id;
    }

    public Builder<I, O> given(String description, Supplier<I> input) {
      Objects.requireNonNull(input);
      this.input = new Supplier<I>() {
        @Override
        public I get() {
          return input.get();
        }

        @Override
        public String toString() {
          return description;
        }
      };
      return this;
    }

    public Builder<I, O> when(String description, Function<I, O> operation) {
      Objects.requireNonNull(operation);
      this.operation = new Function<I, O>() {
        @Override
        public O apply(I i) {
          return operation.apply(i);
        }

        @Override
        public String toString() {
          return description;
        }

      };
      return this;
    }

    public TestAction then(String description, Predicate<O> check) {
      Objects.requireNonNull(check);
      this.check = new Predicate<O>() {
        @Override
        public boolean test(O o) {
          return check.test(o);
        }

        @Override
        public String toString() {
          return description;
        }
      };
      Objects.requireNonNull(this.input);
      Objects.requireNonNull(this.operation);
      Objects.requireNonNull(this.check);
      return new Base<>(id, this);
    }

  }

  class Base<I, O> extends ActionBase implements TestAction {
    private final AtomicReference<Supplier<I>> input  = new AtomicReference<>(
        () -> {
          throw new IllegalStateException("input is not set yet.");
        });
    private final AtomicReference<Supplier<O>> output = new AtomicReference<>(
        () -> {
          throw new IllegalStateException("output is not set yet.");
        });
    private final Builder<I, O> builder;

    public Base(int id, Builder<I, O> builder) {
      super(id);
      this.builder = builder;
    }

    public I input() {
      return input.get().get();
    }

    O output() {
      return output.get().get();
    }

    Function<I, O> operation() {
      return builder.operation;
    }

    Predicate<O> check() {
      return builder.check;
    }

    @Override
    public Action given() {
      return Context.Internal.named(0, "Given",
          Context.Internal.simple(
              0,
              builder.input.toString(),
              () -> Base.this.input.set(InternalUtils.describable(builder.input.toString(), builder.input.get())))
      );
    }

    @Override
    public Action when() {
      return Context.Internal.named(1, "When",
          Context.Internal.simple(
              0,
              builder.operation.toString(),
              () -> output.set(
                  () -> builder.operation.apply(Base.this.input())
              )
          ));
    }


    @Override
    public Action then() {
      return Context.Internal.named(2, "Then",
          Context.Internal.simple(0,
              check().toString(),
              () -> {
                if (!check().test(output()))
                  throw new ActionAssertionError(String.format(
                      "%s(x) %s was not satisfied because %s(x)=<%s>; x=<%s>",
                      operation(),
                      check(),
                      operation(),
                      output(),
                      input()
                  ));
              }
          ));
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }
  }
}
