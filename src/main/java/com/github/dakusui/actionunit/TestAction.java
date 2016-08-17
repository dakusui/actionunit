package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.connectors.Connectors;
import com.github.dakusui.actionunit.connectors.Pipe;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.connectors.Source;
import com.google.common.base.Function;
import org.hamcrest.Matcher;

import static com.github.dakusui.actionunit.connectors.Connectors.*;
import static com.google.common.base.Preconditions.checkNotNull;

public interface TestAction<I, O> extends Action.Piped<I, O> {
  class Base<I, O> extends Impl<I, O> implements TestAction<I, O> {
    public Base(Source<I> given, Pipe<I, O> when, Sink<O> then) {
      //noinspection unchecked
      super(checkNotNull(given), "Given", checkNotNull(when), "When", new Sink[] { checkNotNull(then) }, "Then");
    }
  }

  class Builder<I, O> {

    private final String testName;
    private Source<I> given = Connectors.context();
    private Pipe<I, O> when;
    private Sink<O>    then;

    public Builder() {
      this(null);
    }

    public Builder(String testName) {
      this.testName = testName;
    }

    public Builder<I, O> given(Source<I> given) {
      this.given = checkNotNull(given);
      return this;
    }

    public Builder<I, O> given(I setUp) {
      return this.given(immutable(setUp));
    }

    public Builder<I, O> when(Pipe<I, O> when) {
      this.when = checkNotNull(when);
      return this;
    }

    public Builder<I, O> when(Function<I, O> when) {
      return this.when(toPipe(checkNotNull(when)));
    }


    public Builder<I, O> then(Sink<O> then) {
      this.then = checkNotNull(then);
      return this;
    }

    public Builder<I, O> then(Matcher<O> then) {
      return this.then(toSink(checkNotNull(then)));
    }

    public Action build() {
      checkNotNull(given);
      checkNotNull(when);
      checkNotNull(then);
      return new TestAction.Base<I, O>(given, when, then) {
        @Override
        public String formatClassName() {
          return Builder.this.testName == null
              ? super.formatClassName()
              : Builder.this.testName;
        }
      };
    }
  }
}
