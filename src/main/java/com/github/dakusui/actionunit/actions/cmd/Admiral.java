package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ContextConsumer;
import com.github.dakusui.actionunit.core.ContextFunctions.Params;
import com.github.dakusui.actionunit.core.ContextPredicate;
import com.github.dakusui.actionunit.core.StreamGenerator;
import com.github.dakusui.cmd.core.process.ProcessStreamer;
import com.github.dakusui.cmd.core.process.Shell;

import java.text.MessageFormat;
import java.util.function.Function;

import static com.github.dakusui.actionunit.core.ActionSupport.leaf;
import static java.util.Objects.requireNonNull;

public class Admiral {

  private Shell shell;

  public Admiral(Shell shell) {
    this.shell = shell;
  }

  public Action toActionWith(CommandlineComposer commandLineComposer, String... variableNames) {
    requireNonNull(commandLineComposer);
    return leaf(toContextConsumer(commandLineComposer, variableNames));
  }

  public ContextConsumer toContextConsumer(CommandlineComposer commandLineComposer, String... variableNames) {
    return new ContextConsumer.Builder(variableNames)
        .with((Params params) -> createProcessStreamerBuilder(commandLineComposer, params)
            .build()
            .stream()
            .forEach(System.out::println));
  }

  public ContextPredicate toContextPredicate() {
    return null;
  }

  private ProcessStreamer.Builder createProcessStreamerBuilder(CommandlineComposer commandLineComposer, Params params) {
    return new ProcessStreamer.Builder(shell, commandLineComposer.apply(params.paramNames().stream().map(params::valueOf).toArray()));
  }



  public StreamGenerator<String> toStreamGenerator() {
    return null;
  }

  interface CommandlineComposer extends Function<Object[], String> {
    @Override
    default String apply(Object[] params) {
      return MessageFormat.format(commandLineString(), (Object[]) params);
    }

    String commandLineString();
  }
}
