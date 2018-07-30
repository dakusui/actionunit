package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.utils.InternalUtils;
import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.core.Action;

public abstract class ActionScanner implements Action.Visitor {
  private int indentLevel = 0;

  @Override
  public void visit(Leaf action) {
    this.handleAction(action);
  }

  @Override
  public void visit(Named action) {
    this.handleAction(action);
    this.enter();
    try {
      action.action().accept(this);
    } finally {
      this.leave();
    }
  }

  @Override
  public void visit(Composite action) {
    this.handleAction(action);
    this.enter();
    try {
      action.children().forEach(
          each -> each.accept(this)
      );
    } finally {
      this.leave();
    }
  }

  @Override
  public <E> void visit(ForEach<E> action) {
    this.handleAction(action);
    this.enter();
    try {
      action.perform().accept(this);
    } finally {
      this.leave();
    }
  }

  @Override
  public void visit(When action) {
    this.handleAction(action);
    this.enter();
    try {
      action.perform().accept(this);
      action.otherwise().accept(this);
    } finally {
      this.leave();
    }
  }

  @Override
  public void visit(Attempt action) {
    this.handleAction(action);
    this.enter();
    try {
      action.perform().accept(this);
      action.recover().accept(this);
      action.ensure().accept(this);
    } finally {
      this.leave();
    }
  }

  @Override
  public void visit(Retry action) {
    this.handleAction(action);
    this.enter();
    try {
      action.perform().accept(this);
    } finally {
      this.leave();
    }
  }

  @Override
  public void visit(TimeOut action) {
    this.handleAction(action);
    this.enter();
    try {
      action.perform().accept(this);
    } finally {
      this.leave();
    }
  }

  protected abstract void handleAction(Action action);

  protected String indent() {
    return InternalUtils.indent(this.indentLevel);
  }

  private void enter() {
    indentLevel++;
  }

  private void leave() {
    indentLevel--;
  }
}
