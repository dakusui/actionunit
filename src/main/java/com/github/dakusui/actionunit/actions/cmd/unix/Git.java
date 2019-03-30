package com.github.dakusui.actionunit.actions.cmd.unix;

import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.actionunit.actions.cmd.CommanderFactory;
import com.github.dakusui.actionunit.actions.cmd.CommanderInitializer;
import com.github.dakusui.actionunit.core.context.ContextFunction;
import com.github.dakusui.actionunit.core.context.StreamGenerator;

import static com.github.dakusui.actionunit.core.context.ContextFunctions.immediateOf;
import static java.util.Objects.requireNonNull;

@FunctionalInterface
public interface Git extends CommanderFactory {
  default LsRemote lsRemote() {
    return new LsRemote(initializer());
  }

  default Clone cloneRepo() {
    return new Clone(initializer());
  }

  default Checkout checkout() {
    return new Checkout(initializer());
  }

  default Push push() {
    return new Push(initializer());
  }

  default GitBase<Plain> plain() {
    return new Plain(initializer());
  }

  @Override
  default CommanderInitializer initializer() {
    return parent().initializer();
  }

  CommanderFactory parent();

  class Clone extends GitBase<Clone> {
    @SuppressWarnings("WeakerAccess")
    public Clone(CommanderInitializer initializer) {
      super(initializer);
      this.addOption("clone");
    }

    public Clone branch(String branchName) {
      return this.addOption("-b").add(branchName);
    }

    public Clone branch(ContextFunction<String> branchName) {
      return this.addOption("-b").add(branchName);
    }

    public Clone repo(String repo) {
      return this.add(repo);
    }

    public Clone repo(ContextFunction<String> repo) {
      return this.add(repo);
    }
  }

  class LsRemote extends GitBase<LsRemote> {
    @SuppressWarnings("WeakerAccess")
    public LsRemote(CommanderInitializer initializer) {
      super(initializer);
      this.addOption("ls-remote");
    }

    public LsRemote repo(String repo) {
      return this.add(repo);
    }

    public LsRemote repo(ContextFunction<String> repo) {
      return this.add(requireNonNull(repo));
    }

    public StreamGenerator<String> remoteBranchNames() {
      return c -> toStreamGenerator().apply(c).map(line -> line.trim().split("\\s+")[1]);
    }
  }

  class Checkout extends GitBase<Checkout> {
    @SuppressWarnings("WeakerAccess")
    public Checkout(CommanderInitializer initializer) {
      super(initializer);
      this.addOption("checkout");
    }

    public Checkout branch(String branch) {
      return this.add(branch);
    }

    public Checkout branch(ContextFunction<String> branch) {
      return this.add(branch);
    }

    public Checkout newBranch(String branch) {
      return this.newBranch(immediateOf(branch));
    }

    public Checkout newBranch(ContextFunction<String> branch) {
      return this.addOption("-b").add(branch);
    }
  }

  class Push extends GitBase<Push> {
    @SuppressWarnings("WeakerAccess")
    public Push(CommanderInitializer initializer) {
      super(initializer);
      this.addOption("push");
    }

    public Push repo(String repo) {
      return add(repo);
    }

    public Push repo(ContextFunction<String> repo) {
      return add(repo);
    }

    public Push refspec(String spec) {
      return add(spec);
    }

    public Push refspec(ContextFunction<String> spec) {
      return add(spec);
    }
  }

  class Plain extends GitBase<Plain> {
    @SuppressWarnings("WeakerAccess")
    public Plain(CommanderInitializer initializer) {
      super(initializer);
    }
  }

  abstract class GitBase<C extends GitBase<C>> extends Commander<C> {
    @SuppressWarnings("WeakerAccess")
    public GitBase(CommanderInitializer initializer) {
      super(initializer);
      this.command("git");
    }
  }
}
