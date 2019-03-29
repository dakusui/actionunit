package com.github.dakusui.actionunit.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.actionunit.actions.cmd.CommanderFactory;
import com.github.dakusui.actionunit.core.context.ContextFunction;
import com.github.dakusui.actionunit.core.context.StreamGenerator;
import com.github.dakusui.processstreamer.core.process.Shell;

import java.util.function.Function;
import java.util.function.IntFunction;

import static com.github.dakusui.actionunit.core.context.ContextFunctions.immediateOf;
import static java.util.Objects.requireNonNull;

public interface Git extends CommanderFactory {
  default LsRemote lsRemote() {
    return new LsRemote(variablePlaceHolderFormatter());
  }

  default Clone cloneRepo() {
    return new Clone(variablePlaceHolderFormatter());
  }

  default Checkout checkout() {
    return new Checkout(variablePlaceHolderFormatter());
  }

  default Push push() {
    return new Push(variablePlaceHolderFormatter());
  }

  default GitBase<Plain> plain() {
    return new Plain(variablePlaceHolderFormatter());
  }

  default Function<String[], IntFunction<String>> variablePlaceHolderFormatter() {
    return parent().variablePlaceHolderFormatter();
  }

  default Shell shell() {
    return parent().shell();
  }

  CommanderFactory parent();

  class Clone extends GitBase<Clone> {
    public Clone(Function<String[], IntFunction<String>> parameterPlaceHolderFormatter) {
      super(parameterPlaceHolderFormatter);
      this.addOption("clone");
    }

    public Clone branch(String branchName) {
      return this.addOption("-b").addOption(branchName);
    }

    public Clone repo(String repo) {
      return this.add(repo);
    }

    public Clone repo(ContextFunction<String> repo) {
      return this.add(repo);
    }
  }

  class LsRemote extends GitBase<LsRemote> {
    public LsRemote(Function<String[], IntFunction<String>> parameterPlaceHolderFormatter) {
      super(parameterPlaceHolderFormatter);
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
    public Checkout(Function<String[], IntFunction<String>> parameterPlaceHolderFormatter) {
      super(parameterPlaceHolderFormatter);
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
    public Push(Function<String[], IntFunction<String>> parameterPlaceHolderFormatter) {
      super(parameterPlaceHolderFormatter);
    }
  }

  class Plain extends GitBase<Plain> {
    public Plain(Function<String[], IntFunction<String>> parameterPlaceHolderFormatter) {
      super(parameterPlaceHolderFormatter);
    }
  }

  abstract class GitBase<C extends GitBase<C>> extends Commander<C> {
    public GitBase(Function<String[], IntFunction<String>> parameterPlaceHolderFormatter) {
      super(parameterPlaceHolderFormatter);
      this.command("git");
    }

    /*
  public abstract class Git<G extends Commander<G>> extends Commander<G> {

    public static class Push extends Git<Push> {

      public Push() {
        super();
        super.add("push");
      }

      public Push repoWithRefSpec(String repo, String refSpec) {
        return add(repo).add(refSpec);
      }
    }


    public Git() {
      super();
    }

    public G repo(String repo) {
      return add(repo);
    }

    @Override
    protected String program() {
      return "git";
    }
  }

     */
  }
}
