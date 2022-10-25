package com.github.dakusui.actionunit.actions.cmd.unix;

import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.actionunit.actions.cmd.CommanderConfig;
import com.github.dakusui.actionunit.core.Context;

import java.util.function.Function;

import static com.github.dakusui.actionunit.core.context.ContextFunctions.immediateOf;
import static java.util.Objects.requireNonNull;

public class Curl extends Commander<Curl> {
  public enum DataFormat {
    ASCII("--data"),
    BINARY("--data-binary"),
    RAW("--data-raw"),
    URL_ENCODE("--data-urlencode"),
    ;

    private final String format;

    DataFormat(String format) {
      this.format = format;
    }

    String getFormat() {
      return this.format;
    }
  }

  public Curl(CommanderConfig config) {
    super(config);
    config.setCommandNameFor(this);
  }

  public Curl post() {
    return this.addOption("-X").addOption("POST");
  }

  public Curl put() {
    return this.addOption("-X").addOption("PUT");
  }

  public Curl get() {
    return this.addOption("-X").addOption("GET");
  }

  public Curl delete() {
    return this.addOption("-X").addOption("DELETE");
  }

  public Curl url(String url) {
    return this.add(url);
  }

  public Curl url(Function<Context, String> url) {
    return this.add(url);
  }

  public Curl insecure() {
    return this.addOption("--insecure");
  }

  public Curl silent() {
    return this.addOption("-s");
  }

  public Curl asciiData(String content) {
    return this.asciiData(immediateOf(content));
  }

  public Curl asciiData(Function<Context, String> content) {
    return this.data(DataFormat.ASCII, content);
  }

  public Curl binaryData(String content) {
    return this.binaryData(immediateOf(content));
  }

  public Curl binaryData(Function<Context, String> content) {
    return this.data(DataFormat.BINARY, content);
  }

  public Curl rawData(String content) {
    return this.rawData(immediateOf(content));
  }

  public Curl rawData(Function<Context, String> content) {
    return this.data(DataFormat.RAW, content);
  }

  public Curl urlEncodedData(String content) {
    return this.urlEncodedData(immediateOf(content));
  }

  public Curl urlEncodedData(Function<Context, String> content) {
    return this.data(DataFormat.URL_ENCODE, content);
  }

  public Curl data(DataFormat format, Function<Context, String> content) {
    return this.addOption(format.getFormat()).add(requireNonNull(content));
  }

  public Curl includeHeader() {
    return this.addOption("-i");
  }

  public Curl headerOnly() {
    return this.addOption("-I");
  }
}
