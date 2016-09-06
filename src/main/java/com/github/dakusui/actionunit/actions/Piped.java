package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Context;
import com.github.dakusui.actionunit.connectors.Connectors;
import com.github.dakusui.actionunit.connectors.Pipe;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.connectors.Source;
import com.google.common.base.Function;

import static com.github.dakusui.actionunit.Utils.*;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.join;

public interface Piped<I, O> extends With<I>, Sink<I>, Source<O> {
  class Impl<I, O> extends With.Base<I> implements Piped<I, O> {
    protected final Source<I>  source;
    protected final Pipe<I, O> pipe;
    protected final Sink<O>[]  sinks;
    private final   String     sourceName;
    private final   String     pipeName;
    private final   String     sinksName;

    @SafeVarargs
    public Impl(
        final Source<I> source,
        final Pipe<I, O> pipe,
        final Sink<O>... sinks) {
      this(source, "From", pipe, "Through", sinks, "To");
    }

    protected Impl(
        final Source<I> source, String sourceName,
        final Pipe<I, O> pipe, String pipeName,
        final Sink<O>[] sinks, String sinksName) {
      this(source, sourceName, pipe, pipeName, Connectors.<O>mutable(), sinks, sinksName);
    }

    private Impl(
        final Source<I> source, String sourceName,
        final Pipe<I, O> pipe, String pipeName,
        final Mutable<O> output,
        final Sink<O>[] sinks, String sinksName) {
      //noinspection unchecked
      super(
          source,
          Named.Factory.create(pipeName,
              Sequential.Factory.INSTANCE.create(
                  asList(
                      new Tag(0),
                      new With.Base<>(
                          output,
                          Named.Factory.create(sinksName,
                              Sequential.Factory.INSTANCE.create(
                                  transform(range(sinks.length),
                                      new Function<Integer, Tag>() {
                                        @Override
                                        public Tag apply(Integer input) {
                                          return new Tag(input);
                                        }
                                      }))),
                      /*(Sink<O>[])*/sinks
                      )))),
          new Sink/*<I>*/[] {
              new Sink<I>() {
                @Override
                public void apply(I input, Context context) {
                  output.set(pipe.apply(input, context));
                }

                public String toString() {
                  return describe(pipe);
                }
              }
          }

      );
      this.source = checkNotNull(source);
      this.sourceName = sourceName;
      this.pipe = checkNotNull(pipe);
      this.pipeName = pipeName;
      this.sinks = checkNotNull(sinks);
      this.sinksName = sinksName;
    }

    @Override
    public O apply(Context context) {
      O ret = pipe.apply(source.apply(context), context);
      try {
        return ret;
      } finally {
        for (Sink<O> each : sinks) {
          each.apply(ret, context);
        }
      }
    }

    @Override
    public void apply(I input, Context context) {
      for (Sink<O> each : sinks) {
        each.apply(pipe.apply(input, context), context);
      }
    }

    @Override
    public String toString() {
      return format("%s %s:(%s) %s:(%s) %s:{%s}",
          formatClassName(),
          this.sourceName,
          describe(this.source()),
          this.pipeName,
          join(transform(
              asList(this.getSinks()),
              new Function<Sink<I>, Object>() {
                @Override
                public Object apply(Sink<I> sink) {
                  return describe(sink);
                }
              }),
              ","),
          this.sinksName,
          join(transform(
              asList(this.sinks),
              new Function<Sink<O>, Object>() {
                @Override
                public Object apply(Sink<O> input) {
                  return describe(input);
                }
              }),
              ",")
      );
    }
  }

  enum Factory {
    ;

    @SafeVarargs
    public static <I, O> Piped<I, O> create(
        final Source<I> source,
        final Pipe<I, O> pipe,
        final Sink<O>... sinks) {
      checkNotNull(source);
      checkNotNull(pipe);
      //noinspection unchecked
      return new Impl<>(source, pipe, sinks);
    }
  }
}
