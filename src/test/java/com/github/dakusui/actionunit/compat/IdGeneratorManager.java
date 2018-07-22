package com.github.dakusui.actionunit.compat;

import com.github.dakusui.actionunit.core.Context;

import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Deprecated
public class IdGeneratorManager {
  public static final IdGeneratorManager                       ID_GENERATOR_MANAGER = new IdGeneratorManager();
  private             Map<Context, ThreadLocal<AtomicInteger>> idGenerators         = new Hashtable<>();

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
    return idGenerator(key).getAndIncrement();
  }

  public AtomicInteger idGenerator(Context key) {
    init(key);
    return idGenerators.get(key).get();
  }

  public void reset(Context key) {
    init(key);
    if (idGenerators.get(key).get() != null) {
      idGenerators.get(key).get().set(0);
    }
  }
}
