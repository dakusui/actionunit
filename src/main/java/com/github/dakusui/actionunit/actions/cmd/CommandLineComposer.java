package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.utils.StableTemplatingUtils;

import java.util.Formattable;
import java.util.Formatter;
import java.util.List;
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

  class Builder {
    private IntFunction<String> parameterPlaceHolderFactory;
    private List<String>        knownVariableNames;
    private StringBuilder       builder;

    public Builder(IntFunction<String> parameterPlaceHolderFactory, String... knownVariableNames) {
      this.parameterPlaceHolderFactory = requireNonNull(parameterPlaceHolderFactory);
      this.builder = new StringBuilder();
      this.knownVariableNames = asList(knownVariableNames);
    }

    public Builder append(String text) {
      this.builder.append(requireNonNull(text));
      return this;
    }

    public Builder appendVariable(String variableName) {
      return this.append(
          this.parameterPlaceHolderFactory.apply(
              requireArgument(
                  i -> i >= 0,
                  knownVariableNames.indexOf(requireNonNull(variableName)))));
    }

    public CommandLineComposer build() {
      return new CommandLineComposer() {
        @Override
        public IntFunction<String> parameterPlaceHolder() {
          return parameterPlaceHolderFactory;
        }

        @Override
        public String commandLineString() {
          return builder.toString();
        }
      };
    }

    public String[] knownVariables() {
      return knownVariableNames.toArray(new String[0]);
    }
  }
}
