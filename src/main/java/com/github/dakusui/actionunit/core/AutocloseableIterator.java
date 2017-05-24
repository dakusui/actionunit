package com.github.dakusui.actionunit.core;

import java.util.Iterator;

public interface AutocloseableIterator<E> extends AutoCloseable, Iterator<E> {
  @Override
  void close();

  interface Factory<E> extends Iterable<E> {
    @Override
    AutocloseableIterator<E> iterator();
  }
}