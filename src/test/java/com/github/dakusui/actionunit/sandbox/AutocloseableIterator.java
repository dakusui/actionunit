package com.github.dakusui.actionunit.sandbox;

import java.util.Iterator;

public interface AutocloseableIterator<E> extends AutoCloseable, Iterator<E> {
  @Override
  void close();

  interface Factory<E> extends Iterable<E> {
    @Override
    AutocloseableIterator<E> iterator();
  }
}
