package com.github.dakusui.actionunit.utils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.dakusui.actionunit.utils.Checks.requireArgument;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public enum StableTemplatingUtils {
  ;

  private static class PositionedPlaceHolder {
    final String placeHolder;
    final int    position;

    private PositionedPlaceHolder(String placeHolder, int position) {
      requireArgument(s -> s.length() > 0, requireNonNull(placeHolder));
      requireArgument(i -> i > 0, position);
      this.placeHolder = placeHolder;
      this.position = position;
    }

    static PositionedPlaceHolder of(String placeHolder, int position) {
      return new PositionedPlaceHolder(placeHolder, position);
    }
  }

  public static String template(String template, SortedMap<String, Object> mapping) {
    AtomicInteger lastPosition = new AtomicInteger(0);
    StringBuilder b = new StringBuilder();
    positionToPlaceHolder(template, mapping.keySet())
        .forEach(chainBiConsumers(
            (Integer position, String placeHolder) -> b.append(
                template,
                lastPosition.get(),
                position),
            (Integer position, String placeHolder) -> b.append(mapping.get(placeHolder)),
            (Integer position, String placeHolder) -> lastPosition.accumulateAndGet(
                placeHolder.length(),
                (i, j) -> position + j)
        ));
    if (lastPosition.get() < template.length())
      b.append(template, lastPosition.get(), template.length());
    return b.toString();
  }

  @SafeVarargs
  private static <T, U> BiConsumer<T, U> chainBiConsumers(BiConsumer<T, U> c1, BiConsumer<T, U>... c2) {
    BiConsumer<T, U> ret = c1;
    for (BiConsumer<T, U> each : c2)
      ret = ret.andThen(each);
    return ret;
  }

  static SortedMap<Integer, String> positionToPlaceHolder(String formatString, Collection<String> placeHolders) {
    return new TreeMap<Integer, String>() {{
      for (Optional<PositionedPlaceHolder> positionedPlaceHolderOptional = findFirstPlaceHolderFrom(
          formatString,
          0,
          new LinkedList<>(placeHolders));
           positionedPlaceHolderOptional.isPresent();
           positionedPlaceHolderOptional = findFirstPlaceHolderFrom(
               formatString,
               positionedPlaceHolderOptional.get().position + positionedPlaceHolderOptional.get().placeHolder.length(),
               new LinkedList<>(placeHolders)
           )) {
        PositionedPlaceHolder positionedPlaceHolder = positionedPlaceHolderOptional.get();
        this.put(positionedPlaceHolder.position, positionedPlaceHolder.placeHolder);
      }
    }};
  }

  private static Optional<PositionedPlaceHolder> findFirstPlaceHolderFrom(
      String template,
      int from,
      Collection<String> remainingPlaceHolders) {
    String templateSubString = template.substring(from);
    List<String> notFound = new LinkedList<>();
    AtomicReference<Optional<PositionedPlaceHolder>> ret = new AtomicReference<>(Optional.empty());
    remainingPlaceHolders.forEach(
        (String s) -> {
          int position = templateSubString.indexOf(s);
          if (position < 0)
            notFound.add(s);
          else {
            PositionedPlaceHolder found = PositionedPlaceHolder.of(s, from + position);
            if (!ret.get().isPresent() || found.position < ret.get().get().position)
              ret.set(Optional.of(found));
          }
        });
    remainingPlaceHolders.removeAll(notFound);
    return ret.get();
  }


  public static SortedMap<String, Object> toMapping(IntFunction<String> placeHolderComposer, Object[] argValues) {
    AtomicInteger i = new AtomicInteger();
    return parameterPlaceHolders(placeHolderComposer, argValues.length)
        .stream()
        .collect(toLinkedHashMap(placeHolder -> placeHolder, placeHolder -> argValues[i.getAndIncrement()]));
  }

  static List<String> parameterPlaceHolders(IntFunction<String> placeHolderComposer, int numParameters) {
    return IntStream.range(0, numParameters)
        .mapToObj(placeHolderComposer)
        .collect(toList());
  }

  private static <T, K, U> Collector<T, ?, SortedMap<K, U>> toLinkedHashMap(Function<? super T, ? extends K> keyMapper,
      Function<? super T, ? extends U> valueMapper) {
    return Collectors.toMap(keyMapper, valueMapper, throwingMerger(), TreeMap::new);
  }

  private static <T> BinaryOperator<T> throwingMerger() {
    return (u, v) -> {
      throw new IllegalStateException(String.format("Duplicate key %s", u));
    };
  }
}
