package com.github.dakusui.actionunit.core.context;

import java.util.Formattable;
import java.util.Formatter;

import static com.github.dakusui.actionunit.utils.InternalUtils.objectToStringIfOverridden;

public interface Printable extends Formattable {
  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format(objectToStringIfOverridden(this, () -> "(noname)"));
  }
}
