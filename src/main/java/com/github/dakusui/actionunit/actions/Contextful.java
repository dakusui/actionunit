package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;

public interface Contextful<V> extends Action {
  abstract class Builder<B extends Builder<B, A, V>, A extends Contextful<V>, V> extends Action.Builder<A> {
  }
}
