package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Action;

import java.util.function.Function;
import java.util.function.Supplier;

public interface ExceptionHandlerFactory<T> extends Function<Supplier<T>, Action> {
}
