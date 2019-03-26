package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.actions.cmd.linux.Cat;
import com.github.dakusui.actionunit.actions.cmd.linux.Echo;
import com.github.dakusui.actionunit.actions.cmd.linux.Ls;
import com.github.dakusui.actionunit.actions.cmd.linux.Mkdir;
import com.github.dakusui.actionunit.actions.cmd.linux.Rm;
import com.github.dakusui.actionunit.actions.cmd.linux.Touch;
import com.github.dakusui.actionunit.core.context.ContextFunctions;
import com.github.dakusui.processstreamer.core.process.Shell;

import java.util.function.Function;
import java.util.function.IntFunction;

public interface CommanderFactory {
  default Cat cat() {
    return init(new Cat(variablePlaceHolderFormatter()));
  }

  default Echo echo() {
    return init(new Echo(variablePlaceHolderFormatter()));
  }

  default Ls ls() {
    return init(new Ls(variablePlaceHolderFormatter()));
  }

  default Mkdir mkdir() {
    return init(new Mkdir(variablePlaceHolderFormatter()));
  }

  default Rm rm() {
    return init(new Rm(variablePlaceHolderFormatter()));
  }

  default Touch touch() {
    return init(new Touch(variablePlaceHolderFormatter()));
  }

  default Cmd cmd() {
    return init(new Cmd(variablePlaceHolderFormatter()));
  }

  @SuppressWarnings("unchecked")
  default <C extends Commander<?>> C init(C commander) {
    return (C) commander.shell(shell());
  }

  default Shell shell() {
    return Shell.LOCAL_SHELL;
  }

  default Function<String[], IntFunction<String>> variablePlaceHolderFormatter() {
    return ContextFunctions.DEFAULT_PLACE_HOLDER_FORMATTER;
  }
}
