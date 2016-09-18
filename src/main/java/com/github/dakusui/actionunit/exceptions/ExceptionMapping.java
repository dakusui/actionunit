package com.github.dakusui.actionunit.exceptions;

import java.lang.reflect.InvocationTargetException;

import static java.lang.String.format;

public interface ExceptionMapping<AE extends RuntimeException> {
  Class<? extends AE> getApplicationExceptionClass();

  Class<? extends Throwable> getNativeExceptionClass();

  /**
   * @param <AE> Application exception
   */
  interface Resolver<AE extends RuntimeException> {
    AE resolve(Throwable t);

    enum Factory {
      ;

      public static <T extends Enum & ExceptionMapping<AE>, AE extends RuntimeException> Resolver<AE> create(final Class<T> mappingClass) {
        return new Resolver<AE>() {
          @Override
          public AE resolve(Throwable t) {
            if (t == null) {
              throw new Error("null is not allowed here");
            }
            for (T each : mappingClass.getEnumConstants()) {
              if (matches(each, t.getClass())) {
                throw instantiate(each.getApplicationExceptionClass(), t);
              }
            }
            ////
            // For unknown type of checked exception. Once this line is executed, consider
            // adding new mapping to your enumeration.
            throw error(
                "Consider adding a new mapping to " + mappingClass.getCanonicalName() +
                    "for " + t.getClass().getCanonicalName() + ". : %s", t);
          }

          private boolean matches(T each, Class<? extends Throwable> originalExceptionClass) {
            return each.getNativeExceptionClass().isAssignableFrom(originalExceptionClass);
          }

          private AE instantiate(Class<? extends AE> exceptionClass, Throwable nested) {
            try {
              return exceptionClass.getDeclaredConstructor(String.class, Throwable.class).newInstance(nested.getMessage(), nested);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
              Throwable t = e instanceof InvocationTargetException
                  ? ((InvocationTargetException) e).getTargetException()
                  : e;
              throw error(format(
                  "Failed to instantiate '%s' through its constructor whose parameters are (String, Throwable)"
                      + " since %s was thrown."
                      + " Make sure it exists, is public, and does not throw an exception.",
                  exceptionClass,
                  t.getClass().getSimpleName()) + ": %s",
                  t);
            }
          }

          private Error error(String messageFormat, Throwable nested) {
            throw new Error(format(messageFormat, nested.getMessage()), nested);
          }
        };
      }
    }
  }
}
