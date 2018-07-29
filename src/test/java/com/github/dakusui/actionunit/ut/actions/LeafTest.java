package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.n.actions.Leaf;
import org.hamcrest.Matchers;
import org.junit.Test;

import static com.github.dakusui.actionunit.n.core.ActionSupport.simple;
import static org.junit.Assert.assertThat;

public class LeafTest {
  @Test
  public void givenAnonymousLeaf$whenToString$thenNoname() {
    String s = simple("NameOfRunnable", (context) -> {
    }).toString();
    assertThat(s, Matchers.equalTo("NameOfRunnable"));
  }

  @Test
  public void givenInheritedLeafWithoutName$whenToString$then() {
    String s = Leaf.of(c -> {
    }).toString();
    assertThat(s, Matchers.startsWith("LeafTest$"));
  }
}
