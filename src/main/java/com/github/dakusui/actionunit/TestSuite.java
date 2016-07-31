package com.github.dakusui.actionunit;

public interface TestSuite {
  Action setUp();

  Iterable<TestCase> testCases();

  Action tearDown();
}
