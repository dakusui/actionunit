package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.utils.StableTemplatingUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;

import static com.github.dakusui.actionunit.utils.Checks.requireArgument;
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

  class TokenBuilder {
    private final IntFunction<String> parameterPlaceHolderFactory;
    private final StringBuilder builder = new StringBuilder();
    private final List<String> knownVariableNames;

    TokenBuilder(IntFunction<String> parameterPlaceHolderFactory, String... knownVariableNames) {
      this.knownVariableNames = Arrays.asList(knownVariableNames);
      this.parameterPlaceHolderFactory = parameterPlaceHolderFactory;
    }

    TokenBuilder addText(String text) {
      builder.append(text);
      return this;
    }

    TokenBuilder addVariable(String variableName) {
      return this.addText(
          this.parameterPlaceHolderFactory.apply(
              requireArgument(
                  i -> i >=0,
                  this.knownVariableNames.indexOf(requireNonNull(variableName)))));
    }
  }

  class Builder {
    private IntFunction<String> parameterPlaceHolder;
    private List<String> knownVariableNames;
    private List<String> commandLine;

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

    public Builder addVariable(String variableName) {
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
          return String.join(" ", commandLine);
        }
      };
    }
  }
}
