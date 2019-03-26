package com.github.dakusui.actionunit.ut.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.linux.Ls;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.github.dakusui.actionunit.core.context.ContextFunctions.immediateOf;
import static com.github.dakusui.crest.Crest.asListOf;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.crest.Crest.sublistAfter;
import static com.github.dakusui.crest.Crest.sublistAfterElement;
import static com.github.dakusui.crest.utils.printable.Predicates.matchesRegex;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class LsTest extends CommanderTestBase {
  @Test
  public void givenNormalAndHiddenFiles$whenLs$thenNormalFileIsListed() throws IOException {
    createNewFile(".hello");
    createNewFile("world");
    performAsAction(
        newLs().file(baseDir())
    );
    assertThat(
        out(),
        asListOf(String.class).equalTo(singletonList("world")).$()
    );
  }

  @Test
  public void givenNormalAndHiddenFiles$whenLsWithContextFunction$thenNormalFileIsListed() throws IOException {
    createNewFile(".hello");
    createNewFile("world");
    performAsAction(
        newLs().file(immediateOf(baseDir().getAbsolutePath()))
    );
    assertThat(
        out(),
        asListOf(String.class).equalTo(singletonList("world")).$()
    );
  }

  @Test
  public void givenNormalAndHiddenFiles$whenLsWithLongFormat$thenNormalFilesAreListed() throws IOException {
    createNewFile("hello");
    createNewFile("world");
    performAsAction(
        newLs().longListing().file(baseDir())
    );
    assertThat(
        out(),
        asListOf(
            String.class,
            sublistAfter(matchesRegex(".+hello")).after(matchesRegex(".+world")).$())
            .isEmpty().$());
  }

  @Test
  public void givenNormalAndHiddenFiles$whenLsAll$thenAllFilesAreListed() throws IOException {
    createNewFile(".hello");
    createNewFile("world");
    performAsAction(
        newLs().all().file(baseDir())
    );
    assertThat(
        out(),
        asListOf(String.class).equalTo(asList(".", "..", ".hello", "world")).$()
    );
  }

  @Test
  public void givenSmallFile$whenLsSizeWithHumanReadableOption$thenFilePrintedWithHumanReadableSize()
      throws IOException {
    createNewFile("hello", "HELLO");
    performAsAction(
        newLs().size().humanReadable().file(baseDir())
    );
    assertThat(
        out(),
        asListOf(String.class, sublistAfterElement("4.0K hello").$()).isEmpty().$()
    );
  }

  /**
   * This test is flaky. Even giving a sleep in between file creations, sometimes
   * the output is not ordered correctly. (annoying)
   *
   * @throws IOException          Failed to create file(s)
   * @throws InterruptedException Interrupted
   */
  @Test
  public void givenTwoFiles$whenLsWithReverseOrderSortingByMtime$thenFilesPrintedInExpectedOrder()
      throws IOException, InterruptedException {
    createNewDir("hello");
    // Make sure the result is stably ordered.
    TimeUnit.SECONDS.sleep(1);
    createNewFile("world");
    performAsAction(
        newLs().reverse().sortByMtime().file(baseDir())
    );
    assertThat(
        out(),
        asListOf(String.class).equalTo(asList("hello", "world")).$()
    );
  }

  /**
   * This test is flaky. Even giving a sleep in between file creations, sometimes
   * the output is not ordered correctly. (annoying)
   *
   * @throws IOException          Failed to create file(s)
   * @throws InterruptedException Interrupted
   */
  @Test
  public void givenTwoFiles$whenLsSortingByMtimeWithClassifier$thenFilesPrintedInExpectedOrderWithClassifier()
      throws IOException, InterruptedException {
    createNewDir("hello");
    // Make sure the result is stably ordered.
    TimeUnit.SECONDS.sleep(1);
    createNewFile("world");
    performAsAction(
        newLs().classify().sortByMtime().file(baseDir())
    );
    assertThat(
        out().stream().sorted().collect(toList()),
        asListOf(String.class).equalTo(asList("hello/", "world")).$()
    );
  }

  private Ls newLs() {
    return ls();
  }
}
