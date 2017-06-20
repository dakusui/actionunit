package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class NewStyleExample {
  @Test
  public void run() {
    ReportingActionPerformer.create(
        new Context.Impl().forEachOf(
            "a", "b", "c"
        ).sequentially(
        ).perform(
            ($, i) -> $.sequential(
                $.simple(
                    "print(i)",
                    () -> System.out.println(i.get())
                ),
                $.forEachOf(
                    1, 2, 3, 4, 5
                ).concurrently(
                ).perform(
                    ($$, j) -> $$.concurrent(
                        $$.retry(
                            $$.simple(
                                "print(j)",
                                () -> System.out.println(j.get())
                            )).times(3).withIntervalOf(100, TimeUnit.MILLISECONDS).build(),
                        $$.simple(
                            "print(j + 100)",
                            () -> System.out.println(j.get() + 100)
                        )

                    )))
        )
    ).performAndReport();
  }
}
