package com.github.dakusui.actionunit.compat.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.Utils;
import com.github.dakusui.actionunit.actions.Nested;
import com.github.dakusui.actionunit.compat.connectors.Sink;
import com.github.dakusui.actionunit.compat.connectors.Source;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.github.dakusui.actionunit.helpers.Checks.checkNotNull;
import static com.github.dakusui.actionunit.helpers.Utils.describe;
import static java.lang.String.format;

public class CompatWithBase<T> extends Nested.Base implements CompatWith<T> {
  private final Source<? extends T> dataSource;
  private final Sink<? super T>[]   dataSinks;

  public CompatWithBase(Source<? extends T> source, Action action, Sink<? super T>[] sinks) {
    super(action);
    dataSource = source;
    dataSinks = checkNotNull(sinks);
  }


  @Override
  public Source<T> getSource() {
    //noinspection unchecked
    return (Source<T>) dataSource;
  }

  public Sink<T>[] getSinks() {
    //noinspection unchecked
    return (Sink<T>[]) dataSinks;
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return format("%s (%s) {%s}",
        formatClassName(),
        describe(getSource()),
        String.join(",",
            Arrays.stream(getSinks()).map(
                Utils::describe).collect(Collectors.toList()))
    );
  }
}
