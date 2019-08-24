package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.utils.InternalUtils;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.nop;
import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.printables.Printables.isEqualTo;

public class CloningTest {
  @Test
  public void cloneNop() {
    Action action = nop();

    Action cloned = action.cloneAction();

    assertThat(
        cloned,
        asObject()
            .isInstanceOf(Leaf.class)
            .check(callOn(String.class, "format", "string:<%s>", new Object[]{cloned}).$(),
                isEqualTo("string:<(nop)>"))
            .$()
    );
  }

  @Test
  public void cloneNamed() {
    Action action = ActionSupport.named("namedAction", nop());

    Action cloned = action.cloneAction();

    assertThat(
        cloned,
        asObject()
            .isInstanceOf(Named.class)
            .check(callOn(String.class, "format", "string:<%s>", new Object[]{cloned}).$(),
                isEqualTo("string:<namedAction>"))
            .$()
    );
  }

  @Test
  public void cloneAttempt() {
    Action action = ActionSupport.attempt(nop())
        .recover(Exception.class, nop())
        .ensure(nop());

    Action cloned = action.cloneAction();

    assertThat(
        cloned,
        asObject()
            .isInstanceOf(Attempt.class)
            .check(callOn(String.class, "format", "string:<%s>", new Object[]{cloned}).$(),
                isEqualTo("string:<attempt>"))
            .$()
    );
  }

  @Test
  public void cloneForEach() {
    Action action = ActionSupport.forEach("i", (c) -> Stream.empty()).perform(nop());


    Action cloned = action.cloneAction();

    assertThat(
        cloned,
        asObject()
            .isInstanceOf(ForEach.class)
            .check(callOn(String.class, "format", "string:<%s>", new Object[]{cloned}).$(),
                isEqualTo("string:<for each of (noname) sequentially>"))
            .$()
    );
  }

  @Test
  public void cloneWhen() {
    Action action = ActionSupport.when((c) -> true).perform(nop()).otherwise(nop());

    Action cloned = action.cloneAction();

    assertThat(
        cloned,
        asObject()
            .isInstanceOf(When.class)
            .check(callOn(String.class, "format", "string:<%s>", new Object[]{cloned}).$(),
                isEqualTo("string:<if [(noname)]>"))
            .$()
    );
  }

  @Test
  public void cloneRetry() {
    Action action = ActionSupport.retry(nop()).on(Exception.class).withIntervalOf(1, TimeUnit.SECONDS).times(1).$();

    Action cloned = action.cloneAction();

    assertThat(
        cloned,
        asObject()
            .isInstanceOf(Retry.class)
            .check(callOn(String.class, "format", "string:<%s>", new Object[]{cloned}).$(),
                isEqualTo("string:<retry once in 1 [seconds] on Exception>"))
            .$()
    );
  }

  @Test
  public void cloneTimeout() {
    Action action = ActionSupport.timeout(nop()).in(1, TimeUnit.SECONDS);

    Action cloned = action.cloneAction();

    assertThat(
        cloned,
        asObject()
            .isInstanceOf(TimeOut.class)
            .check(callOn(String.class, "format", "string:<%s>", new Object[]{cloned}).$(),
                isEqualTo("string:<timeout in 1 [seconds]>"))
            .$()
    );
  }

  @Test
  public void cloneSequential() {
    Action action = ActionSupport.sequential(nop(), nop());

    Action cloned = action.cloneAction();

    assertThat(
        cloned,
        asObject()
            .isInstanceOf(Composite.class)
            .check(callOn(String.class, "format", "string:<%s>", new Object[]{cloned}).$(),
                isEqualTo("string:<do sequentially>"))
            .$()
    );
  }

  @Test
  public void cloneParallel() {
    Action action = ActionSupport.parallel(nop(), nop());

    Action cloned = action.cloneAction();

    assertThat(
        cloned,
        asObject()
            .isInstanceOf(Composite.class)
            .check(callOn(String.class, "format", "string:<%s>", new Object[]{cloned}).$(),
                isEqualTo("string:<do parallelly>"))
            .$()
    );
  }

  @Test
  public void cloneCmd() {
    Action action = ActionSupport.cmd("echo hello").describe("echoing hello").toAction();

    Action cloned = action.cloneAction();

    assertThat(
        cloned,
        asObject()
            .isInstanceOf(Leaf.class)
            .check(callOn(String.class, "format", "string:<%s>", new Object[]{cloned}).$(),
                isEqualTo("string:<do parallelly>"))
            .$()
    );
  }

  @Test
  public void cloneCommander() {
    Commander commander = ActionSupport.cmd("echo hello").describe("echoing hello");

    Commander cloned = InternalUtils.cloneObjectBySerialization(commander);

    assertThat(
        cloned,
        asObject()
            .isInstanceOf(Leaf.class)
            .check(callOn(String.class, "format", "string:<%s>", new Object[]{cloned}).$(),
                isEqualTo("string:<do parallelly>"))
            .$()
    );
  }
}
