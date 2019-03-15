package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.utils.StableTemplatingUtils;

import java.util.Formattable;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

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

  IntFunction<String> parameterPlaceHolder();

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
    return new CommandLineComposer() {
      @Override
      public String commandLineString() {
        return commandLineFormat;
      }

      @Override
      public IntFunction<String> parameterPlaceHolder() {
        return parameterIndex -> "{{" + parameterIndex + "}}";
      }
    };
  }

  class Builder {
    private IntFunction<String> parameterPlaceHolder;
    private List<String>        knownVariableNames;
    private List<String>        commandLine;

    public Builder(IntFunction<String> parameterPlaceHolder) {
      this.parameterPlaceHolder = requireNonNull(parameterPlaceHolder);
      this.knownVariableNames()
          .commandLine = new LinkedList<>();
    }

    public Builder knownVariableNames(String... knownVariableNames) {
      this.knownVariableNames = asList(knownVariableNames);
      return this;
    }

    public Builder add(String commandLine) {
      this.commandLine.add(requireNonNull(commandLine));
      return this;
    }

    public Builder addParameter(String variableName) {
      requireNonNull(variableName);
      this.commandLine.add(this.parameterPlaceHolder.apply(knownVariableNames.indexOf(variableName)));
      return this;
    }

    public CommandLineComposer build() {
      return new CommandLineComposer() {
        @Override
        public IntFunction<String> parameterPlaceHolder() {
          return parameterPlaceHolder;
        }

        @Override
        public String commandLineString() {
          return commandLine.stream().collect(Collectors.joining(" "));
        }
      };
    }
  }
}
