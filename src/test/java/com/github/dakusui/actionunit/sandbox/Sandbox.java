package com.github.dakusui.actionunit.sandbox;

import com.github.dakusui.actionunit.ActionUnit;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.examples.ForEachExample;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import static com.github.dakusui.actionunit.helpers.Checks.checkNotNull;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Sandbox {
  public static <T> int sizeOrNegativeIfNonCollection(Iterable<T> iterable) {
    checkNotNull(iterable);
    if (iterable instanceof Collection) {
      return Collection.class.cast(iterable).size();
    }
    return -1;
  }

  @ActionUnit.PerformWith(Test.class)
  public static Action composeNestedLoop2(ForEachExample self) {
    class FailedToLookUpServerid extends RuntimeException {
    }
    Function<String, String> lookupServerIdThroughUinventoryApi = String::toUpperCase;
    return self.forEachOf(asList("dev-ugspbk001", "dev-ugspadm001", "dev-ugspapi001"))
        .concurrently()
        .perform(
            ($, hostName) -> self.sequential(
                self.retry(
                    self.sequential(
                        self.simple("print hostname", () -> {
                          System.out.println("hostname=" + hostName.get());
                        }),
                        self.simple("print serverid", () -> {
                          System.out.println("serverid=" + lookupServerIdThroughUinventoryApi.apply(hostName.get()));
                        })
                    )
                ).on(
                    FailedToLookUpServerid.class
                ).times(
                    3
                ).withIntervalOf(
                    10, SECONDS
                ).build()
            ));
  }

  @Test
  public void testAtomicReference() {
    AtomicReference<String> ref = new AtomicReference<>();
    System.out.println(ref.get());
  }


  @Test
  public void testAutocloseable() {
    List<String> data = asList("A", "B", "C");
    StreamSupport.stream(
        ((Iterable<String>) () ->
            Autocloseables.autocloseable(
                data.iterator(),
                () -> System.err.println("closed!")
            )).spliterator(),
        false
    ).forEach(System.out::println);
  }
}
