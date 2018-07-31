package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.actions.Leaf;
import org.hamcrest.Matchers;
import org.junit.Test;

import static com.github.dakusui.actionunit.core.ActionSupport.simple;
import static org.junit.Assert.assertThat;

public class LeafTest {
  @Test
  public void givenAnonymousLeaf$whenToString$thenNoname() {
    String s = String.format("%s", simple("NameOfRunnable", (context) -> {
    }));
    assertThat(s, Matchers.equalTo("NameOfRunnable"));
  }

  @Test
  public void givenInheritedLeafWithoutName$whenToString$then() {
    String s = String.format("%s", Leaf.of(c -> {
    }));
    assertThat(s, Matchers.startsWith("(noname)"));
  }
}
