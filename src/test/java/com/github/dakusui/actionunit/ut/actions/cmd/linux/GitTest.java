package com.github.dakusui.actionunit.ut.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.linux.Git;
import com.github.dakusui.actionunit.core.context.ContextFunctions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.immediateOf;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.printTo;

@RunWith(Enclosed.class)
public class GitTest {
  public static class ForClone extends CommanderTestBase {
    @Ignore
    @Test
    public void test1() {
      Git.Clone gitClone = git().cloneRepo()
          .repo("https://github.com/dakusui/jcunit.git")
          .branch("0.8.x-develop")
          .cwd(this.baseDir());

      System.out.println(gitClone.buildCommandLineComposer().commandLineString());

      performAsAction(gitClone);
    }
  }

  public static class ForLsRemote extends CommanderTestBase {
    @Ignore
    @Test
    public void test0() {
      Git.LsRemote gitLsRemote = git().lsRemote()
          .repo(immediateOf("https://github.com/dakusui/jcunit.git"));

      performAction(
          forEach("i", gitLsRemote.remoteBranchNames())
              .perform(leaf(printTo(System.out, ContextFunctions.contextValueOf("i"))))
      );
    }

    @Ignore
    @Test
    public void test1() {
      Git.LsRemote gitLsRemote = git().lsRemote()
          .repo("https://github.com/dakusui/jcunit.git");

      performAction(
          forEach("i", gitLsRemote.remoteBranchNames())
              .perform(leaf(printTo(System.out, ContextFunctions.contextValueOf("i"))))
      );
    }

    @Ignore
    @Test
    public void test2() {
      Git.LsRemote gitLsRemote = git().lsRemote();
      performAction(
          forEach("i", gitLsRemote.remoteBranchNames())
              .perform(leaf(printTo(System.out, ContextFunctions.contextValueOf("i"))))
      );
    }
  }

  public static class ForCheckout extends CommanderTestBase {
    @Ignore
    @Test(expected = RuntimeException.class)
    public void test1() {
      performAsAction(
          git().checkout().branch("nonExistingBranch").cwd(repoDir())
      );
    }

    @Ignore
    @Test
    public void test2() {
      performAsAction(
          git().checkout().branch(immediateOf("master-indexof-feature")).cwd(repoDir())
      );
    }

    @Ignore
    @Test
    public void test3() {
      performAction(
          sequential(
              git().checkout().newBranch("master-indexof-feature").cwd(repoDir()).downstreamConsumer(System.out::println).toAction(),
              git().plain().addOption("branch").downstreamConsumer(System.out::println).cwd(repoDir()).toAction()
          ));
    }

    @Ignore
    @Test
    public void test4() {
      performAsAction(
          git().checkout().newBranch(immediateOf("master-indexof-feature")).cwd(repoDir())
      );
    }

    @Override
    @Before
    public void setUp() throws IOException {
      super.setUp();
      Git.Clone gitClone = git().cloneRepo()
          .repo(immediateOf("https://github.com/dakusui/combinatoradix.git"))
          .branch("master")
          .cwd(this.baseDir());
      performAsAction(gitClone);
    }

    private File repoDir() {
      return new File(baseDir(), "combinatoradix");
    }
  }
}
