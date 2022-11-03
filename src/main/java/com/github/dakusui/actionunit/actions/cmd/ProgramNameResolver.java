package com.github.dakusui.actionunit.actions.cmd;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static java.util.Collections.unmodifiableList;

/**
 * A trivial interface to alias {@code BiFunction<String, String, String>} for readability.
 *
 * Sometimes, a program (`echo`, `ls`, etc.) may behave because of its implementation on a certain host.
 * To achieve intended behavior on the host, sometimes we need to specify a full-path of a command.
 * For instance, `echo` is a built-in of `bash` and to use `-E` option of `echo` command (not built-in!),
 * we must specify `/bin/echo`, instead of just `echo`.
 *
 * `CommanderConfig` has a method `programNameResolver`, which returns {@code BiFunction<String, String, String>}.
 * This bi-function is used to figure out an actual program name to be run on a certain host.
 *
 * That is, a pair of string values, which are host name and a command name are given to the bi-function, which is supposed to return an actual program name executed on the host as the given command name.
 */
public interface ProgramNameResolver extends BiFunction<String, String, String> {
  /**
   * A builder to create a {@link ProgramNameResolver} instance easily.
   * The instance built by this class behaves as follows, when a pair of hostname and command name:
   *
   * Check a list of entries ({@link Entry}) one by one if its `matcher` returns `true` for the pair.
   * If it finds an entry whose `matcher` gives true, applies a command name to its `resolver` and returns the value as the result of the program name resolver.
   * If no entry matches with the host name and the command name, an {@link NoSuchElementException} will be thrown.
   */
  class Builder {
    /**
     * A conversion rule entry.
     */
    interface Entry {
      /**
       * A bi-predicate that checks if a given pair of a host name and a command name matches this entry.
       * The first parameter corresponds to the host name and the second one corresponds to the command name.
       *
       * @return matcher bi-predicate.
       */
      BiPredicate<String, String> matcher();

      /**
       * Returns a function that maps a command name to an actual program name, executed on the host, checked by the bi-predicate retuened by `matcher()` method.
       *
       * @return A function that maps a command name to an actual program name
       */
      Function<String, String> resolver();

      /**
       * Createsn an instance of {@link Entry}.
       *
       * @param matcher  A matcher bi-predicate.
       * @param resolver A resolver function.
       * @return A new {@link Entry} objeect.
       * @see this#matcher()
       * @see this#resolver()
       */
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

    /**
     * Constructs a new {@link ProgramNameResolver.Builder} object.
     */
    public Builder() {
    }

    /**
     * Registers a transforming rule for program names.
     *
     * Note that an entry registered later is more prioritized than ones registered earlier.
     *
     * @param matcher             A predicate to decide if program name resolution by {@code programNameResolver} should be applied.
     * @param programNameResolver A function that converts a program name for a host.
     * @return This object.
     */
    public Builder register(BiPredicate<String, String> matcher, Function<String, String> programNameResolver) {
      this.entries.add(0, Entry.create(matcher, programNameResolver));
      return this;
    }

    /**
     * Builds a new {@link ProgramNameResolver} object.
     *
     * @return A new {@link ProgramNameResolver} object
     */
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

  /**
   * Creates a {@link ProgramNameResolver} instance with a handy default for UNIX environment.
   *
   * @return An instance for UNIX environment.
   */
  static ProgramNameResolver createForUnix() {
    return new Builder()
        .register((h, c) -> true, c -> c)
        .register((h, c) -> "echo".equals(c), c -> "/bin/echo")
        .build();
  }
}
