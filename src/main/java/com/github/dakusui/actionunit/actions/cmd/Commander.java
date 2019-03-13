package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer;

import java.util.stream.Stream;

import static com.github.dakusui.processstreamer.core.process.ProcessStreamer.Checker.createCheckerForExitCode;

public interface Commander<C extends Commander<?>> {

  C env(String varname, String varvalue);

  C stdin(Stream<String> stream);

  default Action toAction() {
    return toActionWith(createCheckerForExitCode(0));
  }

  Action toActionWith(ProcessStreamer.Checker checker);

  default ContextConsumer toContextConsumer() {
    return toContextConsumerWith(createCheckerForExitCode(0));
  }

  ContextConsumer toContextConsumerWith(ProcessStreamer.Checker checker);
}
