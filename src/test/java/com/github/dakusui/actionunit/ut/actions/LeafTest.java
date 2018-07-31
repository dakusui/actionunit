package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.actions.Leaf;
import com.github.dakusui.crest.Crest;
import org.junit.Test;

import static com.github.dakusui.actionunit.core.ActionSupport.simple;
import static com.github.dakusui.crest.Crest.asString;

public class LeafTest {
  @Test
  public void givenAnonymousLeaf$whenToString$thenNoname() {
    String s = String.format("%s", simple("NameOfRunnable", (context) -> {
    }));
    Crest.assertThat(s, asString().equalTo("NameOfRunnable").$());
  }

  @Test
  public void givenInheritedLeafWithoutName$whenToString$then() {
    String s = String.format("%s", Leaf.of(c -> {
    }));
    Crest.assertThat(s, asString().startsWith("(noname)").$());
  }
}
