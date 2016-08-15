package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.connectors.Connectors;
import com.github.dakusui.actionunit.connectors.Pipe;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.connectors.Source;
import com.google.common.base.Function;
import org.hamcrest.Matcher;

import static com.google.common.base.Preconditions.checkNotNull;

public interface TestAction<I, O> extends Action.Piped<I, O> {
  class Base<I, O> extends Impl<I, O> implements TestAction<I, O> {
    public Base(Source<I> setUp, Pipe<I, O> exec, Sink<O> verify) {
      //noinspection unchecked
      super(checkNotNull(setUp), checkNotNull(exec), new Sink[] { checkNotNull(verify) });
    }
  }

  class Builder<I, O> {

    private Source<I> setUp = Connectors.context();
    private Pipe<I, O> exec;
    private Sink<O>    verify;

    Builder() {
    }

    Builder<I, O> setUp(Source<I> setUp) {
      this.setUp = checkNotNull(setUp);
      return this;
    }

    Builder<I, O> setUp(I setUp) {
      this.setUp = Connectors.immutable(setUp);
      return this;
    }

    Builder<I, O> exec(Pipe<I, O> exec) {
      this.exec = checkNotNull(exec);
      return this;
    }

    Builder<I, O> exec(Function<I, O> exec) {
      this.exec = Connectors.toPipe(checkNotNull(exec));
      return this;
    }


    Builder<I, O> verify(Sink<O> verify) {
      this.verify = checkNotNull(verify);
      return this;
    }

    Builder<I, O> verify(Matcher<O> verify) {
      this.verify = Connectors.toSink(checkNotNull(verify));
      return this;
    }

    public Action build() {
      checkNotNull(setUp);
      checkNotNull(exec);
      checkNotNull(verify);
      return new TestAction.Base<>(setUp, exec, verify);
    }
  }
}
