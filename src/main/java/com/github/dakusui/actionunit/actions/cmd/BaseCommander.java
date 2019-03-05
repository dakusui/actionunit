package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.actions.cmd.compat.Cmd;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.StreamGenerator;
import com.github.dakusui.actionunit.utils.Checks;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer;
import com.github.dakusui.processstreamer.core.process.Shell;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.actions.cmd.CommanderUtils.quoteWithSingleQuotesForShell;
import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static com.github.dakusui.actionunit.utils.Checks.*;
import static java.util.Objects.requireNonNull;

public class BaseCommander<C extends BaseCommander<C>> extends Commander<C> {
  private CommandLineComposer commandLineComposer;
  private String[]            variableNames;

  public BaseCommander(Shell shell) {
    super(shell);
  }

  public C command(String commandLineFormat, String... variableNames) {
    requireNonNull(commandLineFormat);
    return command(() -> commandLineFormat, variableNames);
  }

  @SuppressWarnings("unchecked")
  public C command(CommandLineComposer commandLineComposer, String... variableNames) {
    this.commandLineComposer = requireNonNull(commandLineComposer);
    this.variableNames = variableNames;
    return (C) this;
  }

  @Override
  protected CommandLineComposer commandLineComposer() {
    return this.commandLineComposer;
  }

  @Override
  protected String[] variableNames() {
    return this.variableNames;
  }

}
