package com.github.dakusui.actionunit.core;

import com.github.dakusui.actionunit.actions.ValueHolder;
import com.github.dakusui.actionunit.core.generator.ActionGenerator;

import static java.util.Objects.requireNonNull;

@FunctionalInterface
public interface ActionFactory extends Context {
  default Action create() {
    return create(this);
  }

  @Override
  default int generateId() {
    return 0;
  }

  Bean bean();

  default Action create(Context self) {
    return bean().actionGenerator().apply(ValueHolder.empty()).apply(self);
  }

  class Bean extends Context.Bean {
    private final ActionGenerator<?> actionGenerator;

    public <I> Bean(ActionGenerator<I> actionGenerator) {
      this.actionGenerator = requireNonNull(actionGenerator);
    }

    @SuppressWarnings("unchecked")
    protected <I> ActionGenerator<I> actionGenerator() {
      return (ActionGenerator<I>) this.actionGenerator;
    }

    ;
  }

  static <I> ActionFactory of(ActionGenerator<I> actionGenerator) {
    return () -> new Bean(actionGenerator);
  }

  static ActionFactory of(Action action) {
    return () -> new Bean(v -> c -> action);
  }
}
