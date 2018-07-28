package com.github.dakusui.actionunit.n.actions;

import com.github.dakusui.actionunit.n.exceptions.ActionException;
import com.github.dakusui.actionunit.n.core.Action;

import java.util.Formatter;

import static com.github.dakusui.actionunit.utils.Checks.requireArgument;
import static com.github.dakusui.actionunit.n.core.ActionSupport.named;
import static java.util.Objects.requireNonNull;

public interface Attempt extends Action {
  @Override
  default void accept(Visitor visitor) {
    visitor.visit(this);
  }

  Action perform();

  Action recover();

  Action ensure();

  Class<? extends Throwable> targetExceptionClass();

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format("attempt");
  }

  class Builder extends Action.Builder<Attempt> {
    private       Class<? extends Throwable> targetExceptionClass = Exception.class;
    private final Action                     perform;
    private       Action                     recover              = Named.of("recover",
        Leaf.of(
            $ -> {
              if ($.thrownException().isPresent()) {
                throw ActionException.wrap($.thrownException().get());
              }
            }));
    private       Action                     ensure               = Named.of("ensure",
        Leaf.NOP
    );

    public Builder(Action perform) {
      this.perform = requireNonNull(perform);
    }

    public Builder recover(Class<? extends Throwable> targetExceptionClass, Action recover) {
      this.recover = Named.of("recover", requireNonNull(recover));
      this.targetExceptionClass = requireArgument(
          t -> Exception.class.isAssignableFrom(t.getClass()),
          requireNonNull(targetExceptionClass)
      );
      return this;
    }

    public Action ensure(Action ensure) {
      this.ensure = named("ensure", requireNonNull(ensure));
      return this.$();
    }

    public Attempt build() {
      return new Attempt() {

        @Override
        public Action perform() {
          return perform;
        }

        @Override
        public Action recover() {
          return recover;
        }

        public Class<? extends Throwable> targetExceptionClass() {
          return targetExceptionClass;
        }

        @Override
        public Action ensure() {
          return ensure;
        }
      };
    }
  }
}
