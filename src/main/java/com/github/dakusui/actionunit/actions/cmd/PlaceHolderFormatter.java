package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.actions.ContextVariable;

import java.util.function.Function;
import java.util.function.IntFunction;

public interface PlaceHolderFormatter extends Function<ContextVariable[], IntFunction<String>> {

  /**
   *
   */
  PlaceHolderFormatter DEFAULT_PLACE_HOLDER_FORMATTER = variables -> i -> String.format("{{%s}}", i);

  /**
   *
   */
  PlaceHolderFormatter PLACE_HOLDER_FORMATTER_BY_NAME = variables -> i -> String.format("{{%s}}", variables[i].variableName());
}
