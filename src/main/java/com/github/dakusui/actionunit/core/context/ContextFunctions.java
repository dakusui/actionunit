package com.github.dakusui.actionunit.core.context;

import com.github.dakusui.actionunit.actions.ContextVariable;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.multiparams.MultiParamsContextConsumerBuilder;
import com.github.dakusui.actionunit.core.context.multiparams.MultiParamsContextFunctionBuilder;
import com.github.dakusui.actionunit.core.context.multiparams.MultiParamsContextPredicateBuilder;
import com.github.dakusui.actionunit.utils.Checks;
import com.github.dakusui.actionunit.utils.InternalUtils;
import com.github.dakusui.actionunit.utils.StableTemplatingUtils;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import static com.github.dakusui.actionunit.utils.InternalUtils.objectToStringIfOverridden;
import static com.github.dakusui.actionunit.utils.InternalUtils.toStringIfOverriddenOrNoname;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public enum ContextFunctions {
  ;

  public static final Function<ContextVariable[], IntFunction<String>> DEFAULT_PLACE_HOLDER_FORMATTER = variables -> i -> String.format("{{%s}}", i);

  public static final Function<ContextVariable[], IntFunction<String>> PLACE_HOLDER_FORMATTER_BY_NAME = variables -> i -> String.format("{{%s}}", variables[i]);

  public static MultiParamsContextPredicateBuilder multiParamsPredicateFor(ContextVariable... variables) {
    return new MultiParamsContextPredicateBuilder(variables);
  }

  public static MultiParamsContextConsumerBuilder multiParamsConsumerFor(ContextVariable... variables) {
    return new MultiParamsContextConsumerBuilder(variables);
  }

  public static <R> MultiParamsContextFunctionBuilder<R> multiParamsFunctionFor(ContextVariable... variables) {
    return new MultiParamsContextFunctionBuilder<>(variables);
  }

  public static String describeFunctionalObject(Object f, final IntFunction<String> placeHolderFormatter, ContextVariable... variables) {
    return String.format("(%s)->%s",
        String.join(",", Arrays.stream(variables).map(ContextVariable::variableName).toArray(String[]::new)),
        summarize(StableTemplatingUtils.template(
            objectToStringIfOverridden(f, obj -> formatPlaceHolders(obj, placeHolderFormatter, variables)),
            new TreeMap<String, Object>() {{
              IntStream.range(0, variables.length).forEach(
                  i -> put(placeHolderFormatter.apply(i), String.format("${%s}", variables[i]))
              );
            }}), 60));
  }

  private static String formatPlaceHolders(Object obj, IntFunction<String> placeHolderFormatter, ContextVariable[] v) {
    return InternalUtils.fallbackFormatter().apply(obj) +
        IntStream.range(0, v.length)
            .mapToObj(placeHolderFormatter)
            .collect(joining(", ", "(", ")"));
  }

  public static <R> ContextConsumer assignTo(ContextVariable contextVariable, Function<Context, R> value) {
    requireNonNull(contextVariable);
    requireNonNull(value);
    return ContextConsumer.of(
        () -> format("assignTo[%s](%s)", contextVariable.variableName(), toStringIfOverriddenOrNoname(value)),
        (c) -> c.assignTo(contextVariable.internalVariableName(), value.apply(c)));
  }

  public static <R> ContextFunction<R> immediateOf(R value) {
    return ContextFunction.of(() -> format("%s", value), c -> value
    );
  }

  public static <R> ContextFunction<R> contextValueOf(ContextVariable contextVariable) {
    requireNonNull(contextVariable);
    return ContextFunction.of(
        () -> format("valueOf[%s]", contextVariable.variableName()),
        contextVariable::resolve
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

  public static <R> ContextConsumer printTo(PrintStream ps, Function<Context, R> value) {
    requireNonNull(ps);
    return ContextConsumer.of(
        () -> format("printTo[%s](%s)", prettyClassName(ps.getClass()), value),
        c -> ps.println(value.apply(c))
    );
  }

  public static <R> ContextConsumer writeTo(Consumer<R> sink, Function<Context, R> value) {
    requireNonNull(sink);
    return ContextConsumer.of(
        () -> format("writeTo[%s](%s)", prettyClassName(sink.getClass()), value),
        c -> sink.accept(value.apply(c))
    );
  }

  public static String prettyClassName(Class<?> c) {
    String ret = c.getSimpleName();
    if (ret.equals("")) {
      Class<?> mostRecentNamedSuper = mostRecentNamedSuperOf(c);
      if (!mostRecentNamedSuper.equals(Object.class))
        ret = format("(anon:%s)", mostRecentNamedSuperOf(c).getSimpleName());
      else
        ret = Arrays.stream(c.getInterfaces()).map(Class::getSimpleName).collect(joining
            (",", "(anon:", ")"));
    }
    return ret.replaceFirst("\\$\\$Lambda\\$\\d+/(0x)?[\\da-f]+$", ".lambda");
  }

  private static Class<?> mostRecentNamedSuperOf(Class<?> c) {
    if ("".equals(c.getSimpleName()))
      return mostRecentNamedSuperOf(c.getSuperclass());
    return c;
  }

  public static String summarize(String commandLine, int length) {
    Checks.requireArgument(l -> l > 3, length);
    return requireNonNull(commandLine).length() < length ?
        replaceNewLines(commandLine) :
        replaceNewLines(commandLine).substring(0, length - 3) + "...";
  }

  private static String replaceNewLines(String s) {
    return s.replaceAll("\n", " ");
  }
}
