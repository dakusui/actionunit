package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.connectors.Connectors;
import com.github.dakusui.actionunit.connectors.Pipe;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.connectors.Source;
import com.google.common.base.Function;
import org.hamcrest.Matcher;

import static com.github.dakusui.actionunit.Utils.transform;
import static com.github.dakusui.actionunit.connectors.Connectors.*;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.join;

public interface TestAction<I, O> extends Action.Piped<I, O> {
  class Base<I, O> extends Impl<I, O> implements TestAction<I, O> {
    public Base(Source<I> given, Pipe<I, O> when, Sink<O> then) {
      //noinspection unchecked
      super(checkNotNull(given), checkNotNull(when), new Sink[] { checkNotNull(then) });
    }

    @Override
    public String describe() {
      return format("%s Given:{%s} When:{%s} Then:{%s}",
          formatClassName(),
          Describables.describe(this.source()),
          join(transform(
              asList(this.getSinks()),
              new Function<Sink<I>, Object>() {
                @Override
                public Object apply(Sink<I> sink) {
                  return Describables.describe(sink);
                }
              }),
              ","),
          join(transform(
              asList(this.sinks),
              new Function<Sink<O>, Object>() {
                @Override
                public Object apply(Sink<O> input) {
                  return Describables.describe(input);
                }
              }),
              ",")
      );
    }

  }

  class Builder<I, O> {

    private Source<I> given = Connectors.context();
    private Pipe<I, O> when;
    private Sink<O>    then;

    public Builder() {
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
      return new TestAction.Base<>(given, when, then);
    }
  }
}
