package com.github.dakusui.actionunit.core.context;

import com.github.dakusui.actionunit.core.Context;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.dakusui.actionunit.utils.Checks.requireArgument;
import static com.github.dakusui.printables.Printables.isKeyOf;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public interface Params {
  List<String> paramNames();

  <T> T valueOf(String parameterName);

  static Params create(Context context, String... paramNames) {
    return new Params() {
      Map<String, Object> values = new LinkedHashMap<String, Object>() {{
        for (String each : paramNames) {
          put(each, context.valueOf(each));
        }
      }};

      @Override
      public List<String> paramNames() {
        return asList(paramNames);
      }

      @SuppressWarnings("unchecked")
      @Override
      public <T> T valueOf(String parameterName) {
        return (T) values.get(requireArgument(isKeyOf(values), requireNonNull(parameterName)));
      }

      @Override
      public String toString() {
        return paramNames().stream()
            .map(each -> format("%s=%s", each, valueOf(each)))
            .collect(joining(",", "[", "]"));
      }
    };
  }
}
