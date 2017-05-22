package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Action;

import java.util.Optional;
import java.util.function.Function;

public interface Processor<T> extends Action, Function<Optional<T>, Action> {

}
