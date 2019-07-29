package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.actions.cmd.unix.Cat;
import com.github.dakusui.actionunit.actions.cmd.unix.Cmd;
import com.github.dakusui.actionunit.actions.cmd.unix.Curl;
import com.github.dakusui.actionunit.actions.cmd.unix.Echo;
import com.github.dakusui.actionunit.actions.cmd.unix.Git;
import com.github.dakusui.actionunit.actions.cmd.unix.Ls;
import com.github.dakusui.actionunit.actions.cmd.unix.Mkdir;
import com.github.dakusui.actionunit.actions.cmd.unix.Rm;
import com.github.dakusui.actionunit.actions.cmd.unix.Scp;
import com.github.dakusui.actionunit.actions.cmd.unix.SshOptions;
import com.github.dakusui.actionunit.actions.cmd.unix.Touch;
import com.github.dakusui.actionunit.core.context.ContextFunctions;
import com.github.dakusui.processstreamer.core.process.Shell;

import java.io.Serializable;
import java.util.function.Function;
import java.util.function.IntFunction;

import static java.lang.String.format;

public interface CommanderInitializer extends Serializable{
  CommanderInitializer DEFAULT_INSTANCE = new CommanderInitializer() {
  };

  default void init(Commander<?> commander) {
    throw new UnsupportedOperationException(
        format("%s is not a supported commander class", commander.getClass().getCanonicalName()));
  }

  default void init(Echo echo) {
    echo.command("/bin/echo");
    initialize(echo);
  }

  default void init(Cat cat) {
    cat.command("cat");
    initialize(cat);
  }

  default void init(Ls ls) {
    ls.command("ls");
    initialize(ls);
  }

  default void init(Mkdir mkdir) {
    mkdir.command("mkdir");
    initialize(mkdir);
  }

  default void init(Rm rm) {
    rm.command("rm");
    initialize(rm);
  }

  default void init(Touch touch) {
    touch.command("touch");
    initialize(touch);
  }

  default void init(Scp scp) {
    scp.command("scp");
    initialize(scp);
    scp.options(sshOptions());
  }

  default void init(Curl curl) {
    curl.command("curl");
    initialize(curl);
  }

  default void init(Git.GitBase<?> git) {
    git.command("git");
    initialize(git);
  }

  default void init(Cmd cmd) {
    initialize(cmd);
  }

  default Shell shell() {
    return Shell.LOCAL_SHELL;
  }

  default void initialize(Commander commander) {
    commander.shell(shell());
  }

  default SshOptions sshOptions() {
    return new SshOptions.Builder()
        .disableStrictHostkeyChecking()
        .disablePasswordAuthentication()
        .build();
  }

  default Function<String[], IntFunction<String>> variablePlaceHolderFormatter() {
    return ContextFunctions.DEFAULT_PLACE_HOLDER_FORMATTER;
  }
}
