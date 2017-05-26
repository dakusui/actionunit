package com.github.dakusui.actionunit.compat.actions;

import com.github.dakusui.actionunit.compat.Context;
import com.github.dakusui.actionunit.helpers.Utils;
import com.github.dakusui.actionunit.actions.Named;
import com.github.dakusui.actionunit.actions.Sequential;
import com.github.dakusui.actionunit.compat.connectors.Connectors;
import com.github.dakusui.actionunit.compat.connectors.Pipe;
import com.github.dakusui.actionunit.compat.connectors.Sink;
import com.github.dakusui.actionunit.compat.connectors.Source;

import static com.github.dakusui.actionunit.helpers.Autocloseables.transform;
import static com.github.dakusui.actionunit.helpers.Utils.describe;
import static com.github.dakusui.actionunit.helpers.Checks.checkNotNull;
import static java.lang.String.format;
import static java.util.Arrays.asList;

/**
 * <pre>
 * +--------+       source  +---------+
 * |  CompatWith  |<>----+------->|Source<I>|
 * +--------+      |        +---------+
 *      |          |
 *      |          |sinks   +---------+
 *      |          +------->|Sink<I>[]|
 *      A                   +---------+
 *      |
 * +----------+     pipe    +---------+
 * |Piped<I,O>|<>--+------->|Pipe<I,O>|
 * +----------+    |        +---------+
 *                 |
 *                 |destinationSinks
 *                 |        +---------+
 *                 +------->|Sink<O>[]|
 *                          +---------+
 * </pre>
 *
 * @param <I> Input type
 * @param <O> Output type
 */
public interface Piped<I, O> extends CompatWith<I> {
  Pipe<I, O> getPipe();

  Sink<O>[] getDestinationSinks();

  class Impl<I, O> extends CompatWithBase<I> implements Piped<I, O> {
    protected final Source<I>  source;
    protected final Pipe<I, O> pipe;
    protected final Sink<O>[]  destinationSinks;
    private final   String     sourceName;
    private final   String     pipeName;
    private final   String     destinationSinksName;

    @SafeVarargs
    public Impl(
        final Source<I> source,
        final Pipe<I, O> pipe,
        final Sink<O>... destinationSinks) {
      this(source, "From", pipe, "Through", destinationSinks, "To");
    }

    protected Impl(
        final Source<I> source, String sourceName,
        final Pipe<I, O> pipe, String pipeName,
        final Sink<O>[] destinationSinks, String destinationSinksName) {
      this(source, sourceName, pipe, pipeName, Connectors.mutable(), destinationSinks, destinationSinksName);
    }

    private Impl(
        final Source<I> source, String sourceName,
        final Pipe<I, O> pipe, String pipeName,
        final Source.Mutable<O> output,
        final Sink<O>[] destinationSinks, String destinationSinksName) {
      //noinspection unchecked
      super(
          source,
          Named.create(pipeName,
              Sequential.Factory.INSTANCE.create(
                  asList(
                      new Tag(0),
                      new CompatWithBase<>(
                          output,
                          Named.create(
                              destinationSinksName,
                              Tag.createFromRange(0, destinationSinks.length)
                          ),
                      /*(Sink<O>[])*/
                          destinationSinks)))),
          new Sink/*<I>*/[] {
              new Sink<I>() {
                @Override
                public void apply(I input, Context context) {
                  output.set(pipe.apply(input, context.getParent()));
                }

                public String toString() {
                  return describe(pipe);
                }
              }
          });
      this.source = checkNotNull(source);
      this.sourceName = sourceName;
      this.pipe = checkNotNull(pipe);
      this.pipeName = pipeName;
      this.destinationSinks = checkNotNull(destinationSinks);
      this.destinationSinksName = destinationSinksName;
    }

    @Override
    public String toString() {
      //noinspection unchecked
      return format("%s%n%s:%s%n%s:%s%n%s:[%s]",
          formatClassName(),
          this.sourceName,
          describe(this.getSource()),
          this.pipeName,
          String.join(
              ",",
              transform(
                  asList(this.getSinks()),
                  Utils::describe)),
          this.destinationSinksName,
          String.join(
              ",",
              transform(
                  asList((Sink<O>[]) getDestinationSinks()),
                  Utils::describe))
      );
    }

    @Override
    public Pipe<I, O> getPipe() {
      return this.pipe;
    }

    public Sink<O>[] getDestinationSinks() {
      return this.destinationSinks;
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
