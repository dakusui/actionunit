package com.github.dakusui.actionunit.ut.actions.cmd;

import com.github.dakusui.actionunit.actions.cmd.CommanderFactoryManager;
import com.github.dakusui.actionunit.actions.cmd.CommanderConfig;
import com.github.dakusui.actionunit.actions.cmd.unix.Echo;
import com.github.dakusui.actionunit.actions.cmd.unix.Scp;
import com.github.dakusui.actionunit.actions.cmd.unix.SshOptions;
import com.github.dakusui.actionunit.actions.cmd.unix.SshShellBuilder;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import com.github.dakusui.processstreamer.core.process.Shell;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.pcond.forms.Predicates.isEmptyString;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@RunWith(Enclosed.class)
public class CommanderFactoryManagerTest {
  static abstract class Base implements CommanderFactoryManager {
    @Ignore
    @Test
    public void performLocally() {
      perform(localEcho().toAction());
    }

    @Ignore
    @Test
    public void performRemotely() {
      perform(remoteEcho().toAction());
    }

    @Test
    public void formatLocalEcho() {
      assertThat(
          localEcho(),
          allOf(
              asString(
                  call("buildCommandLineComposer")
                      .andThen("compose", Context.create()).$())
                  .check(
                      substringAfterRegex("echo")
                          .after("'hello world'").$(),
                      isEmptyString()
                  ).$(),
              asString(
                  call("shell")
                      .andThen("format").$())
                  .check(
                      substringAfterRegex("sh").after("-c").$(),
                      isEmptyString()).$()
          )
      );
    }

    @Test
    public void formatRemoteEcho() {
      assertThat(
          remoteEcho(),
          allOf(
              asString(
                  call("buildCommandLineComposer")
                      .andThen("compose", Context.create()).$())
                  .check(
                      substringAfterRegex("echo").after("'hello world'").$(),
                      isEmptyString()
                  ).$(),
              asString(
                  call("shell").andThen("format").$()
              ).check(
                  substringAfterExpectedRegexesForSshOptions(),
                  isEmptyString()
              ).$()
          )
      );
    }

    @Test
    public void formatScp() {
      assertThat(
          scp(),
          asString(
              call("buildCommandLineComposer")
                  .andThen("compose", Context.create()).$())
              .check(
                  substringAfterExpectedRegexesForSshOptions_Scp(),
                  isEmptyString()
              ).$());
    }


    abstract public Function<String, String> substringAfterExpectedRegexesForSshOptions();

    abstract Function<String, String> substringAfterExpectedRegexesForSshOptions_Scp();

    private Echo remoteEcho() {
      return remote(hostName())
          .echo()
          .message("hello world")
          .downstreamConsumer(System.out::println);
    }

    private Echo localEcho() {
      return local().echo().message("hello world").downstreamConsumer(System.out::println);
    }

    private Scp scp() {
      return local()
          .scp().file(Scp.Target.of("/local/file"))
          .to(Scp.Target.of("user", "host", "/remote/file"));
    }

    private void perform(Action action) {
      ReportingActionPerformer.create().perform(action);
    }
  }

  public static class WithoutUsername extends Base {
    @Override
    public CommanderConfig initializerFor(String host) {
      return "localhost".equals(host) ?
          CommanderConfig.DEFAULT :
          new CommanderConfig() {
            @Override
            public Shell shell() {
              return new SshShellBuilder(host)
                  .program("ssh")
                  .enableAuthAgentConnectionForwarding()
                  .sshOptions(sshOptions())
                  .build();
            }
          };
    }

    @Override
    public Function<String, String> substringAfterExpectedRegexesForSshOptions() {
      return substringAfterRegex("ssh")
          .after("-A")
          .after("-o StrictHostkeyChecking=no")
          .after("-o PasswordAuthentication=no")
          .after(hostName())
          .$();
    }

    @Override
    Function<String, String> substringAfterExpectedRegexesForSshOptions_Scp() {
      return substringAfterRegex("scp")
          .after("-o").after("StrictHostkeyChecking=no")
          .after("-o").after("PasswordAuthentication=no")
          .after("'/local/file'")
          .after("'user@host:/remote/file'")
          .$();
    }
  }

  public static class WithUsername extends Base {
    @Override
    public CommanderConfig initializerFor(String host) {
      return "localhost".equals(host) ?
          CommanderConfig.DEFAULT :
          new CommanderConfig() {
            @Override
            public Shell shell() {
              return new SshShellBuilder(host)
                  .program("ssh")
                  .user(userName())
                  .enableAuthAgentConnectionForwarding()
                  .sshOptions(sshOptions())
                  .build();
            }
          };
    }

    @Override
    public Function<String, String> substringAfterExpectedRegexesForSshOptions() {
      return substringAfterRegex("ssh")
          .after("-A")
          .after("-o StrictHostkeyChecking=no")
          .after("-o PasswordAuthentication=no")
          .after(String.format("%s@%s", userName(), hostName()))
          .$();
    }

    @Override
    Function<String, String> substringAfterExpectedRegexesForSshOptions_Scp() {
      return substringAfterRegex("scp")
          .after("-o").after("StrictHostkeyChecking=no")
          .after("-o").after("PasswordAuthentication=no")
          .after("'/local/file'")
          .after("'user@host:/remote/file'")
          .$();
    }
  }

  public static class WithCustomSshOptions1 extends Base {

    private final SshOptions sshOptions = new SshOptions() {
      @Override
      public boolean ipv4() {
        return true;
      }

      @Override
      public boolean ipv6() {
        return false;
      }

      @Override
      public boolean compression() {
        return true;
      }

      @Override
      public List<String> jumpHosts() {
        return Collections.emptyList();
      }

      @Override
      public Optional<String> cipherSpec() {
        return Optional.empty();
      }

      @Override
      public Optional<String> configFile() {
        return Optional.of("ssh_config");
      }

      @Override
      public Optional<String> identity() {
        return Optional.empty();
      }

      @Override
      public List<String> sshOptions() {
        return emptyList();
      }

      @Override
      public OptionalInt port() {
        return OptionalInt.of(9999);
      }

      @Override
      public boolean quiet() {
        return false;
      }

      @Override
      public boolean verbose() {
        return true;
      }
    };

    @Override
    public CommanderConfig initializerFor(String host) {
      return "localhost".equals(host) ?
          new CommanderConfig() {
            @Override
            public SshOptions sshOptions() {
              return sshOptions;
            }
          } :
          new CommanderConfig() {
            @Override
            public Shell shell() {
              return new SshShellBuilder(host)
                  .program("ssh")
                  .user(userName())
                  .enableAuthAgentConnectionForwarding()
                  .sshOptions(sshOptions())
                  .build();
            }

            @Override
            public SshOptions sshOptions() {

              return sshOptions;
            }
          };
    }

    @Override
    public Function<String, String> substringAfterExpectedRegexesForSshOptions() {
      return substringAfterRegex("ssh")
          .after("-A")
          .after("-4")
          .after("-F")
          .after("-p 9999")
          .after("-v")
          .after(String.format("%s@%s", userName(), hostName()))
          .$();
    }

    @Override
    Function<String, String> substringAfterExpectedRegexesForSshOptions_Scp() {
      return substringAfterRegex("scp")
          .after("-4")
          .after("-C")
          .after("-F ssh_config")
          .after("-P 9999")
          .after("-v")
          .after("'/local/file'")
          .after("'user@host:/remote/file'")
          .$();
    }
  }

  public static class WithCustomSshOptions2 extends Base {
    private final SshOptions sshOptions = new SshOptions() {
      @Override
      public boolean ipv4() {
        return false;
      }

      @Override
      public boolean ipv6() {
        return true;
      }

      @Override
      public boolean compression() {
        return false;
      }

      @Override
      public List<String> jumpHosts() {
        return singletonList("jumphost1,jumphost2");
      }

      @Override
      public Optional<String> cipherSpec() {
        return Optional.of("cipher_spec");
      }

      @Override
      public Optional<String> configFile() {
        return Optional.empty();
      }

      @Override
      public Optional<String> identity() {
        return Optional.of("id_rsa");
      }

      @Override
      public List<String> sshOptions() {
        return emptyList();
      }

      @Override
      public OptionalInt port() {
        return OptionalInt.empty();
      }

      @Override
      public boolean quiet() {
        return true;
      }

      @Override
      public boolean verbose() {
        return false;
      }
    };

    @Override
    public CommanderConfig initializerFor(String host) {
      return "localhost".equals(host) ?
          new CommanderConfig() {
            @Override
            public SshOptions sshOptions() {
              return sshOptions;
            }
          } :
          new CommanderConfig() {
            @Override
            public Shell shell() {
              return new SshShellBuilder(host)
                  .program("ssh")
                  .user(userName())
                  .enableAuthAgentConnectionForwarding()
                  .sshOptions(sshOptions())
                  .build();
            }

            @Override
            public SshOptions sshOptions() {
              return sshOptions;
            }
          };
    }

    @Override
    public Function<String, String> substringAfterExpectedRegexesForSshOptions() {
      return substringAfterRegex("ssh")
          .after("-A")
          .after("-6")
          .after("-c cipher_spec")
          .after("-i id_rsa")
          .after("-q")
          .after(String.format("%s@%s", userName(), hostName()))
          .$();
    }

    @Override
    Function<String, String> substringAfterExpectedRegexesForSshOptions_Scp() {
      return substringAfterRegex("scp")
          .after("-6")
          .after("-c cipher_spec")
          .after("-i id_rsa")
          .after("-q")
          .after("'/local/file'")
          .after("'user@host:/remote/file'")
          .$();
    }
  }

  public static String hostName() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }

  public static String userName() {
    return System.getProperty("user.name");
  }
}