package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextFunction;
import com.github.dakusui.actionunit.utils.StableTemplatingUtils;

import java.util.Formattable;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

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

    public Builder(IntFunction<String> parameterPlaceHolderFactory) {
      this.parameterPlaceHolderFactory = requireNonNull(parameterPlaceHolderFactory);
      this.builder = new LinkedList<>();
      this.knownVariableNames = new LinkedList<>();
    }

    public Builder append(String text, boolean quoted) {
      requireNonNull(text);
      ContextFunction<String> func = ContextFunction.of(() -> text, c -> text);
      if (quoted)
        func = quoteWithApostrophe(func);
      this.builder.add(func);
      return this;
    }

    public Builder appendVariable(String variableName, boolean quoted) {
      ContextFunction<String> func = ContextFunction.of(
          () -> "${" + variableName + "}",
          c -> c.valueOf(variableName)
      );
      if (quoted) {
        func = quoteWithApostrophe(func);
      }
      this.builder.add(func);
      return this.declareVariable(variableName);
    }

    public Builder declareVariable(String variableName) {
      if (!knownVariableNames.contains(variableName))
        this.knownVariableNames.add(variableName);
      return this;
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

    private static ContextFunction<String> quoteWithApostrophe(ContextFunction<String> func) {
      return func.andThen(new Function<String, String>() {
        @Override
        public String apply(String s) {
          return CommanderUtils.quoteWithApostropheForShell(s);
        }
        @Override
        public String toString() {
          return "quoteWith[']";
        }
      });
    }
  }
}
