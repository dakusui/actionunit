package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.visitors.Context;
import com.google.common.base.Throwables;

import static com.github.dakusui.actionunit.Utils.waitFor;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static java.util.Arrays.asList;

public interface TestAction<I, O extends TestAction.Output> extends Action.With<I> {
  interface Output {
    class Text implements Output {
      private final String value;

      public Text(String value) {
        this.value = value;
      }

      public String value() {
        return this.value;
      }
    }

    class Simple implements Output {
      final Throwable exception;

      public Simple(Throwable exception) {
        this.exception = exception;
      }

      boolean passed() {
        return exception == null;
      }

      Throwable getException() {
        return checkNotNull(this.exception);
      }

      RuntimeException propagate() {
        return Throwables.propagate(this.getException());
      }
    }
  }

  enum Factory {
    ;

    public static <I, O extends TestAction.Output> TestAction<I, O> create(
        final Pipe<I, O> execute,
        final Sink<O> verify) {
      checkNotNull(execute);
      checkNotNull(verify);
      return new TestAction<I, O>() {
        final Object monitor = new Object();
        I in = null;
        O out = null;
        boolean done = false;

        @Override
        public Source<I> source() {
          synchronized (monitor) {
            return new Source<I>(){
              @Override
              public I apply() {
                checkState(done);
                return in;
              }
            };
          }
        }

        @Override
        public Sink<I>[] getSinks() {
          //noinspection unchecked
          return new Sink[] {
              new Sink<I>() {
                @Override
                public void apply(I input, Context context) {
                  synchronized (monitor) {
                    in = input;
                    out = execute.apply(input, context);
                    done = true;
                    monitor.notifyAll();
                  }
                }
              }
          };
        }

        @Override
        public Action getAction() {
          //noinspection unchecked
          return Concurrent.Factory.INSTANCE.create(
              format("Test (execute=%s, verify=%s)",
                  Describables.describe(execute),
                  Describables.describe(verify)),
              asList(
                  new Tag(0),
                  new With.Base<>(
                      new Source<O>() {
                        @Override
                        public O apply() {
                          synchronized (monitor) {
                            while (!done) {
                              waitFor(this);
                            }
                            return out;
                          }
                        }
                      },
                      new Tag(0),
                      (Sink<O>[]) new Sink[] {
                          verify
                      }
                  )
              )
          );
        }

        @Override
        public void accept(Visitor visitor) {
          visitor.visit(this);
        }
      };
    }
  }
}
