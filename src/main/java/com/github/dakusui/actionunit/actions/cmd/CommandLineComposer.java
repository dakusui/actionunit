package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.utils.StableTemplatingUtils;

import java.util.Formattable;
import java.util.Formatter;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

@FunctionalInterface
public interface CommandLineComposer extends Function<Object[], String>, Formattable {
  @Override
  default String apply(Object[] argValues) {
    return StableTemplatingUtils.template(
        commandLineString(),
        StableTemplatingUtils.toMapping(this::parameterPlaceHolderFor, argValues)
    );
  }

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format(commandLineString());
  }

  default String parameterPlaceHolderFor(int parameterIndex) {
    return "{{" + parameterIndex + "}}";
  }

  String commandLineString();

  static CommandLineComposer create(
      String commandLineFormat,
      Function<String, String> parameterPlaceHolderFactory,
      String... variableNames) {
    requireNonNull(commandLineFormat);
    return new CommandLineComposer() {
      @Override
      public String commandLineString() {
        return commandLineFormat;
      }

      @Override
      public String parameterPlaceHolderFor(int parameterIndex) {
        return parameterPlaceHolderFactory.apply(variableNames[parameterIndex]);
      }
    };
  }

  static CommandLineComposer create(
      String commandLineFormat,
      String... variableNames) {
    requireNonNull(commandLineFormat);
    return create(commandLineFormat, v -> String.format("{{%s}}", v), variableNames);
  }
}
