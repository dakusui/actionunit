package com.github.dakusui.actionunit.core.context;

import com.github.dakusui.actionunit.utils.StableTemplatingUtils;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import static com.github.dakusui.actionunit.utils.InternalUtils.objectToStringIfOverridden;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
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

  public static <R> ContextConsumer assignTo(String variableName, ContextFunction<R> value) {
    return ContextConsumer.of(
        () -> format("assignTo[%s](%s)", variableName, value),
        (c) -> c.assignTo(variableName, value.apply(c)));
  }

  public static <R> ContextFunction<R> immediateOf(R value) {
    return ContextFunction.of(
        () -> format("immediateOf[%s]", value),
        c -> value
    );
  }

  public static <R> ContextFunction<R> contextValueOf(String variableName) {
    requireNonNull(variableName);
    return ContextFunction.of(
        () -> format("valueOf[%s]", variableName),
        c -> c.valueOf(variableName)
    );
  }

  public static ContextConsumer throwIllegalArgument() {
    return ContextConsumer.of(
        () -> "throw IllegalArgumentException",
        (c) -> {
          throw new IllegalArgumentException();
        }
    );
  }

  public static <R> ContextConsumer printTo(PrintStream ps, ContextFunction<R> value) {
    requireNonNull(ps);
    return ContextConsumer.of(
        () -> format("printTo[%s](%s)", prettyClassName(ps.getClass()), value),
        c -> ps.println(value.apply(c))
    );
  }

  public static <R> ContextConsumer writeTo(Consumer<R> sink, ContextFunction<R> value) {
    requireNonNull(sink);
    return ContextConsumer.of(
        () -> format("writeTo[%s](%s)", prettyClassName(sink.getClass()), value),
        c -> sink.accept(value.apply(c))
    );
  }

  public static String prettyClassName(Class c) {
    String ret = c.getSimpleName();
    if (ret.equals("")) {
      Class mostRecentNamedSuper = mostRecentNamedSuperOf(c);
      if (!mostRecentNamedSuper.equals(Object.class))
        ret = format("(anon:%s)", mostRecentNamedSuperOf(c).getSimpleName());
      else
        ret = Arrays.stream(c.getInterfaces()).map(Class::getSimpleName).collect(joining
            (",", "(anon:", ")"));
    }
    return ret.replaceFirst("\\$\\$Lambda\\$[\\d]+/[\\d]+$", ".lambda");
  }

  private static Class mostRecentNamedSuperOf(Class c) {
    if ("".equals(c.getSimpleName()))
      return mostRecentNamedSuperOf(c.getSuperclass());
    return c;
  }
}
