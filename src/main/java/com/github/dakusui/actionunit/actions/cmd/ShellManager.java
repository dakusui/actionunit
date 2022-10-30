package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.actions.cmd.unix.SshOptions;
import com.github.dakusui.actionunit.actions.cmd.unix.SshShell;
import com.github.dakusui.processstreamer.core.process.Shell;

import java.util.Optional;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public interface ShellManager {
  Shell shellFor(String host);

  String userForRemote(String host);

  Optional<SshOptions> sshOptionsFor(String host);


  interface Default extends ShellManager {
    default Shell shellForLocal() {
      return shellFor("localhost");
    }

    @Override
    default Shell shellFor(String host) {
      if (isLocal(host))
        return Shell.LOCAL_SHELL;

      return new SshShell.Builder(host, sshOptionsFor(host).orElseGet(SshOptions::emptySshOptions))
          .user(userForRemote(host))
          .program("ssh")
          .build();
    }

    default Optional<SshOptions> sshOptionsFor(String host) {
      if (isLocal(host))
        return Optional.empty();
      return Optional.of(sshOptionForRemote(host));
    }

    SshOptions sshOptionForRemote(String remoteHost);


    default boolean isLocal(String host) {
      return "localhost".equals(host) || "127.0.0.1".equals(host) || "::1".equals(host);
    }

    class Builder {
      private Function<String, SshOptions> sshOptionsResolver;
      private Function<String, String>     userNameResolver;

      public Builder() {
        this.sshOptionsResolver(h -> new SshOptions.Builder()
                .disablePasswordAuthentication()
                .disableStrictHostkeyChecking()
                .build())
            .userNameResolver(h -> System.getProperty("user.name"));
      }

      public Builder userNameResolver(Function<String, String> resolver) {
        this.userNameResolver = requireNonNull(resolver);
        return this;
      }

      public Builder sshOptionsResolver(Function<String, SshOptions> resolver) {
        this.sshOptionsResolver = requireNonNull(resolver);
        return this;
      }

      public ShellManager build() {
        return new ShellManager.Default() {
          final Function<String, SshOptions> sshOptionsResolver = Builder.this.sshOptionsResolver;
          final Function<String, String> userNameResolver = Builder.this.userNameResolver;

          @Override
          public SshOptions sshOptionForRemote(String remoteHost) {
            return sshOptionsResolver.apply(remoteHost);
          }

          @Override
          public String userForRemote(String remoteHost) {
            return userNameResolver.apply(remoteHost);
          }
        };
      }
    }
  }
}
