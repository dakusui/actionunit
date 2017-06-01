package com.github.dakusui.actionunit.sandbox;

import com.github.dakusui.actionunit.utils.Helper;
import org.junit.Test;

import static com.github.dakusui.actionunit.utils.Helper.staticMethod;

public class HelperExample implements Helper {
  @Test
  public void run1() {
    staticMethod();
  }

  @Test
  public void run2() {
    defaultMethod();
  }

  @Test
  public void run3() {
    overriddenDefaultMethod();
  }

  @Override
  public void overriddenDefaultMethod() {
    System.out.println("Overridden:");
  }
}
