package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.actions.ForEach2;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import com.github.dakusui.printables.PrintableFunctionals;
import org.junit.Test;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static com.github.dakusui.actionunit.ut.actions.TestFunctionals.constant;
import static com.github.dakusui.printables.PrintableFunctionals.memoize;
import static com.github.dakusui.printables.PrintableFunctionals.printableFunction;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class PracticalExample extends TestUtils.TestBase {
  /**
   * A dummy function that determines an IP address for a given hostname.
   * This function fails probabilistically fails in rate of 50%
   */
  private static final Function<String, String> TO_BACKEND_IP_ADDRESS = printableFunction((String hostName) -> {
    if (System.nanoTime() % 2 == 1)
      throw new UnluckyException();
    return String.format(
        "%d.%d.%d.%d",
        hostName.charAt(1 % hostName.length()) % 256,
        hostName.charAt(2 % hostName.length()) % 256,
        hostName.charAt(3 % hostName.length()) % 256,
        hostName.charAt(4 % hostName.length()) % 256
    );
  }).describe("toBackendIpAddress");

  /**
   * A dummy function that returns a specified server's state.
   * This may return a string "RUNNING" or "WAITING" randomly
   */
  private static final Function<String, String> GET_SERVER_STATE = s -> System.currentTimeMillis() % 2 == 1
      ? "RUNNING"
      : "WAITING";

  @Test
  public void buildAndRunAction() {
    ////
    // Prepare a memoized version of 'TO_BACKEND_IP_ADDRESS' to share its output
    // among actions inside the tree
    Function<String, String> toBackendIpAddress = memoize(TO_BACKEND_IP_ADDRESS);
    ////
    // Build action tree
    Action action = forEach(
        constant(Stream.of("alexios", "nikephoros", "manuel", "constantine", "justinian")))
        .action(
            b -> sequential(
                retry(
                    simple(
                        "Try to figure out physical ip address",
                        (c) -> toBackendIpAddress.apply(b.contextVariable(c))))
                    .on(UnluckyException.class)
                    .times(10)
                    .withIntervalOf(2, MILLISECONDS)
                    .build(),
                sequential(
                    simple(
                        "Do something using retrieved IP address",
                        (c) -> System.out.printf("%s:%s%n", hostName(c, b), toBackendIpAddress.apply(hostName(c, b)))),
                    simple("Do something time consuming", sleep(10)),
                    simple("Do something time consuming", sleep(1)),
                    simple("Get state of the server using IP address", (c) -> System.out.printf("%s:%s%n", hostName(c, b), GET_SERVER_STATE.apply(toBackendIpAddress.apply(hostName(c, b))))),
                    simple("Do something else using retrieved IP address", (c) -> System.out.printf("%s:%s%n", hostName(c, b), toBackendIpAddress.apply(hostName(c, b))))))).$();
    ////
    // Perform the action tree and report the result
    ReportingActionPerformer.create().performAndReport(action, Writer.Std.ERR);

    ////
    // This will print out something like following to stdout
    //
    // alexios:8.103.42.180
    // nikephoros:129.3.94.169
    // justinian:137.183.88.61
    // manuel:185.201.201.199
    // nikephoros:WAITING
    // alexios:RUNNING
    // nikephoros:129.3.94.169
    // alexios:8.103.42.180
    // constantine:170.218.254.36
    // justinian:RUNNING
    // justinian:137.183.88.61
    // manuel:WAITING
    // manuel:185.201.201.199
    // constantine:RUNNING
    // constantine:170.218.254.36

    // And something like following will be printed to stderr
    //
    //  [o]ForEach(CONCURRENTLY)
    //    [o...]Sequential (2 actions)
    //      [o...]Retry(2[milliseconds]x10times)
    //        [oxxxooxxoo]figure out physical ip address
    //        [o...]Sequential (4 actions)
    //        [o...]Do something using retrieved IP address
    //      [o...]Do something time consuming
    //        [o...]sleep for 10[milliseconds]
    //        [o...]get state of the server using IP address
    //      [o...]Do something else using retrieved IP address
  }

  private static Consumer<Context> sleep(int millis) {
    return PrintableFunctionals.printableConsumer((Context c) -> {
      try {
        Thread.sleep(millis, 0);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }).describe("sleep[" + millis + " milliseconds]");
  }

  private static class UnluckyException extends RuntimeException {
  }

  private String hostName(Context c, ForEach2.Builder<String> builder) {
    return builder.contextVariable(c);
  }
}
