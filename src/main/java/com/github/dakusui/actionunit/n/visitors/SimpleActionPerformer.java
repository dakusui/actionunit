package com.github.dakusui.actionunit.n.visitors;

import com.github.dakusui.actionunit.n.core.Action;
import com.github.dakusui.actionunit.n.core.Context;

public class SimpleActionPerformer extends ActionPerformer implements Action.Visitor {

  private SimpleActionPerformer() {
    this(Context.create());
  }

  private SimpleActionPerformer(Context context) {
    super(context);
  }

  @Override
  protected void callAccept(Action action, Action.Visitor visitor) {
    action.accept(visitor);
  }

  @Override
  protected SimpleActionPerformer newInstance(Context context) {
    return new SimpleActionPerformer(context);
  }

  public static SimpleActionPerformer create() {
    return new SimpleActionPerformer();
  }
}
