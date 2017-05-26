package com.github.dakusui.actionunit.sandbox;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

public class Sandbox {
  @Test
  public void testAtomicReference() {
    AtomicReference<String> ref = new AtomicReference<>();
    System.out.println(ref.get());
  }
}
