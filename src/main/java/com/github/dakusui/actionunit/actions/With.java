package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.connectors.Source;
import com.google.common.base.Function;

import static com.github.dakusui.actionunit.Utils.describe;
import static com.github.dakusui.actionunit.Utils.transform;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.join;

/**
 *
 * @param <T>
 */
public interface With<T> extends Action {

  Source<T> source();

  Sink<T>[] getSinks();

  Action getAction();

  class Base<T> extends ActionBase implements com.github.dakusui.actionunit.actions.With<T> {
    final Sink<T>[] sinks;
    final Source<T> source;
    final Action    action;

    public Base(Source<T> source, Action action, Sink<T>[] sinks) {
      this.source = checkNotNull(source);
      this.action = checkNotNull(action);
      this.sinks = checkNotNull(sinks);
    }


    @Override
    public Source<T> source() {
      return this.source;
    }

    public Sink<T>[] getSinks() {
      return sinks;
    }

    @Override
    public Action getAction() {
      return this.action;
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    @Override
    public String toString() {
      return format("%s (%s) {%s}",
          formatClassName(),
          describe(this.source()),
          join(transform(
              asList(this.getSinks()),
              new Function<Sink<T>, Object>() {
                @Override
                public Object apply(Sink<T> sink) {
                  return describe(sink);
                }
              }),
              ","));
    }
  }

}
