package com.github.dakusui.actionunit.core.context;

import com.github.dakusui.actionunit.utils.StableTemplatingUtils;

import java.util.TreeMap;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import static com.github.dakusui.actionunit.utils.InternalUtils.objectToStringIfOverridden;
import static java.util.stream.Collectors.joining;

public enum ContextFunctions {
  ;

  public static final IntFunction<String> PLACE_HOLDER_FORMATTER = i -> String.format("{{%s}}", i);

  public static ContextPredicate.Builder contextPredicateFor(String... variableNames) {
    return new ContextPredicate.Builder(variableNames);
  }

  public static ContextConsumer.Builder contextConsumerFor(String... variableNames) {
    return new ContextConsumer.Builder(variableNames);
  }

  public static <R> ContextFunction.Builder<R> contextFunctionFor(String... variableNames) {
    return new ContextFunction.Builder<>(variableNames);
  }

  static String describeFunctionalObject(Object f, final IntFunction<String> placeHolderFormatter, String... v) {
    return String.format("(%s)->%s",
        String.join(",", v),
        StableTemplatingUtils.template(
            objectToStringIfOverridden(
                f,
                () -> "(noname)" +
                    IntStream.range(0, v.length)
                        .mapToObj(placeHolderFormatter)
                        .collect(joining(", ", "(", ")"))),
            new TreeMap<String, Object>() {{
              IntStream.range(0, v.length).forEach(
                  i -> put(placeHolderFormatter.apply(i), v[i])
              );
            }}
        ));
  }
}
