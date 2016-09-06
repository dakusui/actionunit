package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.exceptions.ExceptionMapping;
import org.junit.Test;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.rmi.NoSuchObjectException;

import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResolverTest {
  public static class NonRegisteredException extends Exception {
    public NonRegisteredException(String message) {
      super(message);
    }
  }

  public static class RuntimeExceptionOnCreation extends ActionException {
    public RuntimeExceptionOnCreation(String message, Throwable t) {
      super(message, t);
      throw new RuntimeException(message);
    }
  }

  public static class ExceptionForInstantiationExceptionTest extends ActionException {
    public ExceptionForInstantiationExceptionTest(String message, Throwable t) throws InstantiationException {
      super(message, t);
    }
  }

  public static class NonPublicConstructor extends ActionException {
    private NonPublicConstructor(String message, Throwable t) throws IllegalAccessException {
      super(message, t);
    }
  }

  public static class NoMatchingConstructor extends ActionException {
    public NoMatchingConstructor(String message) {
      super(message);
    }
  }

  enum Mapping implements ExceptionMapping<ActionException> {
    @SuppressWarnings("unused")BROKEN_RUNTIME(FileNotFoundException.class, RuntimeExceptionOnCreation.class),
    @SuppressWarnings("unused")BROKEN_INSTANTIATION(EOFException.class, ExceptionForInstantiationExceptionTest.class),
    @SuppressWarnings("unused")BROKEN_NONPUBLIC(UnknownHostException.class, NonPublicConstructor.class),
    @SuppressWarnings("unused")BROKEN_NOMATCHING(NoSuchObjectException.class, NoMatchingConstructor.class),;

    private final Class<? extends Throwable>       from;
    private final Class<? extends ActionException> to;

    Mapping(Class<? extends Throwable> original, Class<? extends ActionException> applcation) {
      this.from = original;
      this.to = applcation;
    }

    @Override
    public Class<? extends ActionException> getApplicationExceptionClass() {
      return to;
    }

    @Override
    public Class<? extends Throwable> getNativeExceptionClass() {
      return from;
    }
  }

  ExceptionMapping.Resolver<ActionException> resolver = ExceptionMapping.Resolver.Factory.create(Mapping.class);

  @Test(expected = Error.class)
  public void givenUnknownException$whenResolve$thenError() {
    try {
      throw resolver.resolve(new NonRegisteredException("hello"));
    } catch (Error error) {
      assertThat(error.getMessage(),
          allOf(
              containsString("hello"),
              containsString(Mapping.class.getCanonicalName()),
              containsString(NonRegisteredException.class.getCanonicalName())
          ));
      throw error;
    }
  }

  @Test(expected = RuntimeException.class)
  public void givenBrokenExceptionThatCausesRuntimeException$whenResolve$thenError() throws Throwable {
    try {
      throw resolver.resolve(new FileNotFoundException("hello"));
    } catch (Error error) {
      assertThat(error.getMessage(),
          allOf(
              containsString("hello"),
              containsString(RuntimeExceptionOnCreation.class.getSimpleName()),
              containsString(RuntimeException.class.getSimpleName())
          ));
      throw error.getCause();
    }
  }

  @Test(expected = InstantiationException.class)
  public void givenBrokenExceptionThatCausesInstantiationException$whenResolve$thenError() throws Throwable {

    EOFException eofException = mock(EOFException.class);
    //noinspection unchecked
    when(eofException.getMessage()).thenThrow(InstantiationException.class);
    try {
      throw resolver.resolve(eofException);
    } catch (Error error) {
      assertThat(error.getMessage(),
          allOf(
              containsString(ExceptionForInstantiationExceptionTest.class.getSimpleName()),
              containsString(InstantiationException.class.getSimpleName())

          ));
      throw error.getCause();
    }
  }

  @Test(expected = IllegalAccessException.class)
  public void givenBrokenExceptionMappedToNonPublicConstructor$whenResolve$thenError() throws Throwable {
    try {
      throw resolver.resolve(new UnknownHostException());
    } catch (Error error) {
      assertThat(error.getMessage(),
          allOf(
              containsString(NonPublicConstructor.class.getSimpleName()),
              containsString(IllegalAccessException.class.getSimpleName())

          ));
      throw error.getCause();
    }
  }

  @Test(expected = NoSuchMethodException.class)
  public void givenBrokenExceptionMappedToNoMatchingConstructor$whenResolve$thenError() throws Throwable {
    try {
      throw resolver.resolve(new NoSuchObjectException(""));
    } catch (Error error) {
      assertThat(error.getMessage(),
          allOf(
              containsString(NoMatchingConstructor.class.getSimpleName()),
              containsString(NoSuchMethodException.class.getSimpleName())

          ));
      throw error.getCause();
    }
  }

  @Test(expected = Error.class)
  public void givenNull$whenResolve$thenError() {
    try {
      throw resolver.resolve(null);
    } catch (Error error) {
      assertEquals("null is not allowed here", error.getMessage());
      throw error;
    }
  }
}
