package com.github.dakusui.actionunit.core;

import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class IdGeneratorManager {
  private Map<Context, ThreadLocal<AtomicInteger>> idGenerators = new Hashtable<>();

  private void init(Context key) {
    Objects.requireNonNull(key);
    if (!idGenerators.containsKey(key)) {
      idGenerators.put(key, new ThreadLocal<>());
    }
    if (idGenerators.get(key).get() == null) {
      idGenerators.get(key).set(new AtomicInteger(0));
    }
  }

  public int generateId(Context key) {
    init(key);
    return idGenerators.get(key).get().getAndIncrement();
  }

  public void reset(Context key) {
    init(key);
    if (idGenerators.get(key).get() != null) {
      idGenerators.get(key).get().set(0);
    }
  }
}
