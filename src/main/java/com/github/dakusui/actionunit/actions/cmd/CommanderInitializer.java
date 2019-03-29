package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.actions.cmd.linux.Cat;
import com.github.dakusui.actionunit.actions.cmd.linux.Cmd;
import com.github.dakusui.actionunit.actions.cmd.linux.Curl;
import com.github.dakusui.actionunit.actions.cmd.linux.Echo;
import com.github.dakusui.actionunit.actions.cmd.linux.Git;
import com.github.dakusui.actionunit.actions.cmd.linux.Ls;
import com.github.dakusui.actionunit.actions.cmd.linux.Mkdir;
import com.github.dakusui.actionunit.actions.cmd.linux.Rm;
import com.github.dakusui.actionunit.actions.cmd.linux.Scp;
import com.github.dakusui.actionunit.actions.cmd.linux.Touch;
import com.github.dakusui.actionunit.core.context.ContextFunctions;
import com.github.dakusui.processstreamer.core.process.Shell;

import java.util.function.Function;
import java.util.function.IntFunction;

import static java.lang.String.format;

public interface CommanderInitializer {
  CommanderInitializer INSTANCE = () -> ContextFunctions.DEFAULT_PLACE_HOLDER_FORMATTER;

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

  Function<String[], IntFunction<String>> variablePlaceHolderFormatter();
}
