package com.github.dakusui.actionunit.actions;

import org.hamcrest.Matchers;
import org.junit.Test;

import static com.github.dakusui.actionunit.Actions.simple;
import static org.junit.Assert.assertThat;

public class LeafTest {
  @Test
  public void givenAnonymousLeaf$whenToString$thenNoname() {
    String s = simple(new Runnable() {
      @Override
      public void run() {
      }
      @Override
      public String toString() {
        return "NameOfRunnable";
      }
    }).toString();
    assertThat(s, Matchers.equalTo("NameOfRunnable"));
  }

  @Test
  public void givenInheritedLeafWithoutName$whenToString$then() {
    String s = new Leaf() {
      @Override
      public void perform() {
      }
    }.toString();
    assertThat(s, Matchers.startsWith("LeafTest$"));
  }
}
