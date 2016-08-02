package com.github.dakusui.actionunit;

import com.google.common.base.Preconditions;

import static java.util.Arrays.asList;

/**
 * A utility class of ActionUnit framework.
 */
public enum Actions {
  ;

  public static Action concurrent(Action... actions) {
    return concurrent(null, actions);
  }

  public static Action concurrent(String summary, Action... actions) {
    return Action.Concurrent.Factory.INSTANCE.create(summary, asList(actions));
  }

  public static Action sequential(Action... actions) {
    return sequential(null, actions);
  }

  public static Action sequential(String summary, Action... actions) {
    return Action.Sequential.Factory.INSTANCE.create(summary, asList(actions));
  }

  public static Action simple(final Runnable runnable) {
    return simple(null, runnable);
  }

  public static Action simple(final String summary, final Runnable runnable) {
    Preconditions.checkNotNull(runnable);
    return new Action.Leaf() {
      @Override
      public String describe() {
        return summary == null
            ? "(noname)"
            : summary;
      }

      @Override
      public void perform() {
        runnable.run();
      }
    };
  }
}
