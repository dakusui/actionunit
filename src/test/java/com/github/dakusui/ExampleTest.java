package com.github.dakusui;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExampleTest {
  @ParameterizedTest
  @ValueSource(ints = { 1, 3, 5, -3, 15, Integer.MAX_VALUE })
    // six numbers
  void isOdd_ShouldReturnTrueForOddNumbers(int number) {
    assertTrue(Numbers.isOdd(number));
  }

  @ParameterizedTest
  @ValueSource(ints = { 1, 3, 5, -3, 15, Integer.MAX_VALUE })
    // six numbers
  void isEven_ShouldReturnTrueForOddNumbers(int number) {
    assertTrue(Numbers.isEven(number));
  }
}
