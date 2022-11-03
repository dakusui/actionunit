package com.github.dakusui.actionunit.actions.cmd;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static java.util.Collections.unmodifiableList;

public interface ProgramNameResolver extends BiFunction<String, String, String> {
  class Builder {
    interface Entry {
      BiPredicate<String, String> matcher();

      Function<String, String> resolver();

      static Entry create(BiPredicate<String, String> matcher, Function<String, String> resolver) {
        return new Entry() {
          @Override
          public BiPredicate<String, String> matcher() {
            return matcher;
          }

          @Override
          public Function<String, String> resolver() {
            return resolver;
          }
        };
      }
    }

    final List<Entry> entries = new LinkedList<>();

    public Builder() {
    }

    public Builder register(BiPredicate<String, String> matcher, Function<String, String> programNameResolver) {
      this.entries.add(Entry.create(matcher, programNameResolver));
      return this;
    }

    public ProgramNameResolver build() {
      return new ProgramNameResolver() {
        final List<Entry> entries = unmodifiableList(new ArrayList<>(Builder.this.entries));

        @Override
        public String apply(String host, String programName) {
          return this.entries.stream()
              .filter(each -> each.matcher().test(host, programName))
              .findFirst()
              .map(v -> v.resolver().apply(programName))
              .orElseThrow(NoSuchElementException::new);
        }
      };
    }
  }

  static ProgramNameResolver create() {
    return new Builder().register((h, c) -> true, c -> c).build();
  }
}
