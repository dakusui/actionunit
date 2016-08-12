package com.github.dakusui.actionunit;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class TestAction<I, O extends TestAction.Output> extends Block.Base<I> implements Block<I>, Action.With<O> {
  public interface Output {
    class Text implements Output {
      private final String value;

      Text(String value) {
        this.value = value;
      }

      String value() {
        return this.value;
      }
    }
  }

  O outputOnExecution = null;

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String describe() {
    return this.getClass().getCanonicalName();
  }

  @Override
  public O value() {
    checkNotNull(this.outputOnExecution);
    return this.outputOnExecution;
  }

  @Override
  public Block<O>[] getBlocks() {
    //noinspection unchecked
    return new Block[] {
        new Block.Base<O>() {
          @Override
          public void apply(O input, Object... outer) {
            TestAction.this.verify(input, outer);
          }
        }
    };
  }

  @Override
  public Action getAction() {
    return new Tag(0);
  }

  @Override
  public void apply(I input, Object... outer) {
    this.outputOnExecution = checkNotNull(this.execute(input, outer));
  }

  abstract protected O execute(I input, Object... other);

  abstract protected void verify(O input, Object... outer);
}
