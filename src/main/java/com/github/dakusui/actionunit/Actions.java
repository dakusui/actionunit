package com.github.dakusui.actionunit;

import com.google.common.base.Preconditions;

import java.util.Arrays;

/**
 * A utility class of ActionUnit framework.
 */
public enum Actions {
  ;

  public static Action concurrent(Action... actions) {
    return concurrent(null, actions);
  }

  public static Action concurrent(String summary, Action... actions) {
    return Action.Concurrent.Factory.INSTANCE.create(summary, Arrays.asList(actions));
  }

  public static Action sequential(Action... actions) {
    return sequential(null, actions);
  }

  public static Action sequential(String summary, Action... actions) {
    return Action.Sequential.Factory.INSTANCE.create(summary, Arrays.asList(actions));
  }

  public static Action simple(final Runnable runnable) {
    return simple(null, runnable);
  }

  public static Action simple(final String summary, final Runnable runnable) {
    Preconditions.checkNotNull(runnable);
    return new Action.Leaf() {
      @Override
      public String format() {
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

  public static <T> Action simple(final String summary, final Block<T> block, final T targetElement) {
    return new Action.Targeted<T>(targetElement) {
      @Override
      protected void perform(T target) {
        block.apply(target);
      }

      @Override
      public String format() {
        return String.format("%s(%s)",
            summary == null
                ? "(noname)"
                : summary,
            targetElement == null
                ? null
                : targetElement.toString()
            );
      }
    };
  }
}
