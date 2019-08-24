package com.github.dakusui.actionunit.core.context;

import java.util.stream.Stream;

@FunctionalInterface
public interface StreamSupplier<T> extends SerializableSupplier<Stream<T>> {
}
