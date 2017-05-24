package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.Actions;
import com.github.dakusui.actionunit.exceptions.ActionAssertionError;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface TestAction extends Action {
  Action given();

  Action when();

  Action then();

  class Builder<I, O> {

    private Supplier<I>    input;
    private Function<I, O> operation;
    private Predicate<O>   check;

    public Builder<I, O> given(String description, Supplier<I> input) {
      Objects.requireNonNull(description);
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
      return new Base<>(this);
    }

    static class Base<I, O> implements TestAction {
      private final AtomicReference<Supplier<I>> input  = new AtomicReference<>(
          () -> {
            throw new IllegalStateException("input is not set yet.");
          });
      private final AtomicReference<Supplier<O>> output = new AtomicReference<>(
          () -> {
            throw new IllegalStateException("output is not set yet.");
          });
      private final Builder<I, O> builder;

      public Base(Builder<I, O> builder) {
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
        return Actions.simple(
            "Given:",
            new Runnable() {
              @Override
              public void run() {
                Base.this.input.set(toSupplier(builder.input.toString(), builder.input.get()));
              }

              @Override
              public String toString() {
                return input.get().toString();
              }

              private <T> Supplier<T> toSupplier(String description, T value) {
                return new Supplier<T>() {
                  @Override
                  public T get() {
                    return value;
                  }

                  @Override
                  public String toString() {
                    return description;
                  }
                };
              }
            }
        );
      }

      @Override
      public Action when() {
        return Actions.simple(
            "When:",
            new Runnable() {
              @Override
              public void run() {
                output.set(
                    () -> builder.operation.apply(Base.this.input())
                );
              }

              @Override
              public String toString() {
                return builder.operation.toString();
              }
            }
        );
      }


      @Override
      public Action then() {
        return Actions.simple(
            "Then:",
            new Runnable() {
              @Override
              public void run() {
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

              @Override
              public String toString() {
                return check().toString();
              }
            }
        );
      }

      @Override
      public void accept(Visitor visitor) {
        visitor.visit(this);
      }
    }
  }
}
