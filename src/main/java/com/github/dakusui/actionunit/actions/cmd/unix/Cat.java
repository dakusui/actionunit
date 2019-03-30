package com.github.dakusui.actionunit.actions.cmd.unix;

import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.actionunit.actions.cmd.CommanderInitializer;
import com.github.dakusui.actionunit.core.context.ContextFunction;

import java.io.File;
import java.util.Objects;

import static com.github.dakusui.actionunit.utils.Checks.requireState;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class Cat extends Commander<Cat> {
  /**
   * A tag for here-document
   */
  private String tag;

  public Cat(CommanderInitializer initializer) {
    super(initializer);
    this.tag = null;
    initializer.init(this);
  }

  public Cat beginHereDocument(String tag) {
    requireState(Objects::isNull, this.tag);
    this.tag = requireNonNull(tag);
    return this.append(" ").append("<<").append(tag).newLine();
  }

  /**
   * This method should only be used inside a here document.
   *
   * @return This object.
   */
  public Cat newLine() {
    requireState(Objects::nonNull, this.tag);
    return this.append(format("%n"));
  }

  public Cat write(String text) {
    requireState(Objects::nonNull, this.tag);
    return this.append(requireNonNull(text));
  }

  public Cat writeln(String text) {
    requireState(Objects::nonNull, this.tag);
    return this.append(requireNonNull(text)).newLine();
  }

  public Cat endHereDocument() {
    requireState(Objects::nonNull, this.tag);
    return this.append(this.tag);
  }

  public Cat lineNumber() {
    return this.append(" ").append("-n");
  }

  public Cat file(File file) {
    return this.file(requireNonNull(file).getAbsolutePath());
  }

  public Cat file(String fileName) {
    return add(fileName);
  }

  public Cat file(ContextFunction<String> fileName) {
    return add(fileName);
  }
}
