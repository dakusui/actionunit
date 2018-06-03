package com.github.dakusui.actionunit.extras.cmdaction;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.extras.cmd.Commander;
import com.github.dakusui.crest.Crest;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.github.dakusui.crest.Crest.*;
import static java.util.concurrent.TimeUnit.SECONDS;

public class RetryOnTimeOutTest<R extends Commander<R>> extends FsTestBase<R> {
  public RetryOnTimeOutTest() throws IOException {
  }

  @SuppressWarnings("unchecked")
  @Override
  protected R create(Context context) {
    long start = System.currentTimeMillis() / 1000;
    return (R) new Commander<R>(context) {
      @Override
      protected String program() {
        return "if [[ $(($(date +'%s') - " + start + ")) -ge 3 ]]; then echo success; else echo fail && sleep 3;fi";
      }
    };
  }


  @Test(expected = ActionException.class)
  public void whenRetryOnce$thenFail() {
    try {
      perform(this.commander
          .timeoutIn(1, SECONDS)
          .retryOn(ActionException.class)
          .retries(1)
          .interval(1, SECONDS)
          .build());
    } catch (ActionException e) {
      assertThat(
          this.stdout,
          allOf(
              asInteger("size").equalTo(2).$(),
              asString("get", 0).equalTo("fail").$(),
              asString("get", 1).equalTo("fail").$()
          ));
      throw e;
    }
  }

  @Test
  public void whenRetryThreeTimes$thenPass() {
    perform(this.commander
        .timeoutIn(1, SECONDS)
        .retryOn(ActionException.class)
        .retries(3)
        .interval(1, SECONDS)
        .build()
    );
    assertThat(
        this.stdout,
        allOf(
            allOf(
                asInteger("size").gt(0).$(),
                asInteger("size").le(4).$()
            ),
            asString("get", 0).equalTo("fail").$(),
            Crest.<List<String>>asString(o -> o.get(o.size() - 1)).equalTo("success").$()
        ));
  }
}

