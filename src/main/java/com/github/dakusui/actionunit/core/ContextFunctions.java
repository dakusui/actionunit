package com.github.dakusui.actionunit.core;

public enum ContextFunctions {
  ;

  public static ContextPredicate.Builder contextPredicateFor(String variableName) {
    return new ContextPredicate.Builder(variableName);
  }

  public static ContextConsumer.Builder contextConsumerFor(String variableName) {
    return new ContextConsumer.Builder(variableName);
  }
}
