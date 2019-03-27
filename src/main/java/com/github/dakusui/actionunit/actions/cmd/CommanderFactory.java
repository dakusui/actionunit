package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.core.context.ContextFunctions;
import com.github.dakusui.processstreamer.core.process.Shell;

import java.util.function.Function;
import java.util.function.IntFunction;

import static java.util.Objects.requireNonNull;

public interface CommanderFactory {
  default <C extends Commander<?>> C createCommander(Function<Function<String[], IntFunction<String>>, C> constructor) {
    return init(requireNonNull(constructor).apply(variablePlaceHolderFormatter()));
  }

  default Shell shell() {
    return Shell.LOCAL_SHELL;
  }

  default Function<String[], IntFunction<String>> variablePlaceHolderFormatter() {
    return ContextFunctions.DEFAULT_PLACE_HOLDER_FORMATTER;
  }

  @SuppressWarnings("unchecked")
  default <C extends Commander<?>> C init(C commander) {
    return (C) commander.shell(shell());
  }
}
