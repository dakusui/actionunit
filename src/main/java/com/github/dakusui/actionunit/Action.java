package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.connectors.Connectors;
import com.github.dakusui.actionunit.connectors.Pipe;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.connectors.Source;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.Iterator;

import static com.github.dakusui.actionunit.Utils.*;
import static com.google.common.base.Preconditions.*;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.join;

/**
 * Defines interface of an action performed by ActionUnit runner.
 */
public interface Action {

  /**
   * Applies a visitor to this element.
   *
   * @param visitor the visitor operating on this element.
   */
  void accept(Visitor visitor);

  /**
   * A skeletal base class of all {@code Action}s.
   */
  abstract class Base implements Action, Describable {
    @Override
    public String describe() {
      return this.formatClassName();
    }

    protected String formatClassName() {
      return Utils.shortClassNameOf(this.getClass()).replaceAll("^Action\\$", "").replaceAll("\\$Base$", "");
    }
  }

  /**
   * Any action that actually does any concrete operation outside "ActionUnit"
   * framework should extend this class.
   */
  abstract class Leaf extends Base {
    protected final String description;

    public Leaf() {
      this(null);
    }

    public Leaf(String description) {
      this.description = description;
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    @Override
    public String describe() {
      return this.description == null
          ? formatClassName()
          : description;
    }

    abstract public void perform();
  }

  interface Composite extends Action, Iterable<Action> {
    int size();

    /**
     * A skeletal implementation for composite actions, such as {@link Sequential.Base} or {@link Concurrent.Base}.
     */
    abstract class Base extends Action.Base implements Composite {
      private final Iterable<? extends Action> actions;
      private final String                     summary;

      public Base(String summary, Iterable<? extends Action> actions) {
        this.summary = summary;
        this.actions = checkNotNull(actions);
      }

      @Override
      public String describe() {
        return format(
            "%s (%s, %s actions)",
            nonameIfNull(this.summary),
            getName(),
            unknownIfNegative(this.size())
        );
      }

      /**
       * This method may return negative number if {@code actions} is not a collection.
       */
      @Override
      public int size() {
        if (this.actions instanceof Collection) {
          return ((Collection) this.actions).size();
        }
        return -1;
      }

      @Override
      public Iterator<Action> iterator() {
        //noinspection unchecked
        return (Iterator<Action>) this.actions.iterator();
      }

      protected String getName() {
        return shortClassNameOf(this.getClass().getEnclosingClass()).replace("Action$", "");
      }
    }

    interface Factory {
      Composite create(String summary, Iterable<? extends Action> actions);
    }
  }

  interface Concurrent extends Composite {
    /**
     * A class that represents a collection of actions that should be executed concurrently.
     */
    class Base extends Composite.Base implements Concurrent {
      public Base(String summary, Iterable<? extends Action> actions) {
        super(summary, actions);
      }

      @Override
      public void accept(Visitor visitor) {
        visitor.visit(this);
      }
    }

    enum Factory implements Composite.Factory {
      INSTANCE;

      @Override
      public Concurrent create(String summary, Iterable<? extends Action> actions) {
        return new Base(summary, actions);
      }

      public String toString() {
        return "Concurrent";
      }
    }
  }

  /**
   * An interface that represents a sequence of actions that should be executed one
   * after another.
   */
  interface Sequential extends Composite {
    class Base extends Composite.Base implements Sequential {
      public Base(String summary, Iterable<? extends Action> actions) {
        super(summary, actions);
      }

      @Override
      public void accept(Visitor visitor) {
        visitor.visit(this);
      }
    }

    enum Factory implements Composite.Factory {
      INSTANCE;

      @Override
      public Sequential create(String summary, Iterable<? extends Action> actions) {
        return new Base(summary, actions);
      }

      public String toString() {
        return "Sequential";
      }
    }
  }

  class ForEach<T> extends Base {
    private final Composite.Factory   factory;
    private final Iterable<Source<T>> dataSource;
    private final Action              action;
    private final Sink<T>[]           sinks;


    public ForEach(Composite.Factory factory, Iterable<Source<T>> dataSource, Action action, Sink<T>[] sinks) {
      this.factory = factory;
      this.dataSource = dataSource;
      this.action = checkNotNull(action);
      this.sinks = sinks;
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    @Override
    public String describe() {
      return format("%s (%s, %s items) {%s}",
          this.getClass().getSimpleName(),
          this.factory,
          unknownIfNegative(sizeOrNegativeIfNonCollection(this.dataSource)),
          join(
              transform(
                  asList(sinks),
                  new Function<Sink<T>, Object>() {
                    @Override
                    public Object apply(Sink<T> sink) {
                      return Describables.describe(sink);
                    }
                  }
              ),
              ",")
      );
    }

    public Composite getElements() {
      Function<Source<T>, Action> func = new Function<Source<T>, Action>() {
        @Override
        public Action apply(final Source<T> t) {
          //noinspection unchecked
          return new With.Base(t, ForEach.this.action, ForEach.this.sinks);
        }
      };
      return this.factory.create(
          null, transform(dataSource, func)
      );
    }

    enum Mode {
      SEQUENTIALLY {
        @Override
        Composite.Factory getFactory() {
          return Sequential.Factory.INSTANCE;
        }
      },
      CONCURRENTLY {
        @Override
        Composite.Factory getFactory() {
          return Concurrent.Factory.INSTANCE;
        }
      };

      abstract Composite.Factory getFactory();
    }
  }

  interface With<T> extends Action {

    Source<T> source();

    Sink<T>[] getSinks();

    Action getAction();

    class Base<T> extends Action.Base implements With<T> {
      private final Sink<T>[] sinks;
      private final Source<T> source;
      private final Action    action;

      public Base(Source<T> source, Action action, Sink<T>[] sinks) {
        this.source = checkNotNull(source);
        this.action = checkNotNull(action);
        this.sinks = checkNotNull(sinks);
      }


      @Override
      public Source<T> source() {
        return this.source;
      }

      public Sink<T>[] getSinks() {
        return sinks;
      }

      @Override
      public Action getAction() {
        return this.action;
      }

      @Override
      public void accept(Visitor visitor) {
        visitor.visit(this);
      }

      @Override
      public String describe() {
        return format("%s (%s) {%s}",
            formatClassName(),
            Describables.describe(this.source()),
            join(transform(
                asList(this.getSinks()),
                new Function<Sink<T>, Object>() {
                  @Override
                  public Object apply(Sink<T> sink) {
                    return Describables.describe(sink);
                  }
                }),
                ","));
      }

    }

    class Tag extends Action.Base {
      private final int index;

      public Tag(int i) {
        checkArgument(i >= 0, "Index must not be negative. (%s)", i);
        this.index = i;
      }

      @Override
      public void accept(Visitor visitor) {
        visitor.visit(this);
      }

      @Override
      public String describe() {
        return format("Tag(%d)", index);
      }

      public int getIndex() {
        return index;
      }

      public <T> Leaf toLeaf(final Source<T> source, final Sink<T>[] sinks, final Context context) {
        return new Leaf() {
          @Override
          public void perform() {
            checkState(
                Tag.this.getIndex() < sinks.length,
                "Insufficient number of block(s) are given. (block[%s] was referenced, but only %s block(s) were given.",
                Tag.this.getIndex(),
                sinks.length
            );
            sinks[Tag.this.getIndex()].apply(source.apply(context), context);
          }

          @Override
          public String describe() {
            return Tag.this.describe();
          }
        };
      }
    }
  }

  class Retry extends Base {
    public final Action action;
    public final int    times;
    public final long   intervalInNanos;

    public Retry(Action action, long intervalInNanos, int times) {
      checkNotNull(action);
      checkArgument(intervalInNanos >= 0);
      checkArgument(times >= 0);
      this.action = action;
      this.intervalInNanos = intervalInNanos;
      this.times = times;
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    @Override
    public String describe() {
      return format("%s(%sx%dtimes)",
          formatClassName(),
          formatDuration(intervalInNanos),
          this.times
      );
    }
  }

  class TimeOut extends Base {
    public final Action action;
    public final long   durationInNanos;

    public TimeOut(Action action, long timeoutInNanos) {
      Preconditions.checkArgument(timeoutInNanos > 0);
      this.durationInNanos = timeoutInNanos;
      this.action = checkNotNull(action);
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    @Override
    public String describe() {
      return format(
          "%s (%s)",
          formatClassName(),
          formatDuration(this.durationInNanos)
      );
    }
  }


  /**
   * A visitor of actions, in the style of the visitor design pattern. Classes implementing
   * this interface are used to operate on an action when the kind of element is unknown at compile
   * time. When a visitor is passed to an element's accept method, the visitXYZ method most applicable
   * to that element is invoked.
   * <p/>
   * WARNING: It is possible that methods will be added to this interface to accommodate new, currently
   * unknown, language structures added to future versions of the ActionUnit library. Therefore,
   * visitor classes directly implementing this interface may be source incompatible with future
   * versions of the framework.
   * To avoid this source incompatibility, visitor implementations are encouraged to
   * instead extend the appropriate abstract visitor class that implements this interface. However,
   * an API should generally use this visitor interface as the type for parameters, return type, etc.
   * rather than one of the abstract classes.
   *
   * @see Action.Visitor.Base
   */
  interface Visitor {
    /**
     * Visits an {@code action}
     *
     * @param action action to be visited by this object.
     */
    void visit(Action action);

    /**
     * Visits an {@code action}
     *
     * @param action action to be visited by this object.
     */
    void visit(Action.Leaf action);

    /**
     * Visits an {@code action}
     *
     * @param action action to be visited by this object.
     */
    void visit(Composite action);

    /**
     * Visits an {@code action}
     *
     * @param action action to be visited by this object.
     */
    void visit(Sequential action);

    /**
     * Visits an {@code action}
     *
     * @param action action to be visited by this object.
     */
    void visit(Concurrent action);

    /**
     * Visits an {@code action}
     *
     * @param action action to be visited by this object.
     */
    void visit(ForEach action);

    /**
     * Visits an {@code action}
     *
     * @param action action to be visited by this object.
     */
    void visit(With.Tag action);

    /**
     * Visits an {@code action}
     *
     * @param action action to be visited by this object.
     */
    void visit(With action);

    /**
     * Visits an {@code action}
     *
     * @param action action to be visited by this object.
     */
    void visit(Retry action);

    /**
     * Visits an {@code action}
     *
     * @param action action to be visited by this object.
     */
    void visit(TimeOut action);

    abstract class Base implements Visitor {
      @Override
      public void visit(Leaf action) {
        this.visit((Action) action);
      }

      @Override
      public void visit(Composite action) {
        this.visit((Action) action);
      }

      @Override
      public void visit(Sequential action) {
        this.visit((Composite) action);
      }

      @Override
      public void visit(Concurrent action) {
        this.visit((Composite) action);
      }

      @Override
      public void visit(Action.ForEach action) {
        this.visit((Action) action);
      }

      @Override
      public void visit(With.Tag action) {
        this.visit((Action) action);
      }

      @Override
      public void visit(With action) {
        this.visit((Action) action);
      }

      @Override
      public void visit(Retry action) {
        this.visit((Action) action);
      }

      @Override
      public void visit(TimeOut action) {
        this.visit((Action) action);
      }

    }
  }

  interface Piped<I, O> extends With<I>, Sink<I>, Source<O> {
    class Impl<I, O> extends With.Base<I> implements Piped<I, O> {
      protected final Source<I>  source;
      protected final Pipe<I, O> pipe;
      protected final Sink<O>[]  sinks;

      public Impl(
          final Source<I> source,
          final Pipe<I, O> pipe,
          final Sink<O>[] sinks) {
        this(source, pipe, "Pipe", sinks, "Do");
      }

      protected Impl(
          final Source<I> source,
          final Pipe<I, O> pipe, String pipeName,
          final Sink<O>[] sinks, String sinksName) {
        this(source, pipe, pipeName, Connectors.<O>mutable(), sinks, sinksName);
      }

      private Impl(
          final Source<I> source,
          final Pipe<I, O> pipe, String pipeName,
          final Mutable<O> output,
          final Sink<O>[] sinks, String sinksName) {
        //noinspection unchecked
        super(
            source,
            Sequential.Factory.INSTANCE.create(
                pipeName,
                asList(
                    new Tag(0),
                    new With.Base<>(
                        output,
                        Sequential.Factory.INSTANCE.create(
                            sinksName,
                            transform(range(sinks.length),
                                new Function<Integer, Tag>() {
                                  @Override
                                  public Tag apply(Integer input) {
                                    return new Tag(input);
                                  }
                                })),
                        /*(Sink<O>[])*/sinks
                    ))),
            new Sink/*<I>*/[] {
                new Sink<I>() {
                  @Override
                  public void apply(I input, Context context) {
                    output.set(pipe.apply(input, context));
                  }

                  public String toString() {
                    return Describables.describe(pipe);
                  }
                }
            }
        );
        this.source = checkNotNull(source);
        this.pipe = checkNotNull(pipe);
        this.sinks = checkNotNull(sinks);
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
}
