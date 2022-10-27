package com.github.dakusui.actionunit.actions.cmd.unix;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

/**
 * Models options common to both {@code ssh} and {@code scp} commands. (see below).
 * <pre>
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
 * </pre>
 */
public interface SshOptions {
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

  default List<String> options(Formatter formatter) {
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

    public Impl(boolean ipv4, boolean ipv6, boolean compression, List<String> jumpHosts, String cipherSpec, String configFile, String identity, List<String> sshOptions, Integer port, boolean quiet, boolean verbose) {
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
    private final SshOptions   base;
    private       String       identity;
    private final List<String> sshOptions = new LinkedList<>();
    private final List<String> jumpHosts  = new LinkedList<>();

    public Builder() {
      this(new Impl(false, false, false, emptyList(), null, null, null, emptyList(), null, false, false));
    }

    public Builder(SshOptions base) {
      this.base = requireNonNull(base);
    }


    public Builder identity(String identity) {
      this.identity = requireNonNull(identity);
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
          base.ipv4(),
          base.ipv6(),
          base.compression(),
          jumpHosts,
          base.cipherSpec().orElse(null),
          base.configFile().orElse(null),
          identity,
          sshOptions,
          base.port().isPresent() ? base.port().getAsInt() : null,
          base.quiet(),
          base.verbose());
    }
  }

  @FunctionalInterface
  interface Formatter {
    List<String> format(SshOptions sshOptions);

    static Formatter forSsh() {
      return sshOptions -> new LinkedList<String>() {{
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
