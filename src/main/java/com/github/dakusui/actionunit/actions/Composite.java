package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Action;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.Iterator;

import static com.github.dakusui.actionunit.Utils.unknownIfNegative;
import static com.google.common.base.Preconditions.checkNotNull;
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
      if (!(object instanceof Composite.Base)) {
        return false;
      }
      Composite.Base another = (Base) object;
      return typeName.equals(another.typeName) && Iterables.elementsEqual(actions, another.actions);
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
