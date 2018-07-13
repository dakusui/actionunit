package com.github.dakusui.actionunit.extras.cmdaction;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.extras.cmd.Commander;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * This test base is intended to test a concrete commander class and assumes
 * that the instance is created from top level {@code context}.
 * <p>
 * That is, it's not useful to test an instance inside {@code ForEachOf} loop
 * structure.
 *
 * @param <C> A commander class under test.
 */
public abstract class CommanderTestBase<C extends Commander<C>> {
  protected final Context      context;
  protected final C            commander;
  protected final List<String> stdout;
  protected final List<String> stderr;

  protected abstract C create(Context context);

  protected <D extends Commander<D>> D configure(D commander) {
    commander.cmdBuilder().consumeStdout(
        ((Consumer<String>) requireNonNull(stdout)::add).andThen(System.out::println)
    ).consumeStderr(
        ((Consumer<String>) requireNonNull(stderr)::add).andThen(System.err::println)
    );
    return commander;
  }

  public CommanderTestBase() {
    this.context = new Context.Impl();
    this.stdout = new LinkedList<>();
    this.stderr = new LinkedList<>();
    this.commander = this.configure(create(this.context));
  }

  protected void perform(Action action) {
    CommanderTestUtil.perform(action);
  }
}
