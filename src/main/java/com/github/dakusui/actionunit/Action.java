package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.exceptions.Abort;

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
  abstract class Base implements Action {
    @Override
    public String toString() {
      return this.formatClassName();
    }

    protected String formatClassName() {
      return Utils.shortClassNameOf(this.getClass()).replaceAll("^Action\\$", "").replaceAll("\\$Base$", "");
    }
  }

  /**
   * A skeletal base class of a simple action, which cannot be divided into smaller
   * actions.
   * Any action that actually does any concrete operation outside "ActionUnit"
   * framework should extend this class.
   */
  abstract class Leaf extends Base {
    public Leaf() {
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    abstract public void perform();
  }

  /**
   * An action that has a name.
   */
  interface Named extends Action {
    /**
     * Returns a name of this action.
     */
    String getName();

    /**
     * Returns an action named by this object.
     */
    Action getAction();

    /**
     * A skeletal base class to implement {@code Named} action.
     */
    class Base extends Action.Base implements Named {
      private final String name;
      private final Action action;

      /**
       * Creates an object of this class.
       *
       * @param name   Name of this object.
       * @param action Action to be performed as a body of this object.
       */
      public Base(String name, Action action) {
        this.name = checkNotNull(name);
        this.action = checkNotNull(action);
      }

      /**
       * {@inheritDoc}
       *
       * @param visitor the visitor operating on this element.
       */
      @Override
      public void accept(Visitor visitor) {
        visitor.visit(this);
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public String getName() {
        return name;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public Action getAction() {
        return action;
      }

      /**
       * {@inheritDoc}
       */
      public String toString() {
        return this.getName();
      }
    }

    /**
     * A factory that creates {@link Named} action object.
     */
    enum Factory {
      ;

      /**
       * Creates an action with the given {@code name} and {@code action}.
       *
       * @param name   A name of the returned action.
       * @param action An action body of the returned action.
       */
      public static Named create(String name, Action action) {
        return new Named.Base(name, action);
      }
    }
  }

  /**
   * An interface to represent an action which executes its members.
   * The manner in which those members should be executed is left to sub-interfaces
   * of this.
   *
   * @see Sequential
   * @see Concurrent
   */
  interface Composite extends Action, Iterable<Action> {
    int size();

    /**
     * A skeletal implementation for composite actions, such as {@link Sequential.Impl} or {@link Concurrent.Base}.
     */
    abstract class Base extends Action.Base implements Composite {
      private final Iterable<? extends Action> actions;
      private final String                     typeName;

      public Base(String typeName, Iterable<? extends Action> actions) {
        this.actions = checkNotNull(actions);
        this.typeName = checkNotNull(typeName);
      }

      @Override
      public String toString() {
        return format("%s (%s actions)", typeName, unknownIfNegative(this.size()));
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
    }

    interface Factory {
      Composite create(Iterable<? extends Action> actions);
    }
  }

  /**
   * An interface that represents a sequence of actions to be executed concurrently.
   */
  interface Concurrent extends Composite {
    /**
     * A class that represents a collection of actions that should be executed concurrently.
     */
    class Base extends Composite.Base implements Concurrent {
      public Base(Iterable<? extends Action> actions) {
        super("Concurrent", actions);
      }

      @Override
      public void accept(Visitor visitor) {
        visitor.visit(this);
      }
    }

    enum Factory implements Composite.Factory {
      INSTANCE;

      @Override
      public Concurrent create(Iterable<? extends Action> actions) {
        return new Base(actions);
      }

      /**
       * {@inheritDoc}
       */
      public String toString() {
        return "Concurrent";
      }
    }
  }

  /**
   * An interface that represents a sequence of actions to be executed one
   * after another.
   */
  interface Sequential extends Composite {
    /**
     * An implementation of {@link Sequential} action.
     */
    class Impl extends Composite.Base implements Sequential {
      /**
       * Creates an object of this class.
       *
       * @param actions Actions to be executed by this object.
       */
      public Impl(Iterable<? extends Action> actions) {
        super("Sequential", actions);
      }

      /**
       * {@inheritDoc}
       *
       * @param visitor the visitor operating on this element.
       */
      @Override
      public void accept(Visitor visitor) {
        visitor.visit(this);
      }
    }

    /**
     * A factory for {@link Sequential} action object.
     */
    enum Factory implements Composite.Factory {
      INSTANCE;

      @Override
      public Sequential create(Iterable<? extends Action> actions) {
        return new Impl(actions);
      }

      public String toString() {
        return "Sequential";
      }
    }
  }

  /**
   * An action that is repeated on values given by an {@link Iterable&lt;T&gt;}.
   *
   * @param <T> A type of values on which this action is repeated.
   */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
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
                      return describe(sink);
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
          return createWithAction(t);
        }

        private With createWithAction(final Source<T> t) {
          return new With.Base<T>(t, ForEach.this.action, ForEach.this.sinks) {
            @Override
            public int hashCode() {
              return ForEach.this.action.hashCode();
            }

            @Override
            public boolean equals(Object anotherObject) {
              if (!(anotherObject instanceof With.Base)) {
                return false;
              }
              With.Base another = (With.Base) anotherObject;
              return ForEach.this.action.equals(another.action) && Arrays.equals(ForEach.this.sinks, another.sinks);
            }
          };
        }
      };
      return this.factory.create(transform(dataSource, func));
    }

    public Action getAction() {
      return this.action;
    }

    public enum Mode {
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
      public String toString() {
        return format("%s (%s) {%s}",
            formatClassName(),
            describe(this.source()),
            join(transform(
                asList(this.getSinks()),
                new Function<Sink<T>, Object>() {
                  @Override
                  public Object apply(Sink<T> sink) {
                    return describe(sink);
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
      public String toString() {
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
          public String toString() {
            return Tag.this.toString();
          }
        };
      }
    }
  }

  /**
   * An action that corresponds to Java's try/catch mechanism.
   * In order to build an instance of this class, use {@link Builder} class.
   *
   * @param <T> Type of exception which is caught and handled by {@code recover} action.
   */
  class Attempt<T extends Throwable> extends Base {
    public final Action    attempt;
    public final Class<T>  exceptionClass;
    public final Action    recover;
    public final Sink<T>[] sinks;
    public final Action    ensure;

    /**
     * Creates an object of this class.
     *
     * @param attempt        Action initially attempted by this action.
     * @param exceptionClass Exception on which {@code recover} action is performed
     *                       if it is thrown during {@code attempt} action's execution.
     * @param recover        Action performed when an exception of {exceptionClass}
     *                       is thrown.
     * @param sinks          sink operations applied as a part of {@code recover}
     *                       action.
     * @param ensure         Action which will be performed regardless of {@code attempt}
     *                       action's behavior.
     */
    protected Attempt(Action attempt, Class<? extends Throwable> exceptionClass, Action recover, Sink<? extends Throwable>[] sinks, Action ensure) {
      this.attempt = attempt;
      //noinspection unchecked
      this.exceptionClass = (Class<T>) exceptionClass;
      this.recover = Action.Named.Factory.create("Recover", recover);
      //noinspection unchecked
      this.sinks = (Sink<T>[]) sinks;
      this.ensure = Action.Named.Factory.create("Ensure", ensure);
    }

    /**
     * {@inheritDoc}
     *
     * @param visitor the visitor operating on this element.
     */
    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    /**
     * A builder to construct an instance of {@link Attempt} action.
     */
    public static class Builder {
      private final Action attempt;
      private Action                      recover        = nop();
      @SuppressWarnings("unchecked")
      private Sink<? extends Throwable>[] recoverWith    = new Sink[0];
      private Action                      ensure         = nop();
      private Class<? extends Throwable>  exceptionClass = ActionException.class;

      public Builder(Action attempt) {
        this.attempt = checkNotNull(attempt);
      }

      @SafeVarargs
      public final <T extends Throwable> Builder recover(Class<T> exceptionClass, Action action, Sink<? extends T>... sinks) {
        this.exceptionClass = checkNotNull(exceptionClass);
        this.recover = checkNotNull(action);
        this.recoverWith = sinks;
        return this;
      }

      @SafeVarargs
      public final <T extends Throwable> Builder recover(Class<T> exceptionClass, Sink<? extends T>... sinks) {
        return this.recover(
            exceptionClass,
            sequential(transform(range(0, sinks.length),
                new Function<Integer, Action>() {
                  @Override
                  public Action apply(Integer input) {
                    return tag(input);
                  }
                })),
            sinks
        );
      }

      @SafeVarargs
      public final Builder recover(Action action, Sink<? extends ActionException>... sinks) {
        return recover(ActionException.class, action, sinks);
      }

      @SafeVarargs
      public final Builder recover(Sink<? extends ActionException>... sinks) {
        return recover(ActionException.class, sinks);
      }

      public Builder ensure(Action action) {
        this.ensure = checkNotNull(action);
        return this;
      }

      public Builder ensure(Runnable runnable) {
        return this.ensure(simple(runnable));
      }

      public <T extends Throwable> Attempt<T> build() {
        return new Attempt<>(this.attempt, this.exceptionClass, this.recover, this.recoverWith, this.ensure);
      }
    }
  }

  class Retry extends Base {
    /**
     * A constant that represents an instance of this class should be repeated infinitely.
     */
    public static final int INFINITE = -1;
    public final Action action;
    public final int    times;
    public final long   intervalInNanos;

    public Retry(Action action, long intervalInNanos, int times) {
      checkNotNull(action);
      checkArgument(intervalInNanos >= 0);
      checkArgument(times >= 0 || times == INFINITE);
      this.action = action;
      this.intervalInNanos = intervalInNanos;
      this.times = times;
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    @Override
    public String toString() {
      return format("%s(%sx%dtimes)",
          formatClassName(),
          formatDuration(intervalInNanos),
          this.times
      );
    }
  }

  class TimeOut extends Base {
    /**
     * A constant which means an instance of this class should wait forever.
     */
    public static final int FOREVER = -1;
    public final Action action;
    public final long   durationInNanos;

    /**
     * Creates an object of this class.
     *
     * @param action         Action to be monitored and interrupted by this object.
     * @param timeoutInNanos Duration to time out in nano seconds.
     */
    public TimeOut(Action action, long timeoutInNanos) {
      Preconditions.checkArgument(timeoutInNanos > 0 || timeoutInNanos == FOREVER,
          "Timeout duration must be positive or %d (FOREVER) but %d was given",
          FOREVER,
          timeoutInNanos
      );
      this.durationInNanos = timeoutInNanos;
      this.action = checkNotNull(action);
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    @Override
    public String toString() {
      return format(
          "%s(%s)",
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
   * @see Base
   */
  interface Visitor {
    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    void visit(Action action);

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    void visit(Leaf action);

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    void visit(Named action);

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    void visit(Composite action);

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    void visit(Sequential action);

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    void visit(Concurrent action);

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    void visit(ForEach action);

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    void visit(With.Tag action);

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    void visit(With action);

    /**
     * Visits an {@code action}.
     * An implementation of this method should not attempt retry if {@link Abort} exception
     * is thrown.
     *
     * @param action action to be visited by this object.
     */
    void visit(Retry action);

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    void visit(TimeOut action);

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    void visit(Attempt action);

    abstract class Base implements Visitor {

      protected Base() {
      }

      @Override
      public void visit(Leaf action) {
        this.visit((Action) action);
      }

      @Override
      public void visit(Named action) {
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
      public void visit(ForEach action) {
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

      @Override
      public void visit(Attempt action) {
        this.visit((Action) action);
      }
    }
  }
}
