package com.github.dakusui.actionunit;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.Iterator;

import static com.github.dakusui.actionunit.Utils.*;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;
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
   * Describes this object.
   */
  String describe();

  /**
   * A skeletal base class of all {@code Action}s.
   */
  abstract class Base implements Action {
  }

  /**
   * Any action that actually does any concrete operation outside "ActionUnit"
   * framework should extend this class.
   */
  abstract class Leaf extends Base {
    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    abstract public void perform();
  }

  /**
   * A skeletal implementation for composite actions, such as {@link Action.Sequential} or {@link Action.Concurrent}.
   */
  abstract class Composite extends Base implements Iterable<Action> {
    private final Iterable<? extends Action> actions;
    private final String                     summary;

    public Composite(String summary, Iterable<? extends Action> actions) {
      this.summary = summary;
      this.actions = checkNotNull(actions);
    }

    public String describe() {
      return format(
          "%s (%s, %s actions)",
          nonameIfNull(this.summary),
          this.getClass().getSimpleName(),
          unknownIfNegative(this.size())
      );
    }

    /**
     * This method may return negative number if {@code actions} is not a collection.
     */
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

    interface Factory {
      Action.Composite create(String summary, Iterable<? extends Action> actions);
    }

  }

  /**
   * A class that represents a collection of actions that should be executed concurrently.
   */
  class Concurrent extends Composite {
    public Concurrent(String summary, Iterable<? extends Action> actions) {
      super(summary, actions);
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    public enum Factory implements Composite.Factory {
      INSTANCE;

      @Override
      public Action.Concurrent create(String summary, Iterable<? extends Action> actions) {
        return new Action.Concurrent(summary, actions);
      }

      public String toString() {
        return "Concurrent";
      }
    }
  }

  /**
   * A class that represents a sequence of actions that should be executed one
   * after another.
   */
  class Sequential extends Composite {
    public Sequential(String summary, Iterable<? extends Action> actions) {
      super(summary, actions);
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    public enum Factory implements Composite.Factory {
      INSTANCE;

      @Override
      public Action.Sequential create(String summary, Iterable<? extends Action> actions) {
        return new Action.Sequential(summary, actions);
      }

      public String toString() {
        return "Sequential";
      }
    }
  }

  class ForEach<T> extends Base {
    private final Composite.Factory factory;
    private final Iterable<T>       dataSource;
    private final Block<T>[]        blocks;
    private final Action            action;


    public ForEach(Composite.Factory factory, Iterable<T> dataSource, Action action, Block<T>[] blocks) {
      this.factory = factory;
      this.dataSource = dataSource;
      this.action = checkNotNull(action);
      this.blocks = blocks;
    }

    public Iterable<T> getDataSource() {
      return dataSource;
    }

    public Block<T>[] getBlocks() {
      return blocks;
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    @Override
    public String describe() {
      return format("%s (%s, %s items) { %s }",
          this.getClass().getSimpleName(),
          this.factory,
          unknownIfNegative(sizeOrNegativeIfNonCollection(this.dataSource)),
          join(
              transform(
                  asList(blocks),
                  new Function<Block<T>, Object>() {
                    @Override
                    public Object apply(Block<T> block) {
                      return block.describe();
                    }
                  }
              ),
              ",")
      );
    }

    public Composite getElements() {
      final int[] counter = new int[] { 0 };
      Function<T, Action> func = new Function<T, Action>() {
        @Override
        public Action apply(final T t) {
          synchronized (counter) {
            return new Indexed(counter[0]++, action);
          }
        }
      };
      return this.factory.create(
          null,
          dataSource instanceof Collection
              ? Collections2.transform((Collection<T>) dataSource, func)
              : Iterables.transform(dataSource, func)
      );
    }

    public static class Tag extends Action.Base {
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
        return format("tag %d", index);
      }

      public int getIndex() {
        return index;
      }

      public <T> Action.Leaf toLeaf(final T data, final Block<T>[] blocks) {
        return new Action.Leaf() {
          @Override
          public void perform() {
            blocks[Tag.this.getIndex()].apply(data);
          }

          @Override
          public String describe() {
            return Tag.this.describe();
          }
        };
      }
    }

    public static class Indexed extends Action.Base {

      private final int    index;
      private final Action target;

      public Indexed(int index, Action target) {
        this.index = index;
        this.target = target;
      }

      public int getIndex() {
        return this.index;
      }

      public Action getTarget() {
        return this.target;
      }


      @Override
      public void accept(Visitor visitor) {
        visitor.visit(this);
      }

      @Override
      public String describe() {
        return String.format("%s[%s]", this.getClass().getSimpleName(), this.index);
      }
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
          this.getClass().getSimpleName(),
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
          this.getClass().getSimpleName(),
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
    void visit(Action.Composite action);

    /**
     * Visits an {@code action}
     *
     * @param action action to be visited by this object.
     */
    void visit(Action.Sequential action);

    /**
     * Visits an {@code action}
     *
     * @param action action to be visited by this object.
     */
    void visit(Action.Concurrent action);

    /**
     * Visits an {@code action}
     *
     * @param action action to be visited by this object.
     */
    void visit(Action.ForEach action);

    /**
     * Visits an {@code action}
     *
     * @param action action to be visited by this object.
     */
    void visit(Action.ForEach.Tag action);

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
        this.visit((Action.Composite) action);
      }

      @Override
      public void visit(Concurrent action) {
        this.visit((Action.Composite) action);
      }

      @Override
      public void visit(Action.ForEach action) {
        this.visit((Action) action);
      }

      @Override
      public void visit(Action.ForEach.Tag action) {
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
}
