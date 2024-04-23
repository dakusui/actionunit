package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Context;

public interface ContextVariable {
  /**
   * A name of the variable.
   * The string returned by this method is used only for printing an action tree.
   * To identify a variable, a string returned by {@link Contextful#internalVariableName()}.
   *
   * @return A human-readable variable name.
   */
  String variableName();

  /**
   * @return An internal context variable name.
   */
  String internalVariableName();

  default <T> T resolve(Context context) {
    return context.valueOf(this.internalVariableName());
  }

  /**
   * @return Returns a global variable.
   */
  static ContextVariable createGlobal(String variableName) {
    return new ContextVariable() {
      @Override
      public String variableName() {
        return variableName;
      }

      @Override
      public String internalVariableName() {
        return "GLOBAL:" + variableName;
      }

      @Override
      public String toString() {
        return variableName();
      }
    };
  }
}
