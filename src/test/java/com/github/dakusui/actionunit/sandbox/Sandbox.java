package com.github.dakusui.actionunit.sandbox;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;

public class Sandbox {
  @Test
  public void testAtomicReference() {
    AtomicReference<String> ref = new AtomicReference<>();
    System.out.println(ref.get());
  }


  @Test
  public void testAutocloseable() {
    List<String> data = asList("A", "B", "C");
    StreamSupport.stream(
        ((Iterable<String>) () ->
            Autocloseables.autocloseable(
                data.iterator(),
                () -> System.err.println("closed!")
            )).spliterator(),
        false
    ).forEach(System.out::println);
  }
}
