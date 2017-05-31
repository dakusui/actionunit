package com.github.dakusui.actionunit.sandbox;


import com.github.dakusui.actionunit.helpers.Checks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A factory class to synthesize an implementation of a given interface (semi-)automatically.
 *
 * @param <T> A class of an interface for which an implementation is to be synthesized.
 */
public abstract class ObjectSynthesizer<T> {
  private final Class<T> anInterface;

  protected ObjectSynthesizer(Class<T> anInterface) {
    Checks.checkNotNull(anInterface);
    Checks.checkArgument(anInterface.isInterface());
    this.anInterface = Objects.requireNonNull(anInterface);
  }

  public T synthesize() {
    //noinspection unchecked
    return (T) Proxy.newProxyInstance(
        anInterface.getClassLoader(),
        new Class[] { anInterface },
        (proxy, method, args) -> handleMethodCall(method, args)
    );
  }

  protected Object handleMethodCall(Method method, Object[] args) {
    return lookUpMethodCallHandler(method).apply(args);
  }

  abstract protected Function<Object[], Object> lookUpMethodCallHandler(Method method);

  public static class WithDelegation<T> extends ObjectSynthesizer<T> {
    private final List<Object> delegatees;

    public WithDelegation(Class<T> anInterface, List<Object> delegatees) {
      super(anInterface);
      this.delegatees = delegatees;
    }

    protected Object route(Method method) {
      return chooseHandler(
          delegatees.stream().filter(compatibilityChecker(method)),
          method
      ).orElseThrow(
          () -> new UnsupportedOperationException(method.toString())
      );
    }

    /**
     * It is guaranteed that objects in {@code objectStream} are all satisfying
     * criterion defined by a predicate returned from {@code compatibilityChecker(Method)}
     * method.
     *
     * @param objectStream A stream contains objects to handle a method call.
     */
    protected Optional<Object> chooseHandler(Stream<Object> objectStream, Method method) {
      return objectStream.findFirst();
    }

    protected Predicate<Object> compatibilityChecker(Method method) {
      return o -> method.getDeclaringClass().isAssignableFrom(o.getClass());
    }

    protected Function<Object[], Object> lookUpMethodCallHandler(Method method) {
      return args -> invokeMethod(route(method), method, args);
    }

    private Object invokeMethod(Object object, Method method, Object[] args) {
      try {
        return method.invoke(object, args);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }

    static class Builder<T> {
      private final Class<T> anInterface;
      private List<Object> handlers = new LinkedList<>();

      public Builder(Class<T> anInterface) {
        this.anInterface = anInterface;
      }

      public Builder<T> add(Object handler) {
        handlers.add(handler);
        return this;
      }

      public ObjectSynthesizer<T> build() {
        return new WithDelegation<>(this.anInterface, new ArrayList<>(handlers));
      }
    }
  }

  interface A {
    void aMethod();
  }

  interface B {
    void bMethod();
  }

  interface C {
    void cMethod();
  }

  interface X extends A, B, C {
    void xMethod();
  }

  public static void main(String... args) {
    X x = new ObjectSynthesizer.WithDelegation.Builder<>(X.class)
        .add((A) () -> System.out.println("a is called"))
        .add((B) () -> System.out.println("b is called"))
        .add((C) () -> System.out.println("c is called"))
        .build()
        .synthesize();
    x.aMethod();
    x.bMethod();
    x.cMethod();
    System.out.println(x.toString());
    x.xMethod();
  }
}
