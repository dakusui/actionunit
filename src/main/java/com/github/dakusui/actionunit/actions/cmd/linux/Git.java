package com.github.dakusui.actionunit.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.actionunit.actions.cmd.CommanderFactory;
import com.github.dakusui.processstreamer.core.process.Shell;

import java.util.function.Function;
import java.util.function.IntFunction;

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
    }
  }

  class LsRemote extends GitBase<LsRemote> {
    public LsRemote(Function<String[], IntFunction<String>> parameterPlaceHolderFormatter) {
      super(parameterPlaceHolderFormatter);
    }
  }

  class Checkout extends GitBase<Checkout> {
    public Checkout(Function<String[], IntFunction<String>> parameterPlaceHolderFormatter) {
      super(parameterPlaceHolderFormatter);
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

    public static class LsRemote extends Git<LsRemote> {

      public LsRemote() {
        super();
        super.add("ls-remote");
        cmdBuilder().transformStdout(stream -> stream.map(line -> line.trim().split("\\s+")[1]));
      }
    }

    public static class Clone extends Git<Clone> {

      public Clone() {
        super();
        super.add("clone");
      }

      public Clone branch(String branch) {
        return add(Option.BRANCH, branch);
      }

      public Clone repoWithDir(String repo, String dir) {
        return add(repo).add(dir);
      }

      private enum Option implements CommanderOption {
        BRANCH("-b", null);

        private final String shortFormat;
        private final String longFormat;

        Option(String shortFormat, String longFormat) {
          this.shortFormat = shortFormat;
          this.longFormat = longFormat;
        }

        @Override
        public String longFormat() {
          return requireNonNull(this.longFormat);
        }

        @Override
        public String shortFormat() {
          return this.shortFormat;
        }
      }
    }

    public static class Checkout extends Git<Checkout> {

      public Checkout() {
        super();
        super.add("checkout");
      }

      public Checkout baseBranch(String branch) {
        return add(branch);
      }

      public Checkout newBranch(String branch) {
        return add(Option.BRANCH, branch);
      }

      private enum Option implements CommanderOption {
        BRANCH("-b", null);

        private final String shortFormat;
        private final String longFormat;

        Option(String shortFormat, String longFormat) {
          this.shortFormat = shortFormat;
          this.longFormat = longFormat;
        }

        @Override
        public String longFormat() {
          return requireNonNull(this.longFormat);
        }

        @Override
        public String shortFormat() {
          return this.shortFormat;
        }
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
