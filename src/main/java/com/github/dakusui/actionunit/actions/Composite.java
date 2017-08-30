package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.InternalUtils;

import java.util.Collection;
import java.util.Iterator;

import static com.github.dakusui.actionunit.helpers.Checks.checkNotNull;
import static com.github.dakusui.actionunit.helpers.InternalUtils.unknownIfNegative;
import static java.lang.String.format;

/**
 * An interface to represent an action which executes its members.
 * The manner in which those members should be executed is left to sub-interfaces
 * of this.
 *
 * @see Sequential
 * @see Concurrent
 */
public interface Composite extends Action, Iterable<Action> {
  /**
   * Returns a number of elements to be handled by this object.
   *
   * @return number of actions that this object has if they are given as a {@link Collection}.
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

    public Base(int id, String typeName, Iterable<? extends Action> actions) {
      super(id);
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
      return getClass().equals(another.getClass()) && InternalUtils.elementsEqual(actions, another);
    }

    @Override
    public Iterator<Action> iterator() {
      //noinspection unchecked
      return (Iterator<Action>) this.actions.iterator();
    }
  }

  interface Factory {
    Composite create(int id, Iterable<? extends Action> actions);
  }
}
