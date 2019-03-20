package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextFunction;
import com.github.dakusui.actionunit.utils.StableTemplatingUtils;

import java.util.Formattable;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.IntFunction;

import static com.github.dakusui.actionunit.utils.Checks.requireArgument;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public interface CommandLineComposer extends BiFunction<Context, Object[], String>, Formattable {
  @Override
  default String apply(Context context, Object[] argValues) {
    return StableTemplatingUtils.template(
        compose(context),
        StableTemplatingUtils.toMapping(this.parameterPlaceHolderFactory(), argValues)
    );
  }

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format(commandLineString());
  }

  IntFunction<String> parameterPlaceHolderFactory();

  String commandLineString();

  String compose(Context context);

  class Builder {
    private IntFunction<String>           parameterPlaceHolderFactory;
    private List<String>                  knownVariableNames;
    private List<ContextFunction<String>> builder;

    public Builder(IntFunction<String> parameterPlaceHolderFactory, String... knownVariableNames) {
      this.parameterPlaceHolderFactory = requireNonNull(parameterPlaceHolderFactory);
      this.builder = new LinkedList<>();
      this.knownVariableNames = asList(knownVariableNames);
    }

    public Builder append(String text) {
      requireNonNull(text);
      this.builder.add(ContextFunction.of(() -> text, c -> text));
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
        public IntFunction<String> parameterPlaceHolderFactory() {
          return parameterPlaceHolderFactory;
        }

        @Override
        public String commandLineString() {
          return builder.stream().map(Object::toString).collect(joining());
        }

        @Override
        public String compose(Context context) {
          return builder.stream().map(each -> each.apply(context)).collect(joining());
        }
      };
    }

    public String[] knownVariables() {
      return knownVariableNames.toArray(new String[0]);
    }
  }
}
