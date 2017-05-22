package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Context;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.connectors.Source;

import static com.github.dakusui.actionunit.Autocloseables.transform;
import static com.github.dakusui.actionunit.Checks.checkArgument;
import static com.github.dakusui.actionunit.Checks.checkState;
import static com.github.dakusui.actionunit.Utils.range;
import static java.lang.String.format;

/**
 * A tag action. This class is used with {@link With} action and replaced at runtime
 * with a {@link Sink} object held by the {@code With} action.
 *
 * @see With
 * @see Sink
 */
public class Tag extends ActionBase {
  private final int index;

  public Tag(int i) {
    checkArgument(i >= 0, "Index must not be negative. (%s)", i);
    this.index = i;
  }

  public int getIndex() {
    return index;
  }

  public <T> Leaf toLeaf(final Source<T> source, final Sink<T>[] sinks, final Context context) {
    //noinspection unchecked
    return new TagRunner(sinks, source, context);
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return format("Tag(%d)", index);
  }

  @Override
  public int hashCode() {
    return this.index;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof Tag)) {
      return false;
    }
    Tag another = (Tag) object;
    return this.index == another.index;
  }

  public static Action createFromRange(int from, int to) {
    return Sequential.Factory.INSTANCE.create(
        transform(range(from, to), Tag::new));
  }

  private class TagRunner<T> extends Leaf implements Synthesized {
    private final Sink<T>[] sinks;
    private final Source<T> source;
    private final Context   context;

    public TagRunner(Sink<T>[] sinks, Source<T> source, Context context) {
      this.sinks = sinks;
      this.source = source;
      this.context = context;
    }

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
    public Action getParent() {
      return Tag.this;
    }
  }
}
