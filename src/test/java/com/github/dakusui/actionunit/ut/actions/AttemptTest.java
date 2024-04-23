package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.actions.ContextVariable;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.context.ContextFunctions;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static com.github.dakusui.actionunit.core.ActionSupport.attempt;
import static com.github.dakusui.actionunit.core.ActionSupport.leaf;
import static com.github.dakusui.actionunit.core.ActionSupport.sequential;
import static com.github.dakusui.crest.Crest.allOf;
import static com.github.dakusui.crest.Crest.asInteger;
import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;

public class AttemptTest extends TestUtils.TestBase {
  @Test
  public void givenAttemptAction$whenPerform$thenWorksFine() {
    List<String> out = new LinkedList<>();
    buildAttemptAction(out::add).accept(TestUtils.createActionPerformer());

    assertThat(
        out,
        allOf(
            asInteger("size").equalTo(2).$(),
            asString("get", 0).equalTo("UPDATED").$(),
            asString("get", 1).equalTo("UPDATED").$()));
  }

  private Action buildAttemptAction(Consumer<String> sink) {
    return sequential(
        leaf(ContextFunctions.assignTo(
            ContextVariable.createGlobal("i"),
            ContextFunctions.immediateOf("INITIAL"))),
        attempt(
            leaf(ContextFunctions.throwIllegalArgument())
        ).recover(
            IllegalArgumentException.class,
            leaf(
                ContextFunctions.assignTo(
                        ContextVariable.createGlobal("i"),
                        ContextFunctions.immediateOf("UPDATED"))
                    .andThen(
                        ContextFunctions.writeTo(sink, ContextFunctions.contextValueOf(ContextVariable.createGlobal("i"))).andThen(
                            ContextFunctions.printTo(System.out, ContextFunctions.contextValueOf(ContextVariable.createGlobal("i")))
                        )))
        ).ensure(
            leaf(ContextFunctions.writeTo(sink, ContextFunctions.contextValueOf(ContextVariable.createGlobal("i"))).andThen(
                ContextFunctions.printTo(System.out, ContextFunctions.contextValueOf(ContextVariable.createGlobal("i")))
            ))
        )
    );
  }
}
