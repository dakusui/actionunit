package com.github.dakusui.actionunit;

import java.util.Iterator;

public interface AutocloseableIterator<E> extends AutoCloseable, Iterator<E> {
  @Override
  void close();

  interface Factory<E> extends Iterable<E> {
    @Override
    AutocloseableIterator<E> iterator();
  }
}
