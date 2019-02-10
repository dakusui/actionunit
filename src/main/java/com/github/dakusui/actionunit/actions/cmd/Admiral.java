package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ContextConsumer;
import com.github.dakusui.actionunit.core.ContextPredicate;
import com.github.dakusui.actionunit.core.StreamGenerator;
import com.github.dakusui.cmd.core.process.ProcessStreamer;
import com.github.dakusui.cmd.core.process.Shell;
import com.github.dakusui.printables.Printables;

import java.text.MessageFormat;
import java.util.function.Function;

import static com.github.dakusui.actionunit.core.ActionSupport.leaf;
import static java.util.Objects.requireNonNull;

public class Admiral {

  private Shell shell;

  public Admiral(Shell shell) {
    this.shell = shell;
  }

  public Action toActionWith(String variableName, CommandlineComposer commandLineComposer) {
    requireNonNull(commandLineComposer);
    return leaf(toContextConsumer(variableName, commandLineComposer));
  }

  public ContextConsumer toContextConsumer(String variableName, CommandlineComposer commandLineComposer) {
    return ContextConsumer.of(
        variableName,
        Printables.consumer(
            (Object[] s) ->
                new ProcessStreamer.Builder(shell, commandLineComposer.apply(s))
                    .build()
                    .stream()
                    .forEach(System.out::println))
            .describe(commandLineComposer.apply(new Object[0])));
  }


  public ContextPredicate toContextPredicate() {
    return null;
  }

  public StreamGenerator<String> toStreamGenerator() {
    return null;
  }

  interface Params {
    String[] paramNames();
  }

  interface CommandlineComposer extends Function<Object[], String> {
    @Override
    default String apply(Object[] params) {
      return MessageFormat.format(commandLineString(), (Object[]) params);
    }

    String commandLineString();
  }
}
