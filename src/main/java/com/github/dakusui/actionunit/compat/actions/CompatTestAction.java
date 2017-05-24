package com.github.dakusui.actionunit.compat.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.compat.connectors.Connectors;
import com.github.dakusui.actionunit.compat.connectors.Pipe;
import com.github.dakusui.actionunit.compat.connectors.Sink;
import com.github.dakusui.actionunit.compat.connectors.Source;
import org.hamcrest.Matcher;

import java.util.function.Function;

import static com.github.dakusui.actionunit.helpers.Checks.checkNotNull;
import static com.github.dakusui.actionunit.compat.connectors.Connectors.*;

public interface CompatTestAction<I, O> extends Piped<I, O> {
  class Base<I, O> extends Impl<I, O> implements CompatTestAction<I, O> {
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
      //noinspection unchecked
      return this.then(toSink(checkNotNull(then)));
    }

    public Action build() {
      checkNotNull(given);
      checkNotNull(when);
      checkNotNull(then);
      return new CompatTestAction.Base<I, O>(given, when, then) {
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
