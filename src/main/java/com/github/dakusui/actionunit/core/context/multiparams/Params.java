package com.github.dakusui.actionunit.core.context.multiparams;

import com.github.dakusui.actionunit.actions.ContextVariable;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.dakusui.actionunit.utils.Checks.requireArgument;
import static com.github.dakusui.printables.PrintableFunctionals.isKeyOf;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

/**
 * An interface that represents multiple-parameters as a single object for functions/predicates/consumers
 * that take multiple parameters in the actionunit's model.
 *
 * This interface is instantiated during the action processing procedure launched by
 * calling {@link com.github.dakusui.actionunit.core.Action#accept(Action.Visitor)}.
 *
 * @see Action#accept(Action.Visitor)
 * @see Action.Visitor
 */
public interface Params {

  /**
   * Returns a {@link Context} object from which this object is created.
   *
   * @return A context.
   */
  Context context();

  List<ContextVariable> parameters();

  /**
   * Throws an {@link java.util.NoSuchElementException}, if this object doesn't
   * hold the given `contextVariable` in it.
   *
   * @param contextVariable A variable to resolve its current value.
   * @param <T>             The type of the variable.
   * @return The resolved value.
   */
  <T> T valueOf(ContextVariable contextVariable);

  /**
   * Creates an instance of {@link Params} interface.
   *
   * @param context          A context on which the variables are evaluated.
   * @param contextVariables Context variables.
   * @return An instance of this interface.
   */
  static Params create(Context context, ContextVariable... contextVariables) {
    return new Params() {
      final Map<ContextVariable, Object> values = new LinkedHashMap<ContextVariable, Object>() {{
        for (ContextVariable each : contextVariables) {
          this.put(each, each.resolve(context));
        }
      }};

      @Override
      public Context context() {
        return context;
      }

      @Override
      public List<ContextVariable> parameters() {
        return asList(contextVariables);
      }

      @SuppressWarnings("unchecked")
      @Override
      public <T> T valueOf(ContextVariable parameterName) {
        return (T) values.get(requireArgument(isKeyOf(values), requireNonNull(parameterName)));
      }

      @Override
      public String toString() {
        return parameters().stream() // printing purpose
            .map(each -> format("%s=%s", each, valueOf(each)))
            .collect(joining(",", "[", "]"));
      }
    };
  }
}
