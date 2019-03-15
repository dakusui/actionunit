package com.github.dakusui.actionunit.actions.cmd;

import java.util.function.BiFunction;

public enum CommandLineComposerFactory implements BiFunction<String, String[], CommandLineComposer> {
  BY_INDEX {
    public CommandLineComposer apply(String commandLineFormat, String[] variableNames) {
      return CommandLineComposer.byVariableName(commandLineFormat);
    }
  },
  BY_KNOWN_VARIABLE_NAME {
    public CommandLineComposer apply(String commandLineFormat, String[] variableNames) {
      return CommandLineComposer.byIndex(commandLineFormat);
    }
  };
}
