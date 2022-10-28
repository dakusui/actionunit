package com.github.dakusui.actionunit.actions.cmd.unix;

import java.util.*;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

/**
 * Models options common to both {@code ssh} and {@code scp} commands. (see below).
 * ----
 * -4               : IPv4
 * -6               : IPv6
 * -C               : compression
 * -c               : cipher spec
 * -F               : config file
 * -i               : identity
 * -o               : ssh option
 * -p (-P for scp)  : port
 * -q               : quiet
 * -v               : verbose
 * ----
 */
public interface SshOptions {
  boolean authAgentConnectionForwardingEnabled();

  boolean ipv4();

  boolean ipv6();

  boolean compression();

  List<String> jumpHosts();

  Optional<String> cipherSpec();

  Optional<String> configFile();

  Optional<String> identity();

  List<String> sshOptions();

  OptionalInt port();

  boolean quiet();

  boolean verbose();

  default List<String> formatOptionsWith(Formatter formatter) {
    return requireNonNull(formatter).format(this);
  }

  class Impl implements SshOptions {
    final boolean      ipv4;
    final boolean      ipv6;
    final boolean      compression;
    final List<String> jumpHosts;
    final String       cipherSpec;
    final String       configFile;
    final String       identity;
    final List<String> sshOptions;
    final Integer      port;
    final boolean      quiet;
    final boolean      verbose;
    final boolean      authAgentConnectionForwarding;

    public Impl(boolean authAgentConnectionForwardingEnabled, boolean ipv4, boolean ipv6, boolean compression, List<String> jumpHosts, String cipherSpec, String configFile, String identity, List<String> sshOptions, Integer port, boolean quiet, boolean verbose) {
      this.authAgentConnectionForwarding = authAgentConnectionForwardingEnabled;
      this.ipv4 = ipv4;
      this.ipv6 = ipv6;
      this.compression = compression;
      this.jumpHosts = unmodifiableList(new ArrayList<>(requireNonNull(jumpHosts)));
      this.cipherSpec = cipherSpec;
      this.configFile = configFile;
      this.identity = identity;
      this.sshOptions = unmodifiableList(new ArrayList<>(requireNonNull(sshOptions)));
      this.port = port;
      this.quiet = quiet;
      this.verbose = verbose;
    }


    @Override
    public boolean authAgentConnectionForwardingEnabled() {
      return authAgentConnectionForwarding;
    }

    @Override
    public boolean ipv4() {
      return ipv4;
    }

    @Override
    public boolean ipv6() {
      return ipv6;
    }

    @Override
    public boolean compression() {
      return compression;
    }

    @Override
    public List<String> jumpHosts() {
      return jumpHosts;
    }

    @Override
    public Optional<String> cipherSpec() {
      return Optional.ofNullable(cipherSpec);
    }

    @Override
    public Optional<String> configFile() {
      return Optional.ofNullable(configFile);
    }

    @Override
    public Optional<String> identity() {
      return Optional.ofNullable(identity);
    }

    @Override
    public List<String> sshOptions() {
      return sshOptions;
    }

    @Override
    public OptionalInt port() {
      return port != null ? OptionalInt.of(port) : OptionalInt.empty();
    }

    @Override
    public boolean quiet() {
      return quiet;
    }

    @Override
    public boolean verbose() {
      return verbose;
    }
  }

  class Builder {
    private       String       identity;
    private final List<String> sshOptions = new LinkedList<>();
    private final List<String> jumpHosts  = new LinkedList<>();
    private       boolean      ipv4;
    private       boolean      ipv6;
    private       boolean      compression;
    private       String       cipherSpec;
    private       String       configFile;
    private       Integer      port;
    private       boolean      quiet;
    private       boolean      verbose;
    private       boolean      authAgentConnectionForwarding;

    public Builder() {
      this.ipv4(false)
          .ipv6(false)
          .compression(false)
          .cipherSpec(null)
          .configFile(null)
          .identity(null)
          .port(null)
          .quiet(false)
          .verbose(false);
    }

    public Builder authAgentConnectionForwarding(boolean enable) {
      this.authAgentConnectionForwarding = enable;
      return this;
    }

    public Builder ipv4(boolean enable) {
      this.ipv4 = enable;
      return this;
    }

    public Builder ipv6(boolean enable) {
      this.ipv6 = enable;
      return this;
    }

    public Builder compression(boolean enable) {
      this.compression = enable;
      return this;
    }

    public Builder cipherSpec(String cipherSpec) {
      this.cipherSpec = cipherSpec;
      return this;
    }

    public Builder configFile(String configFile) {
      this.configFile = configFile;
      return this;
    }

    public Builder port(Integer port) {
      this.port = port;
      return this;
    }

    public Builder quiet(boolean quiet) {
      this.quiet = quiet;
      return this;
    }

    public Builder verbose(boolean verbose) {
      this.verbose = verbose;
      return this;
    }


    public Builder identity(String identity) {
      this.identity = identity;
      return this;
    }

    public Builder addJumpHost(String jumpHost) {
      this.jumpHosts.add(requireNonNull(jumpHost));
      return this;
    }

    public Builder addSshOption(String option) {
      sshOptions.add(option);
      return this;
    }

    public Builder addSshOption(String option, String value) {
      return this.addSshOption(String.format("%s=%s", option, value));
    }

    public Builder disablePasswordAuthentication() {
      return this.addSshOption("PasswordAuthentication", "no");
    }

    public Builder disableStrictHostkeyChecking() {
      return this.addSshOption("StrictHostkeyChecking", "no");
    }

    public SshOptions build() {
      return new SshOptions.Impl(
          authAgentConnectionForwarding,
          ipv4,
          ipv6,
          compression,
          jumpHosts,
          cipherSpec,
          configFile,
          identity,
          sshOptions,
          port,
          quiet,
          verbose);
    }
  }

  @FunctionalInterface
  interface Formatter {
    List<String> format(SshOptions sshOptions);

    static Formatter forSsh() {
      return sshOptions -> new LinkedList<String>() {{
        if (sshOptions.authAgentConnectionForwardingEnabled())
          add("-A");
        if (sshOptions.ipv4())
          add("-4");
        if (sshOptions.ipv6())
          add("-6");
        if (sshOptions.compression())
          add("-C");
        sshOptions.configFile().ifPresent(v -> {
          add("-F");
          add(v);
        });
        if (!sshOptions.jumpHosts().isEmpty())
          add("-J " + String.join(",", sshOptions.jumpHosts()));
        sshOptions.cipherSpec().ifPresent(v -> {
          add("-c");
          add(v);
        });
        sshOptions.identity().ifPresent(v -> {
          add("-i");
          add(v);
        });
        sshOptions.sshOptions().forEach(v -> {
          add("-o");
          add(v);
        });
        sshOptions.port().ifPresent(v -> {
          add("-p");
          add(Objects.toString(v));
        });
        if (sshOptions.quiet())
          add("-q");
        if (sshOptions.verbose())
          add("-v");
      }};
    }

    static Formatter forScp() {
      return sshOptions -> new LinkedList<String>() {{
        if (sshOptions.authAgentConnectionForwardingEnabled())
          add("-A");
        if (sshOptions.ipv4())
          add("-4");
        if (sshOptions.ipv6())
          add("-6");
        if (sshOptions.compression())
          add("-C");
        sshOptions.configFile().ifPresent(v -> {
          add("-F");
          add(v);
        });
        if (!sshOptions.jumpHosts().isEmpty())
          add("-J " + String.join(",", sshOptions.jumpHosts()));
        sshOptions.cipherSpec().ifPresent(v -> {
          add("-c");
          add(v);
        });
        sshOptions.identity().ifPresent(v -> {
          add("-i");
          add(v);
        });
        sshOptions.sshOptions().forEach(v -> {
          add("-o");
          add(v);
        });
        sshOptions.port().ifPresent(v -> {
          add("-P");
          add(Objects.toString(v));
        });
        if (sshOptions.quiet())
          add("-q");
        if (sshOptions.verbose())
          add("-v");
      }};
    }
  }
}
