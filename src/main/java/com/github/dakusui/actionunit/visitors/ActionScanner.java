package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.utils.InternalUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class ActionScanner implements Action.Visitor {
  private final List<Action> path = new LinkedList<>();

  @Override
  public void visit(Leaf action) {
    this.handleAction(action);
  }

  @Override
  public void visit(Named action) {
    this.handleAction(action);
    this.enter(action);
    try {
      action.action().accept(this);
    } finally {
      this.leave(action);
    }
  }

  @Override
  public void visit(Composite action) {
    this.handleAction(action);
    this.enter(action);
    try {
      action.children().forEach(
          each -> each.accept(this)
      );
    } finally {
      this.leave(action);
    }
  }

  @Override
  public <E> void visit(ForEach<E> action) {
    this.handleAction(action);
    this.enter(action);
    try {
      action.perform().accept(this);
    } finally {
      this.leave(action);
    }
  }

  @Override
  public void visit(While action) {
    this.handleAction(action);
    this.enter(action);
    try {
      action.perform().accept(this);
    } finally {
      this.leave(action);
    }
  }

  @Override
  public void visit(When action) {
    this.handleAction(action);
    this.enter(action);
    try {
      action.perform().accept(this);
      action.otherwise().accept(this);
    } finally {
      this.leave(action);
    }
  }

  @Override
  public <T> void visit(With action) {
    this.handleAction(action);
    this.enter(action);
    try {
      action.begin().accept(this);
      action.perform().accept(this);
      action.end().accept(this);
    } finally {
      this.leave(action);
    }
  }

  @Override
  public void visit(Attempt action) {
    this.handleAction(action);
    this.enter(action);
    try {
      action.perform().accept(this);
      action.recover().accept(this);
      action.ensure().accept(this);
    } finally {
      this.leave(action);
    }
  }

  @Override
  public void visit(Retry action) {
    this.handleAction(action);
    this.enter(action);
    try {
      action.perform().accept(this);
    } finally {
      this.leave(action);
    }
  }

  @Override
  public void visit(TimeOut action) {
    this.handleAction(action);
    this.enter(action);
    try {
      action.perform().accept(this);
    } finally {
      this.leave(action);
    }
  }

  protected abstract void handleAction(Action action);

  protected String indent() {
    return InternalUtils.indent(this.indentLevel(), 2);
  }

  protected void enter(Action action) {
    path.add(action);
  }

  protected void leave(Action action) {
    path.remove(path().size() - 1);
  }

  protected List<? extends Action> path() {
    return Collections.unmodifiableList(this.path);
  }

  protected int indentLevel() {
    return this.path.size();
  }
}
