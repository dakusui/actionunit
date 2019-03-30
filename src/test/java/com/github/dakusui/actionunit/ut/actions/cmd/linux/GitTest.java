package com.github.dakusui.actionunit.ut.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.unix.Git;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextFunctions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static com.github.dakusui.actionunit.core.ActionSupport.forEach;
import static com.github.dakusui.actionunit.core.ActionSupport.leaf;
import static com.github.dakusui.actionunit.core.ActionSupport.sequential;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.immediateOf;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.printTo;
import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.crest.Crest.call;
import static com.github.dakusui.crest.Crest.substringAfterRegex;
import static com.github.dakusui.printables.Printables.isEmptyString;

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

      System.out.println(gitClone.buildCommandLineComposer().format());

      performAsAction(gitClone);
    }

    @Test
    public void givenCloneCommandUsingLiterals$whenFormatGitCloneCommand$thenProperlyFormatted() {
      String repoUrl = "https://github.com/dakusui/jcunit.git";
      String branchName = "0.8.x-develop";
      Git.Clone gitClone = git().cloneRepo()
          .repo(repoUrl)
          .branch(branchName);

      System.out.println(gitClone.buildCommandLineComposer().compose(Context.create()));
      assertThat(
          gitClone,
          asString(
              call("buildCommandLineComposer")
                  .andThen("compose", Context.create())
                  .$())
              .check(
                  substringAfterRegex("git")
                      .after("clone")
                      .after("'https://github.com/dakusui/jcunit.git'")
                      .after("-b '0.8.x-develop'")
                      .$(),
                  isEmptyString())
              .$()
      );
    }

    @Test
    public void givenCloneCommandUsingContextFunctions$whenFormatGitCloneCommand$thenProperlyFormatted() {
      String repoUrl = "https://github.com/dakusui/jcunit.git";
      String branchName = "0.8.x-develop";
      Git.Clone gitClone = git().cloneRepo()
          .repo(immediateOf(repoUrl))
          .branch(immediateOf(branchName));

      System.out.println(gitClone.buildCommandLineComposer().compose(Context.create()));
      assertThat(
          gitClone,
          asString(
              call("buildCommandLineComposer")
                  .andThen("compose", Context.create())
                  .$())
              .check(
                  substringAfterRegex("git")
                      .after("clone")
                      .after("'https://github.com/dakusui/jcunit.git'")
                      .after("-b '0.8.x-develop'")
                      .$(),
                  isEmptyString())
              .$()
      );
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

    @Test
    public void givenLsRemoteCommandUsingLiteral$whenFormatGitCloneCommand$thenProperlyFormatted() {
      String repoUrl = "https://github.com/dakusui/jcunit.git";
      Git.LsRemote gitLsRemote = git().lsRemote()
          .repo(repoUrl);

      System.out.println(gitLsRemote.buildCommandLineComposer().compose(Context.create()));
      assertThat(
          gitLsRemote,
          asString(
              call("buildCommandLineComposer")
                  .andThen("compose", Context.create())
                  .$())
              .check(
                  substringAfterRegex("git")
                      .after("ls-remote")
                      .after("'https://github.com/dakusui/jcunit.git'")
                      .$(),
                  isEmptyString())
              .$()
      );
    }

    @Test
    public void givenLsRemoteCommandUsingContextFunction$whenFormatGitCloneCommand$thenProperlyFormatted() {
      String repoUrl = "https://github.com/dakusui/jcunit.git";
      Git.LsRemote gitLsRemote = git().lsRemote()
          .repo(immediateOf(repoUrl));

      System.out.println(gitLsRemote.buildCommandLineComposer().compose(Context.create()));
      assertThat(
          gitLsRemote,
          asString(
              call("buildCommandLineComposer")
                  .andThen("compose", Context.create())
                  .$())
              .check(
                  substringAfterRegex("git")
                      .after("ls-remote")
                      .after("'https://github.com/dakusui/jcunit.git'")
                      .$(),
                  isEmptyString())
              .$()
      );
    }
  }

  public static class ForCheckout extends CommanderTestBase {
    @Ignore
    public static class ActuallyPerformingActions extends CommanderTestBase{
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

    @Test
    public void givenCheckoutNewBranchCommandUsingContextFunction$whenFormatGitCloneCommand$thenProperlyFormatted() {
      String branchName = "master-indexof-feature";
      Git.Checkout gitCheckout = git().checkout()
          .newBranch(immediateOf(branchName));

      System.out.println(gitCheckout.buildCommandLineComposer().compose(Context.create()));
      assertThat(
          gitCheckout,
          asString(
              call("buildCommandLineComposer")
                  .andThen("compose", Context.create())
                  .$())
              .check(
                  substringAfterRegex("git")
                      .after("checkout")
                      .after("-b").after("'master-indexof-feature'")
                      .$(),
                  isEmptyString())
              .$()
      );
    }

    @Test
    public void givenCheckoutNewBranchCommandUsingLiteral$whenFormatGitCloneCommand$thenProperlyFormatted() {
      String branchName = "master-indexof-feature";
      Git.Checkout gitCheckout = git().checkout()
          .newBranch(branchName);

      System.out.println(gitCheckout.buildCommandLineComposer().compose(Context.create()));
      assertThat(
          gitCheckout,
          asString(
              call("buildCommandLineComposer")
                  .andThen("compose", Context.create())
                  .$())
              .check(
                  substringAfterRegex("git")
                      .after("checkout")
                      .after("-b").after("'master-indexof-feature'")
                      .$(),
                  isEmptyString())
              .$()
      );
    }

    @Test
    public void givenCheckoutCommandUsingContextFunction$whenFormatGitCloneCommand$thenProperlyFormatted() {
      String branchName = "master-indexof-feature";
      Git.Checkout gitCheckout = git().checkout()
          .branch(immediateOf(branchName));

      System.out.println(gitCheckout.buildCommandLineComposer().compose(Context.create()));
      assertThat(
          gitCheckout,
          asString(
              call("buildCommandLineComposer")
                  .andThen("compose", Context.create())
                  .$())
              .check(
                  substringAfterRegex("git")
                      .after("checkout")
                      .after("'master-indexof-feature'")
                      .$(),
                  isEmptyString())
              .$()
      );
    }

    @Test
    public void givenCheckoutCommandUsingLiteral$whenFormatGitCloneCommand$thenProperlyFormatted() {
      String branchName = "master-indexof-feature";
      Git.Checkout gitCheckout = git().checkout()
          .branch(branchName);

      System.out.println(gitCheckout.buildCommandLineComposer().compose(Context.create()));
      assertThat(
          gitCheckout,
          asString(
              call("buildCommandLineComposer")
                  .andThen("compose", Context.create())
                  .$())
              .check(
                  substringAfterRegex("git")
                      .after("checkout")
                      .after("'master-indexof-feature'")
                      .$(),
                  isEmptyString())
              .$()
      );
    }
  }

  public static class ForPush extends CommanderTestBase {
    @Test
    public void givenRepoAndSpecWithImmediate$whenFormat$thenCommandLineFormatIsCorrect() {
      Git.Push gitPush = git().push().repo("origin").refspec("master:master");
      assertThat(
          gitPush.buildCommandLineComposer().format(),
          asString().equalTo("git push quoteWith['](origin) quoteWith['](master:master)").$()
      );
    }

    @Test
    public void givenRepoAndSpecWithFunction$whenFormat$thenCommandLineFormatIsCorrect() {
      Git.Push gitPush = git().push().repo(immediateOf("origin")).refspec(immediateOf("master:master"));
      assertThat(
          gitPush.buildCommandLineComposer().format(),
          asString().equalTo("git push quoteWith['](origin) quoteWith['](master:master)").$()
      );
    }
  }

  public static class ForPlain extends CommanderTestBase {
    @Test
    public void givenRepoAndSpecWithImmediate$whenFormat$thenCommandLineFormatIsCorrect() {
      Git.Plain git = git().plain().add("hello").add("world");
      assertThat(
          git.buildCommandLineComposer().format(),
          asString().equalTo("git quoteWith['](hello) quoteWith['](world)").$()
      );
    }

    @Test
    public void givenRepoAndSpecWithFunction$whenFormat$thenCommandLineFormatIsCorrect() {
      Git.Plain git = git().plain().add("hello").add("world");
      assertThat(
          git.buildCommandLineComposer().format(),
          asString().equalTo("git quoteWith['](hello) quoteWith['](world)").$()
      );
    }
  }
}
