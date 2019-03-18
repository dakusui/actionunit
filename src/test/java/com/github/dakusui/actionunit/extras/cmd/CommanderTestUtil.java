package com.github.dakusui.actionunit.extras.cmd;

import com.github.dakusui.actionunit.actions.cmd.CommandLineComposer;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;

import java.util.function.Function;
import java.util.function.IntFunction;

import static java.util.Objects.requireNonNull;

public enum CommanderTestUtil {
  ;

  static void performAndReport(Action action) {
    ReportingActionPerformer.create().performAndReport(action, Writer.Std.OUT);
  }

  public static CommandLineComposer byVariableName(
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

  public static CommandLineComposer byVariableName(
      String commandLineFormat,
      String... knownVariableNames) {
    requireNonNull(commandLineFormat);
    return byVariableName(commandLineFormat, v -> String.format("{{%s}}", v), knownVariableNames);
  }

  public static CommandLineComposer byIndex(
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
}
