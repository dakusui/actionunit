package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextFunction;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.utils.StableTemplatingUtils;

import java.util.ArrayList;
import java.util.Formattable;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public interface CommandLineComposer extends Function<String[], BiFunction<Context, Object[], String>>, Formattable {
  @Override
  default BiFunction<Context, Object[], String> apply(String[] variableNames) {
    return (context, argValues) -> StableTemplatingUtils.template(
        compose(context),
        StableTemplatingUtils.toMapping(this.parameterPlaceHolderFactory().apply(variableNames), argValues)
    );
  }

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format(format());
  }

  Function<String[], IntFunction<String>> parameterPlaceHolderFactory();

  String format();

  String compose(Context context);

  class Builder implements Cloneable {
    private Function<String[], IntFunction<String>> parameterPlaceHolderFactory;
    private List<String>                            knownVariableNames;
    private List<ContextFunction<String>>           tokens;

    public Builder(Function<String[], IntFunction<String>> parameterPlaceHolderFactory) {
      this.parameterPlaceHolderFactory = requireNonNull(parameterPlaceHolderFactory);
      this.tokens = new LinkedList<>();
      this.knownVariableNames = new LinkedList<>();
    }

    public Builder append(String text, boolean quoted) {
      requireNonNull(text);
      ContextFunction<String> func = ContextFunction.of(() -> text, c -> text);
      return append(func, quoted);
    }

    public Builder append(ContextFunction<String> func, boolean quoted) {
      if (quoted)
        func = quoteWithApostrophe(func);
      this.tokens.add(func);
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
      this.tokens.add(func);
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
        public Function<String[], IntFunction<String>> parameterPlaceHolderFactory() {
          return parameterPlaceHolderFactory;
        }

        @Override
        public String format() {
          return tokens.stream().map(Object::toString).collect(joining());
        }

        @Override
        public String compose(Context context) {
          return tokens.stream().map(each -> each.apply(context)).collect(joining());
        }
      };
    }

    public String[] knownVariables() {
      return knownVariableNames.toArray(new String[0]);
    }

    @Override
    public CommandLineComposer.Builder clone() {
      try {
        CommandLineComposer.Builder ret = (Builder) super.clone();
        ret.knownVariableNames = new ArrayList<>(this.knownVariableNames);
        ret.tokens = new ArrayList<>(this.tokens);
        return ret;
      } catch (CloneNotSupportedException e) {
        throw ActionException.wrap(e);
      }
    }

    @Override
    public String toString() {
      return String.format("Builder:%s(vars=%s)", tokens, knownVariableNames);
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
