package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.actions.ContextVariable;
import com.github.dakusui.actionunit.actions.RetryOption;
import com.github.dakusui.actionunit.actions.cmd.unix.*;
import com.github.dakusui.actionunit.core.context.ContextFunctions;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer;
import com.github.dakusui.processstreamer.core.process.Shell;

import java.util.function.Function;
import java.util.function.IntFunction;

import static com.github.dakusui.processstreamer.core.process.ProcessStreamer.Checker.createCheckerForExitCode;

public interface CommanderConfig {
  CommanderConfig DEFAULT = new CommanderConfig() {
  };

  default void setCommandNameFor(Echo echo) {
    echo.commandName("/bin/echo");
  }

  default void setCommandNameFor(Cat cat) {
    cat.commandName("cat");
  }

  default void setCommandNameFor(Ls ls) {
    ls.commandName("ls");
  }

  default void setCommandNameFor(Mkdir mkdir) {
    mkdir.commandName("mkdir");
  }

  default void setCommandNameFor(Rm rm) {
    rm.commandName("rm");
  }

  default void setCommandNameFor(Touch touch) {
    touch.commandName("touch");
  }

  default void setCommandNameFor(Scp scp) {
    scp.commandName("scp");
    scp.options(sshOptions());
  }

  default void setCommandNameFor(Curl curl) {
    curl.commandName("curl");
  }

  default void setCommandNameFor(Git.GitBase<?> git) {
    git.commandName("git");
  }

  default void setCommandNameFor(Cmd cmd) {
  }

  default Shell shell() {
    return Shell.LOCAL_SHELL;
  }


  default SshOptions sshOptions() {
    return new SshOptions.Builder()
        .disableStrictHostkeyChecking()
        .disablePasswordAuthentication()
        .build();
  }

  default RetryOption retryOption() {
    return RetryOption.none();
  }

  default Function<ContextVariable[], IntFunction<String>> variablePlaceHolderFormatter() {
    return ContextFunctions.DEFAULT_PLACE_HOLDER_FORMATTER;
  }

  default ProcessStreamer.Checker checker() {
    return createCheckerForExitCode(0);
  }
}
