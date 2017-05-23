package com.github.dakusui.actionunit.compat.actions;

import com.github.dakusui.actionunit.actions.Nested;
import com.github.dakusui.actionunit.compat.connectors.Sink;
import com.github.dakusui.actionunit.compat.connectors.Source;

/**
 * @param <T> Type of the value with which child {@code Action} is executed.
 */
public interface CompatWith<T> extends Nested {

  Source<T> getSource();

  Sink<T>[] getSinks();

}
