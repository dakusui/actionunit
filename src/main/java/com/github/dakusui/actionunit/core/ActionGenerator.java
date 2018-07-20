package com.github.dakusui.actionunit.core;

import java.util.function.Function;

/**
 * Named this interface {@code ActionGenerator} because we already have {@code ActionFactory}.
 */
@FunctionalInterface
public interface ActionGenerator extends Function<Context, Action> {
}
