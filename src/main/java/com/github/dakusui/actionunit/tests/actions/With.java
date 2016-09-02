package com.github.dakusui.actionunit.tests.actions;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Context;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.connectors.Source;
import com.google.common.base.Function;

import static com.github.dakusui.actionunit.Utils.describe;
import static com.github.dakusui.actionunit.Utils.transform;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.join;

/**
 * Created by hiroshi on 9/1/16.
 */
public interface With<T> extends Action {

  Source<T> source();

  Sink<T>[] getSinks();

  Action getAction();

  class Base<T> extends ActionBase implements com.github.dakusui.actionunit.tests.actions.With<T> {
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

  class Tag extends ActionBase {
    private final int index;

    public Tag(int i) {
      checkArgument(i >= 0, "Index must not be negative. (%s)", i);
      this.index = i;
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    @Override
    public String toString() {
      return format("Tag(%d)", index);
    }

    public int getIndex() {
      return index;
    }

    public <T> Leaf toLeaf(final Source<T> source, final Sink<T>[] sinks, final Context context) {
      return new Leaf() {
        @Override
        public void perform() {
          checkState(
              Tag.this.getIndex() < sinks.length,
              "Insufficient number of block(s) are given. (block[%s] was referenced, but only %s block(s) were given.",
              Tag.this.getIndex(),
              sinks.length
          );
          sinks[Tag.this.getIndex()].apply(source.apply(context), context);
        }

        @Override
        public String toString() {
          return Tag.this.toString();
        }
      };
    }
  }
}
