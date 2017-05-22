package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.AutocloseableIterator;
import com.github.dakusui.actionunit.Autocloseables;
import com.github.dakusui.actionunit.Utils;

import java.util.Collection;

import static com.github.dakusui.actionunit.Utils.unknownIfNegative;
import static com.github.dakusui.actionunit.Checks.checkNotNull;
import static java.lang.String.format;

/**
 * An interface to represent an action which executes its members.
 * The manner in which those members should be executed is left to sub-interfaces
 * of this.
 *
 * @see Sequential
 * @see Concurrent
 */
public interface Composite extends Action, AutocloseableIterator.Factory<Action> {
  /**
   * Returns number of actions that this object has if they are given as a {@link Collection}.
   * Otherwise, for instance actions are given as {@link Iterable}, {@code -1}
   * will be returned.
   */
  int size();

  /**
   * A skeletal implementation for composite actions, such as {@link Sequential.Impl} or {@link Concurrent.Base}.
   */
  abstract class Base extends ActionBase implements Composite {
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
    public int hashCode() {
      return actions.hashCode();
    }

    @Override
    public boolean equals(Object object) {
      if (!(object instanceof Composite)) {
        return false;
      }
      Composite another = (Composite) object;
      return getClass().equals(another.getClass()) && Utils.elementsEqual(actions, another);
    }

    @Override
    public AutocloseableIterator<Action> iterator() {
      //noinspection unchecked
      return (AutocloseableIterator<Action>) Autocloseables.autocloseable(this.actions.iterator());
    }
  }

  interface Factory {
    Composite create(Iterable<? extends Action> actions);
  }
}
