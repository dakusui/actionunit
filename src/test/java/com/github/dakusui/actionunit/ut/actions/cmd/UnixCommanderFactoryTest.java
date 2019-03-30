package com.github.dakusui.actionunit.ut.actions.cmd;

import com.github.dakusui.actionunit.actions.cmd.UnixCommanderFactory;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class UnixCommanderFactoryTest implements UnixCommanderFactory {
  @Test
  public void localTest() {
    perform(local().echo().message("hello world").downstreamConsumer(System.out::println).toAction());
  }

  @Test
  public void remoteTest() throws UnknownHostException {
    perform(remote(InetAddress.getLocalHost().getHostName())
        .echo()
        .message("hello world")
        .downstreamConsumer(System.out::println).toAction());
  }

  private void perform(Action action) {
    ReportingActionPerformer.create().perform(action);
  }
}
