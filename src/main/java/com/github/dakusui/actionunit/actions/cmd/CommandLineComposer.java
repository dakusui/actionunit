package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.utils.StableTemplatingUtils;

import java.util.Formattable;
import java.util.Formatter;
import java.util.function.Function;
import java.util.function.IntFunction;

import static java.util.Objects.requireNonNull;

@FunctionalInterface
public interface CommandLineComposer extends Function<Object[], String>, Formattable {
  @Override
  default String apply(Object[] argValues) {
    return StableTemplatingUtils.template(
        commandLineString(),
        StableTemplatingUtils.toMapping(this.parameterPlaceHolder(), argValues)
    );
  }

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format(commandLineString());
  }

  default IntFunction<String> parameterPlaceHolder() {
    return parameterIndex -> "{{" + parameterIndex + "}}";
  }

  String commandLineString();

  static CommandLineComposer byVariableName(
      String commandLineFormat,
      Function<String, String> parameterPlaceHolderFactory,
      String... knownVariableNames) {
    requireNonNull(commandLineFormat);
    return new CommandLineComposer() {
      @Override
      public String commandLineString() {
        return commandLineFormat;
      }

      @Override
      public IntFunction<String> parameterPlaceHolder() {
        return parameterIndex -> parameterPlaceHolderFactory.apply(knownVariableNames[parameterIndex]);
      }
    };
  }

  static CommandLineComposer byVariableName(
      String commandLineFormat,
      String... knownVariableNames) {
    requireNonNull(commandLineFormat);
    return byVariableName(commandLineFormat, v -> String.format("{{%s}}", v), knownVariableNames);
  }

  static CommandLineComposer byIndex(
      String commandLineFormat
  ) {
    return () -> commandLineFormat;
  }
}
